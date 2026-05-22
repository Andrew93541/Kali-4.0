const pool = require('../config/db');

// GET /api/contacts — list emergency contacts for authenticated user
async function listContacts(req, res) {
  const result = await pool.query(
    'SELECT * FROM emergency_contacts WHERE user_id=$1 ORDER BY is_primary DESC, created_at ASC',
    [req.user.id]
  );
  res.json(result.rows);
}

// POST /api/contacts — add an emergency contact
async function addContact(req, res) {
  const { name, phone, relation, is_primary } = req.body;
  if (!name || !phone) return res.status(400).json({ error: 'Name and phone are required' });

  // If marking as primary, unmark others first
  if (is_primary) {
    await pool.query('UPDATE emergency_contacts SET is_primary=0 WHERE user_id=$1', [req.user.id]);
  }

  const result = await pool.query(
    'INSERT INTO emergency_contacts (user_id, name, phone, relation, is_primary) VALUES ($1,$2,$3,$4,$5) RETURNING *',
    [req.user.id, name, phone, relation || null, is_primary ? 1 : 0]
  );
  const contact = result.rows[0] || { id: result.insertId, user_id: req.user.id, name, phone, relation, is_primary: is_primary ? 1 : 0 };
  res.status(201).json(contact);
}

// PUT /api/contacts/:id — update a contact
async function updateContact(req, res) {
  const { name, phone, relation, is_primary } = req.body;
  const { id } = req.params;

  // Verify ownership
  const check = await pool.query('SELECT id FROM emergency_contacts WHERE id=$1 AND user_id=$2', [id, req.user.id]);
  if (!check.rows[0]) return res.status(404).json({ error: 'Contact not found' });

  if (is_primary) {
    await pool.query('UPDATE emergency_contacts SET is_primary=0 WHERE user_id=$1', [req.user.id]);
  }

  await pool.query(
    'UPDATE emergency_contacts SET name=$1, phone=$2, relation=$3, is_primary=$4 WHERE id=$5',
    [name, phone, relation || null, is_primary ? 1 : 0, id]
  );
  res.json({ success: true });
}

// DELETE /api/contacts/:id — remove a contact
async function deleteContact(req, res) {
  const check = await pool.query('SELECT id FROM emergency_contacts WHERE id=$1 AND user_id=$2', [req.params.id, req.user.id]);
  if (!check.rows[0]) return res.status(404).json({ error: 'Contact not found' });
  await pool.query('DELETE FROM emergency_contacts WHERE id=$1', [req.params.id]);
  res.json({ success: true });
}

// GET /api/contacts/for/:userId — used by Guardian/Admin to see victim's contacts
async function getContactsForUser(req, res) {
  // Only Guardian linked to this user or Admin can access
  if (req.user.role === 'Guardian') {
    const check = await pool.query('SELECT id FROM users WHERE id=$1 AND guardian_id=$2', [req.params.userId, req.user.id]);
    if (!check.rows[0]) return res.status(403).json({ error: 'Not authorized' });
  } else if (req.user.role !== 'Admin') {
    return res.status(403).json({ error: 'Not authorized' });
  }

  const result = await pool.query(
    'SELECT * FROM emergency_contacts WHERE user_id=$1 ORDER BY is_primary DESC',
    [req.params.userId]
  );
  res.json(result.rows);
}

module.exports = { listContacts, addContact, updateContact, deleteContact, getContactsForUser };
