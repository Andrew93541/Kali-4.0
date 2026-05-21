const pool = require('../config/db');

/**
 * Simulation Controller
 * Generates realistic GPS location streams for testing the Swiggy-style tracking engine.
 * Simulates movement patterns including walking, running, vehicle travel, and GPS jitter.
 */

// Active simulation intervals keyed by alertId
const activeSimulations = {};

/**
 * POST /api/simulate/start
 * Start a simulated location stream for an alert.
 * Body: { alert_id, pattern: 'walk'|'run'|'vehicle'|'erratic', startLat, startLng }
 */
async function startSimulation(req, res) {
  const { alert_id, pattern = 'walk', startLat = 28.6139, startLng = 77.2090 } = req.body;

  if (!alert_id) {
    return res.status(400).json({ error: 'alert_id is required' });
  }

  if (activeSimulations[alert_id]) {
    return res.status(409).json({ error: 'Simulation already running for this alert' });
  }

  // Speed profiles (degrees per second, roughly)
  const profiles = {
    walk:    { speedDeg: 0.000015, jitter: 0.00003,  intervalMs: 3000,  name: 'Walking' },
    run:     { speedDeg: 0.00005,  jitter: 0.00004,  intervalMs: 2000,  name: 'Running' },
    vehicle: { speedDeg: 0.0003,   jitter: 0.00005,  intervalMs: 1500,  name: 'Vehicle' },
    erratic: { speedDeg: 0.0001,   jitter: 0.0002,   intervalMs: 2500,  name: 'Erratic/Panic' }
  };

  const profile = profiles[pattern] || profiles.walk;

  let currentLat = startLat;
  let currentLng = startLng;
  let bearing = Math.random() * 360; // Initial random direction
  let step = 0;

  const intervalId = setInterval(async () => {
    step++;

    // Gradually curve the path (simulate turning corners, not a straight line)
    bearing += (Math.random() - 0.5) * 30; // ±15 degree random turn each step
    if (bearing < 0) bearing += 360;
    if (bearing >= 360) bearing -= 360;

    // Compute next position along bearing with GPS jitter
    const bearingRad = bearing * Math.PI / 180;
    currentLat += profile.speedDeg * Math.cos(bearingRad) + (Math.random() - 0.5) * profile.jitter;
    currentLng += profile.speedDeg * Math.sin(bearingRad) + (Math.random() - 0.5) * profile.jitter;

    try {
      const result = await pool.query(
        'INSERT INTO locations (alert_id, latitude, longitude) VALUES ($1,$2,$3) RETURNING *',
        [alert_id, currentLat, currentLng]
      );

      // Broadcast via Socket.io
      req.io.emit('locationUpdate', {
        alertId: alert_id,
        latitude: currentLat,
        longitude: currentLng,
        speed: 0,
        bearing,
        timestamp: result.rows[0].timestamp,
        simulated: true
      });
    } catch (err) {
      console.error('Simulation location insert error:', err.message);
    }

    // Auto-stop after 200 points (about 5-10 minutes depending on interval)
    if (step >= 200) {
      clearInterval(intervalId);
      delete activeSimulations[alert_id];
      console.log(`Simulation for alert ${alert_id} completed (${step} points).`);
    }
  }, profile.intervalMs);

  activeSimulations[alert_id] = { intervalId, profile: profile.name, startedAt: Date.now(), step: 0 };

  res.json({
    status: 'started',
    alert_id,
    pattern: profile.name,
    intervalMs: profile.intervalMs,
    startLocation: { lat: startLat, lng: startLng },
    message: `Simulating ${profile.name} movement. Points will be emitted every ${profile.intervalMs}ms.`
  });
}

/**
 * POST /api/simulate/stop
 * Stop an active simulation for an alert.
 * Body: { alert_id }
 */
async function stopSimulation(req, res) {
  const { alert_id } = req.body;

  if (!alert_id) {
    return res.status(400).json({ error: 'alert_id is required' });
  }

  if (!activeSimulations[alert_id]) {
    return res.status(404).json({ error: 'No active simulation for this alert' });
  }

  clearInterval(activeSimulations[alert_id].intervalId);
  const sim = activeSimulations[alert_id];
  delete activeSimulations[alert_id];

  res.json({
    status: 'stopped',
    alert_id,
    pattern: sim.profile,
    duration: Math.round((Date.now() - sim.startedAt) / 1000) + 's'
  });
}

/**
 * GET /api/simulate/status
 * List all currently active simulations.
 */
