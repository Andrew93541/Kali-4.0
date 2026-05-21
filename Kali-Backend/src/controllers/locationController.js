const pool = require('../config/db');

/**
 * Haversine formula – compute great-circle distance between two lat/lng pairs.
 * Returns distance in kilometres.
 */
function haversine(lat1, lng1, lat2, lng2) {
  const R = 6371; // Earth radius km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLng = (lng2 - lng1) * Math.PI / 180;
  const a = Math.sin(dLat / 2) ** 2 +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLng / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

/**
 * Compute bearing (in degrees) from point 1 to point 2.
 */
function computeBearing(lat1, lng1, lat2, lng2) {
  const dLng = (lng2 - lng1) * Math.PI / 180;
  const y = Math.sin(dLng) * Math.cos(lat2 * Math.PI / 180);
  const x = Math.cos(lat1 * Math.PI / 180) * Math.sin(lat2 * Math.PI / 180) -
            Math.sin(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.cos(dLng);
  return ((Math.atan2(y, x) * 180 / Math.PI) + 360) % 360;
}

/**
 * POST /api/locations
 * Add a single location point for an alert.
 * Broadcasts enriched data via Socket.io including computed speed & bearing.
 */
async function addLocation(req, res) {
  const { alert_id, latitude, longitude, accuracy } = req.body;

  if (!alert_id || latitude == null || longitude == null) {
    return res.status(400).json({ error: 'alert_id, latitude, and longitude are required' });
  }

  const result = await pool.query(
    'INSERT INTO locations (alert_id, latitude, longitude) VALUES ($1,$2,$3) RETURNING *',
    [alert_id, latitude, longitude]
  );

  // Compute speed & bearing from previous location
  const prev = await pool.query(
    'SELECT * FROM locations WHERE alert_id=$1 ORDER BY timestamp DESC LIMIT 1 OFFSET 1',
    [alert_id]
  );

  let speed = 0;
  let bearing = 0;

  if (prev.rows.length > 0) {
    const p = prev.rows[0];
    const dist = haversine(p.latitude, p.longitude, latitude, longitude);
    const now = new Date(result.rows[0].timestamp || Date.now());
    const then = new Date(p.timestamp);
    const hoursElapsed = (now - then) / 3600000;
    if (hoursElapsed > 0 && dist > 0.001) {
      speed = Math.min(dist / hoursElapsed, 200); // km/h, capped
    }
    bearing = computeBearing(p.latitude, p.longitude, latitude, longitude);
  }

  // Broadcast enriched location update
  req.io.emit('locationUpdate', {
    alertId: alert_id,
    latitude,
    longitude,
    speed,
    bearing,
    accuracy: accuracy || null,
    timestamp: result.rows[0].timestamp
  });

  res.status(201).json(result.rows[0]);
}

/**
 * POST /api/locations/batch
 * Accept an array of location points for offline-to-online sync.
 * Useful when the mobile device reconnects after a period of no connectivity.
 */
async function addLocationBatch(req, res) {
  const { alert_id, points } = req.body;

  if (!alert_id || !Array.isArray(points) || points.length === 0) {
    return res.status(400).json({ error: 'alert_id and a non-empty points array are required' });
  }

  const inserted = [];
  for (const pt of points) {
    if (pt.latitude == null || pt.longitude == null) continue;
    const result = await pool.query(
      'INSERT INTO locations (alert_id, latitude, longitude) VALUES ($1,$2,$3) RETURNING *',
      [alert_id, pt.latitude, pt.longitude]
    );
    inserted.push(result.rows[0]);
  }

  // Broadcast only the latest point to avoid flooding the UI
  if (inserted.length > 0) {
    const latest = inserted[inserted.length - 1];
    req.io.emit('locationUpdate', {
      alertId: alert_id,
      latitude: latest.latitude,
      longitude: latest.longitude,
      speed: 0,
      bearing: 0,
      timestamp: latest.timestamp,
      batchSize: inserted.length
    });
  }

  res.status(201).json({ count: inserted.length, points: inserted });
}

/**
 * GET /api/locations/:alert_id
 * Retrieve all location points for an alert, ordered chronologically.
 */
async function getLocations(req, res) {
  const result = await pool.query(
    'SELECT * FROM locations WHERE alert_id=$1 ORDER BY timestamp ASC',
    [req.params.alert_id]
  );
  res.json(result.rows);
}

/**
 * GET /api/locations/:alert_id/stats
 * Compute and return aggregated telemetry statistics for an alert's location trail.
 */
async function getLocationStats(req, res) {
  const result = await pool.query(
    'SELECT * FROM locations WHERE alert_id=$1 ORDER BY timestamp ASC',
    [req.params.alert_id]
  );

  const locs = result.rows;
  if (locs.length < 2) {
    return res.json({
      totalDistance: 0,
      avgSpeed: 0,
      maxSpeed: 0,
      duration: 0,
      pointCount: locs.length,
      lastBearing: 0
    });
  }

  let totalDist = 0;
  let maxSpeed = 0;
  const speeds = [];

  for (let i = 1; i < locs.length; i++) {
    const dist = haversine(locs[i - 1].latitude, locs[i - 1].longitude, locs[i].latitude, locs[i].longitude);
    const dt = (new Date(locs[i].timestamp) - new Date(locs[i - 1].timestamp)) / 3600000;
    totalDist += dist;
    if (dt > 0 && dist > 0.001) {
      const spd = Math.min(dist / dt, 200);
      speeds.push(spd);
      if (spd > maxSpeed) maxSpeed = spd;
    }
  }

  const avgSpeed = speeds.length > 0 ? speeds.reduce((a, b) => a + b, 0) / speeds.length : 0;
  const duration = (new Date(locs[locs.length - 1].timestamp) - new Date(locs[0].timestamp)) / 1000; // seconds
  const lastBearing = computeBearing(
    locs[locs.length - 2].latitude, locs[locs.length - 2].longitude,
    locs[locs.length - 1].latitude, locs[locs.length - 1].longitude
  );

  res.json({
    totalDistance: Math.round(totalDist * 1000) / 1000,
    avgSpeed: Math.round(avgSpeed * 10) / 10,
    maxSpeed: Math.round(maxSpeed * 10) / 10,
    duration: Math.round(duration),
    pointCount: locs.length,
    lastBearing: Math.round(lastBearing)
  });
}

module.exports = { addLocation, addLocationBatch, getLocations, getLocationStats };
