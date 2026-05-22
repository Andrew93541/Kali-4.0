const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const pool = require('../config/db');

async function register(req, res) {
  const { name, phone, email, password, role = 'User' } = req.body;
  if (!email || !password || !name) return res.status(400).json({ error: 'Name, email, and password are required' });
  if (!['User', 'Guardian', 'Admin'].includes(role)) return res.status(400).json({ error: 'Invalid role' });
  const hash = await bcrypt.hash(password, 10);
  try {
    const result = await pool.query(
      'INSERT INTO users (name, phone, email, password_hash, role) VALUES ($1,$2,$3,$4,$5) RETURNING id, name, email, role, phone',
      [name, phone || null, email, hash, role]
    );
    const user = result.rows[0] || { id: result.insertId, name, email, role, phone };
    res.status(201).json(user);
  } catch (e) {
    const isUnique = e.code === '23505' || (e.message && e.message.includes('UNIQUE constraint'));
    if (isUnique) return res.status(409).json({ error: 'Email already registered' });
    console.error('Register error:', e.message);
    res.status(500).json({ error: 'Server error' });
  }
}

async function login(req, res) {
  const { email, password } = req.body;
  if (!email || !password) return res.status(400).json({ error: 'Email and password required' });
  const result = await pool.query('SELECT * FROM users WHERE email=$1', [email]);
  const user = result.rows[0];
  if (!user || !(await bcrypt.compare(password, user.password_hash)))
    return res.status(401).json({ error: 'Invalid credentials' });
  const token = jwt.sign({ userId: user.id, role: user.role }, process.env.JWT_SECRET, { expiresIn: '7d' });
  res.json({ token, role: user.role, name: user.name, phone: user.phone, userId: user.id });
}

async function googleLogin(req, res) {
  const { email, name, googleId, role = 'User' } = req.body;
  if (!email) return res.status(400).json({ error: 'Email required' });

  try {
    const result = await pool.query('SELECT * FROM users WHERE email=$1', [email]);
    let user = result.rows[0];

    if (!user) {
      const validRole = ['User', 'Guardian', 'Admin'].includes(role) ? role : 'User';
      const tempPassword = Math.random().toString(36).slice(-10) + 'A1!';
      const hash = await bcrypt.hash(tempPassword, 10);
      const insertResult = await pool.query(
        'INSERT INTO users (name, email, password_hash, role) VALUES ($1,$2,$3,$4) RETURNING *',
        [name || email.split('@')[0], email, hash, validRole]
      );
      user = insertResult.rows[0] || { id: insertResult.insertId, name: name || email.split('@')[0], email, role: validRole };
    }

    const token = jwt.sign({ userId: user.id, role: user.role }, process.env.JWT_SECRET, { expiresIn: '7d' });
    res.json({ token, role: user.role, name: user.name, phone: user.phone || null, userId: user.id });
  } catch (e) {
    console.error('Google Auth error:', e.message);
    res.status(500).json({ error: 'Server error' });
  }
}

async function getProfile(req, res) {
  const result = await pool.query(
    'SELECT id, name, email, phone, role, guardian_id, created_at FROM users WHERE id=$1',
    [req.user.id]
  );
  if (!result.rows[0]) return res.status(404).json({ error: 'User not found' });
  res.json(result.rows[0]);
}

async function updateProfile(req, res) {
  const { name, phone, fcm_token } = req.body;
  const updates = [];
  const params = [];

  if (name) { updates.push(`name=$${params.length + 1}`); params.push(name); }
  if (phone) { updates.push(`phone=$${params.length + 1}`); params.push(phone); }
  if (fcm_token) { updates.push(`fcm_token=$${params.length + 1}`); params.push(fcm_token); }

  if (updates.length === 0) return res.status(400).json({ error: 'Nothing to update' });

  params.push(req.user.id);
  await pool.query(`UPDATE users SET ${updates.join(',')} WHERE id=$${params.length}`, params);
  const result = await pool.query('SELECT id, name, email, phone, role FROM users WHERE id=$1', [req.user.id]);
  res.json(result.rows[0]);
}

module.exports = { register, login, googleLogin, getProfile, updateProfile };