function getSimulationStatus(req, res) {
  const status = Object.entries(activeSimulations).map(([alertId, sim]) => ({
    alertId: parseInt(alertId),
    pattern: sim.profile,
    runningFor: Math.round((Date.now() - sim.startedAt) / 1000) + 's'
  }));

  res.json({ active: status.length, simulations: status });
}

/**
 * POST /api/simulate/demo-scenario
 * Create a complete demo scenario: register a user alert and immediately start simulation.
 * This is a convenience endpoint for quick testing.
 */
async function startDemoScenario(req, res) {
  const { pattern = 'walk', city = 'delhi' } = req.body;

  const cityCoords = {
    delhi:   { lat: 28.6139, lng: 77.2090 },
    mumbai:  { lat: 19.0760, lng: 72.8777 },
    bangalore: { lat: 12.9716, lng: 77.5946 },
    chennai: { lat: 13.0827, lng: 80.2707 },
    kolkata: { lat: 22.5726, lng: 88.3639 },
    hyderabad: { lat: 17.3850, lng: 78.4867 }
  };

  const coords = cityCoords[city.toLowerCase()] || cityCoords.delhi;

  // Find the demo user
  const userResult = await pool.query('SELECT id FROM users WHERE email=$1', ['user@kali.com']);
  if (userResult.rows.length === 0) {
    return res.status(404).json({ error: 'Demo user not found. Ensure database is seeded.' });
  }
  const userId = userResult.rows[0].id;

  // Create an alert
  const alertResult = await pool.query(
    'INSERT INTO alerts (user_id) VALUES ($1) RETURNING *',
    [userId]
  );
  const alert = alertResult.rows[0];

  // Insert initial location
  await pool.query(
    'INSERT INTO locations (alert_id, latitude, longitude) VALUES ($1,$2,$3)',
    [alert.id, coords.lat, coords.lng]
  );

  // Broadcast new alert
  req.io.emit('newAlert', {
    alertId: alert.id,
    userId,
    latitude: coords.lat,
    longitude: coords.lng,
    timestamp: alert.timestamp
  });

  // Start simulation on this alert
  req.body.alert_id = alert.id;
  req.body.startLat = coords.lat;
  req.body.startLng = coords.lng;

  // Inline simulation start
  const profiles = {
    walk:    { speedDeg: 0.000015, jitter: 0.00003,  intervalMs: 3000,  name: 'Walking' },
    run:     { speedDeg: 0.00005,  jitter: 0.00004,  intervalMs: 2000,  name: 'Running' },
    vehicle: { speedDeg: 0.0003,   jitter: 0.00005,  intervalMs: 1500,  name: 'Vehicle' },
    erratic: { speedDeg: 0.0001,   jitter: 0.0002,   intervalMs: 2500,  name: 'Erratic/Panic' }
  };
  const profile = profiles[pattern] || profiles.walk;

  let currentLat = coords.lat;
  let currentLng = coords.lng;
  let bearing = Math.random() * 360;
  let step = 0;

  const intervalId = setInterval(async () => {
    step++;
    bearing += (Math.random() - 0.5) * 30;
    if (bearing < 0) bearing += 360;
    if (bearing >= 360) bearing -= 360;

    const bearingRad = bearing * Math.PI / 180;
    currentLat += profile.speedDeg * Math.cos(bearingRad) + (Math.random() - 0.5) * profile.jitter;
    currentLng += profile.speedDeg * Math.sin(bearingRad) + (Math.random() - 0.5) * profile.jitter;

    try {
      const result = await pool.query(
        'INSERT INTO locations (alert_id, latitude, longitude) VALUES ($1,$2,$3) RETURNING *',
        [alert.id, currentLat, currentLng]
      );
      req.io.emit('locationUpdate', {
        alertId: alert.id,
        latitude: currentLat,
        longitude: currentLng,
        speed: 0,
        bearing,
        timestamp: result.rows[0].timestamp,
        simulated: true
      });
    } catch (err) {
      console.error('Demo simulation error:', err.message);
    }

    if (step >= 200) {
      clearInterval(intervalId);
      delete activeSimulations[alert.id];
    }
  }, profile.intervalMs);

  activeSimulations[alert.id] = { intervalId, profile: profile.name, startedAt: Date.now() };

  res.status(201).json({
    status: 'demo_started',
    alertId: alert.id,
    city,
    pattern: profile.name,
    startLocation: coords,
    message: `Demo SOS alert #${alert.id} created in ${city}. ${profile.name} simulation active. Open the Police or Guardian dashboard to watch it live.`
  });
}

module.exports = { startSimulation, stopSimulation, getSimulationStatus, startDemoScenario };
