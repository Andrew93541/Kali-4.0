const router = require('express').Router();
const { authenticateToken, requireRole } = require('../middleware/auth');
const { createAlert, getAlert, listAlerts, resolveAlert, assignOfficer } = require('../controllers/alertController');

router.use(authenticateToken);
router.post('/', createAlert);
router.get('/', listAlerts);
router.get('/:id', getAlert);
router.post('/:id/resolve', requireRole('Admin'), resolveAlert);
router.post('/:id/assign', requireRole('Admin'), assignOfficer);

module.exports = router;
