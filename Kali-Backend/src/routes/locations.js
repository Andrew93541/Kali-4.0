const router = require('express').Router();
const { authenticateToken } = require('../middleware/auth');
const { addLocation, addLocationBatch, getLocations, getLocationStats } = require('../controllers/locationController');

router.use(authenticateToken);

// Single location point
router.post('/', addLocation);

// Batch location upload (for offline sync)
router.post('/batch', addLocationBatch);

// Get all locations for an alert
router.get('/:alert_id', getLocations);

// Get computed telemetry stats for an alert
router.get('/:alert_id/stats', getLocationStats);

module.exports = router;
