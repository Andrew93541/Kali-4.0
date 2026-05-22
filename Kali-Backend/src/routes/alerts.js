const router = require('express').Router();
const { authenticateToken, requireRole } = require('../middleware/auth');
const { createAlert, getAlert, listAlerts, getMyAlerts, resolveAlert, assignOfficer } = require('../controllers/alertController');

router.use(authenticateToken);
router.post('/', createAlert);
router.get('/', listAlerts);
router.get('/my', getMyAlerts);
router.get('/:id', getAlert);
router.post('/:id/resolve', requireRole('Admin', 'Guardian'), resolveAlert);
router.post('/:id/assign', requireRole('Admin'), assignOfficer);

module.exports = router;
