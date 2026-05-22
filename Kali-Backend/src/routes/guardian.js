const router = require('express').Router();
const { authenticateToken, requireRole } = require('../middleware/auth');
const { linkUser, unlinkUser, getMyUser, getActiveAlert } = require('../controllers/guardianController');

router.use(authenticateToken);
router.post('/link', requireRole('Guardian', 'Admin'), linkUser);
router.delete('/unlink', requireRole('Guardian'), unlinkUser);
router.get('/my-user', requireRole('Guardian', 'Admin'), getMyUser);
router.get('/active-alert', requireRole('Guardian'), getActiveAlert);

module.exports = router;
