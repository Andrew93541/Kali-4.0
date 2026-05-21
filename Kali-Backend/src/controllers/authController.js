const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const pool = require('../config/db');

async function register(req, res) {
  const { name, phone, email, password, role = 'User' } = req.body;
  if (!email || !password) return res.status(400).json({ error: 'Email and password required' });
  const hash = await bcrypt.hash(password, 10);
  try {
    const result = await pool.query(
      'INSERT INTO users (name, phone, email, password_hash, role) VALUES ($1,$2,$3,$4,$5) RETURNING id, name, email, role',
      [name, phone, email, hash, role]
    );
    res.status(201).json(result.rows[0]);
  } catch (e) {
    const isUniqueViolation = e.code === '23505' || 
                              e.code === 'ERR_SQLITE_ERROR' || 
                              (e.message && e.message.includes('UNIQUE constraint'));
    if (isUniqueViolation) return res.status(409).json({ error: 'Email already registered' });
    res.status(500).json({ error: 'Server error' });
  }
}

async function login(req, res) {
  const { email, password } = req.body;
  const result = await pool.query('SELECT * FROM users WHERE email=$1', [email]);
  const user = result.rows[0];
  if (!user || !(await bcrypt.compare(password, user.password_hash)))
    return res.status(401).json({ error: 'Invalid credentials' });
  const token = jwt.sign({ userId: user.id, role: user.role }, process.env.JWT_SECRET, { expiresIn: '7d' });
  res.json({ token, role: user.role, name: user.name });
}

async function googleLogin(req, res) {
  const { email, name, googleId } = req.body;
  if (!email) return res.status(400).json({ error: 'Email required' });

  try {
    const result = await pool.query('SELECT * FROM users WHERE email=$1', [email]);
    let user = result.rows[0];

    if (!user) {
      const tempPassword = Math.random().toString(36).slice(-10) + 'A1!';
      const hash = await bcrypt.hash(tempPassword, 10);
      const insertResult = await pool.query(
        'INSERT INTO users (name, email, password_hash, role) VALUES ($1,$2,$3,$4) RETURNING *',
        [name || email.split('@')[0], email, hash, 'User']
      );
      user = insertResult.rows[0];
    }

    const token = jwt.sign({ userId: user.id, role: user.role }, process.env.JWT_SECRET, { expiresIn: '7d' });
    res.json({ token, role: user.role, name: user.name });
  } catch (e) {
    console.error('Google Auth error:', e);
    res.status(500).json({ error: 'Server error' });
  }
}

module.exports = { register, login, googleLogin };
