const pool = require('../config/db');

// POST /api/guardian/link — Guardian links themselves to a User by phone/email
async function linkUser(req, res) {
  if (req.user.role !== 'Guardian' && req.user.role !== 'Admin') {
    return res.status(403).json({ error: 'Only Guardians can link to users' });
  }

  const { userEmail, userPhone } = req.body;
  if (!userEmail && !userPhone) return res.status(400).json({ error: 'User email or phone required' });

  let query = 'SELECT id, name, email, phone FROM users WHERE role=$1 AND ';
  const params = ['User'];
  if (userEmail) { query += 'email=$2'; params.push(userEmail); }
  else { query += 'phone=$2'; params.push(userPhone); }

  const result = await pool.query(query, params);
  const user = result.rows[0];
  if (!user) return res.status(404).json({ error: 'User not found with that email/phone' });

  await pool.query('UPDATE users SET guardian_id=$1 WHERE id=$2', [req.user.id, user.id]);
  res.json({ success: true, linkedUser: { id: user.id, name: user.name, email: user.email, phone: user.phone } });
}

// DELETE /api/guardian/unlink — Guardian unlinks from their user
async function unlinkUser(req, res) {
  await pool.query('UPDATE users SET guardian_id=NULL WHERE guardian_id=$1', [req.user.id]);
  res.json({ success: true });
}

// GET /api/guardian/my-user — Guardian gets the profile of their linked user
async function getMyUser(req, res) {
  const result = await pool.query(
    'SELECT id, name, email, phone, role FROM users WHERE guardian_id=$1',
    [req.user.id]
  );
  if (!result.rows.length) return res.json({ linkedUser: null });
  res.json({ linkedUser: result.rows[0] });
}

// GET /api/guardian/active-alert — Guardian gets the active alert of their linked user
async function getActiveAlert(req, res) {
  // Find the user linked to this guardian
  const userRes = await pool.query('SELECT id FROM users WHERE guardian_id=$1', [req.user.id]);
  if (!userRes.rows.length) return res.json({ alert: null, message: 'No linked user' });

  const userId = userRes.rows[0].id;
  const alertRes = await pool.query(
    `SELECT a.*, u.name, u.phone FROM alerts a JOIN users u ON a.user_id=u.id
     WHERE a.user_id=$1 AND a.status='active' ORDER BY a.timestamp DESC LIMIT 1`,
    [userId]
  );
  res.json({ alert: alertRes.rows[0] || null });
}

module.exports = { linkUser, unlinkUser, getMyUser, getActiveAlert };
