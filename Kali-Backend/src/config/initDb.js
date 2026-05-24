const pool = require('./db');
const bcrypt = require('bcryptjs');

async function initDb() {
  // Core users table with guardian_id + fcm_token
  await pool.query(`
    CREATE TABLE IF NOT EXISTS users (
      id SERIAL PRIMARY KEY,
      name VARCHAR(100),
      email VARCHAR(100) UNIQUE NOT NULL,
      phone VARCHAR(20),
      password_hash VARCHAR(255) NOT NULL,
      role VARCHAR(20) NOT NULL DEFAULT 'User',
      guardian_id INTEGER,
      fcm_token TEXT,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS alerts (
      id SERIAL PRIMARY KEY,
      user_id INT,
      timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      status VARCHAR(20) DEFAULT 'active',
      resolved_at TIMESTAMP,
      resolve_note TEXT
    )
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS locations (
      id SERIAL PRIMARY KEY,
      alert_id INT,
      latitude REAL,
      longitude REAL,
      accuracy REAL,
      timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS media (
      id SERIAL PRIMARY KEY,
      alert_id INT,
      file_url TEXT,
      media_type VARCHAR(10),
      file_hash VARCHAR(64),
      upload_status VARCHAR(10) DEFAULT 'uploaded',
      timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS emergency_contacts (
      id SERIAL PRIMARY KEY,
      user_id INT,
      name VARCHAR(100) NOT NULL,
      phone VARCHAR(20) NOT NULL,
      relation VARCHAR(50),
      is_primary INTEGER DEFAULT 0,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS audit_log (
      id SERIAL PRIMARY KEY,
      alert_id INT,
      action VARCHAR(100),
      user_id INT,
      entry_hash VARCHAR(64),
      timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
  `);

  // Indexes
  await pool.query(`CREATE INDEX IF NOT EXISTS idx_alerts_user ON alerts(user_id)`);
  await pool.query(`CREATE INDEX IF NOT EXISTS idx_alerts_status ON alerts(status)`);
  await pool.query(`CREATE INDEX IF NOT EXISTS idx_locations_alert ON locations(alert_id)`);
  await pool.query(`CREATE INDEX IF NOT EXISTS idx_media_alert ON media(alert_id)`);
  await pool.query(`CREATE INDEX IF NOT EXISTS idx_contacts_user ON emergency_contacts(user_id)`);

  // Add missing columns to existing tables (safe migrations)
  try { await pool.query(`ALTER TABLE users ADD COLUMN guardian_id INTEGER`); } catch(e) { /* already exists */ }
  try { await pool.query(`ALTER TABLE users ADD COLUMN fcm_token TEXT`); } catch(e) { /* already exists */ }
  try { await pool.query(`ALTER TABLE users ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP`); } catch(e) { /* already exists */ }
  try { await pool.query(`ALTER TABLE alerts ADD COLUMN resolved_at TIMESTAMP`); } catch(e) { /* already exists */ }
  try { await pool.query(`ALTER TABLE alerts ADD COLUMN resolve_note TEXT`); } catch(e) { /* already exists */ }
  try { await pool.query(`ALTER TABLE locations ADD COLUMN accuracy REAL`); } catch(e) { /* already exists */ }

  console.log('✅ Database schema initialized');

  // Seed demo accounts
  const demoUsers = [
    { name: 'Priya Sharma', email: 'user@kali.com', phone: '+919876543210', password: 'user123', role: 'User' },
    { name: 'Raj Sharma', email: 'guardian@kali.com', phone: '+919876543211', password: 'guardian123', role: 'Guardian' },
    { name: 'Inspector Amit Singh', email: 'admin@kali.com', phone: '+919876543212', password: 'admin123', role: 'Admin' }
  ];

  const seededIds = {};
  for (const user of demoUsers) {
    try {
      const checkRes = await pool.query('SELECT id FROM users WHERE email = $1', [user.email]);
      if (checkRes.rows.length === 0) {
        const passwordHash = await bcrypt.hash(user.password, 10);
        const insertRes = await pool.query(
          'INSERT INTO users (name, email, phone, password_hash, role) VALUES ($1, $2, $3, $4, $5) RETURNING id',
          [user.name, user.email, user.phone, passwordHash, user.role]
        );
        seededIds[user.role] = insertRes.rows[0]?.id || insertRes.insertId;
        console.log(`✅ Seeded: ${user.email} (${user.role})`);
      } else {
        seededIds[user.role] = checkRes.rows[0].id;
      }
    } catch (err) {
      console.error(`❌ Seed error ${user.email}:`, err.message);
    }
  }

  // Link Guardian to User
  if (seededIds['User'] && seededIds['Guardian']) {
    try {
      await pool.query('UPDATE users SET guardian_id = $1 WHERE id = $2', [seededIds['Guardian'], seededIds['User']]);
      console.log('✅ Guardian linked to User (demo)');
    } catch (err) { /* ignore */ }
  }

  // Seed emergency contacts
  if (seededIds['User']) {
    const contacts = [
      { name: 'Raj Sharma (Husband)', phone: '+919876543211', relation: 'Husband', primary: 1 },
      { name: 'Sunita Sharma (Mother)', phone: '+919876543213', relation: 'Mother', primary: 0 },
      { name: 'Emergency Services', phone: '112', relation: 'Emergency', primary: 0 }
    ];
    for (const c of contacts) {
      try {
        const exists = await pool.query('SELECT id FROM emergency_contacts WHERE user_id=$1 AND phone=$2', [seededIds['User'], c.phone]);
        if (exists.rows.length === 0) {
          await pool.query(
            'INSERT INTO emergency_contacts (user_id, name, phone, relation, is_primary) VALUES ($1,$2,$3,$4,$5)',
            [seededIds['User'], c.name, c.phone, c.relation, c.primary]
          );
        }
      } catch (err) { /* ignore */ }
    }
    console.log('✅ Emergency contacts seeded');
  }
}

module.exports = initDb;
