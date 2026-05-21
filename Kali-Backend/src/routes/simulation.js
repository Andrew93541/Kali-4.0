const router = require('express').Router();
const { authenticateToken } = require('../middleware/auth');
const {
  startSimulation,
  stopSimulation,
  getSimulationStatus,
  startDemoScenario
} = require('../controllers/simulationController');

router.use(authenticateToken);

// Start a location simulation for a specific alert
router.post('/start', startSimulation);

// Stop a running simulation
router.post('/stop', stopSimulation);

// Check which simulations are running
router.get('/status', getSimulationStatus);

// One-click demo: create alert + start simulation
router.post('/demo', startDemoScenario);

module.exports = router;
