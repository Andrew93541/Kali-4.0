const router = require('express').Router();
const { authenticateToken } = require('../middleware/auth');
const { listContacts, addContact, updateContact, deleteContact, getContactsForUser } = require('../controllers/contactsController');

router.use(authenticateToken);
router.get('/', listContacts);
router.post('/', addContact);
router.put('/:id', updateContact);
router.delete('/:id', deleteContact);
router.get('/for/:userId', getContactsForUser);

module.exports = router;
