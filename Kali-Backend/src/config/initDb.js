const pool = require('./db');
const bcrypt = require('bcryptjs');

async function initDb() {
  await pool.query(`
    CREATE TABLE IF NOT EXISTS users (
      id SERIAL PRIMARY KEY,
      name VARCHAR(100),
      email VARCHAR(100) UNIQUE NOT NULL,
      phone VARCHAR(20),
      password_hash VARCHAR(255) NOT NULL,
      role VARCHAR(20) NOT NULL DEFAULT 'User'
    );

    CREATE TABLE IF NOT EXISTS alerts (
      id SERIAL PRIMARY KEY,
      user_id INT REFERENCES users(id),
      timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      status VARCHAR(20) DEFAULT 'active'
    );

    CREATE TABLE IF NOT EXISTS locations (
      id SERIAL PRIMARY KEY,
      alert_id INT REFERENCES alerts(id),
      latitude DOUBLE PRECISION,
      longitude DOUBLE PRECISION,
      timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE IF NOT EXISTS media (
      id SERIAL PRIMARY KEY,
      alert_id INT REFERENCES alerts(id),
      file_url TEXT,
      media_type VARCHAR(10),
      file_hash VARCHAR(64),
      upload_status VARCHAR(10) DEFAULT 'uploaded',
      timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE IF NOT EXISTS audit_log (
      id SERIAL PRIMARY KEY,
      alert_id INT,
      action VARCHAR(100),
      user_id INT,
      entry_hash VARCHAR(64),
      timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    CREATE INDEX IF NOT EXISTS idx_alerts_user ON alerts(user_id);
    CREATE INDEX IF NOT EXISTS idx_locations_alert ON locations(alert_id);
    CREATE INDEX IF NOT EXISTS idx_media_alert ON media(alert_id);
  `);
  console.log('Database initialized');

  // Seed demo accounts if they don't already exist
  const demoUsers = [
    { name: 'KALI Admin', email: 'admin@kali.com', phone: '+1111111111', password: 'admin123', role: 'Admin' },
    { name: 'KALI Guardian', email: 'guardian@kali.com', phone: '+2222222222', password: 'guardian123', role: 'Guardian' },
    { name: 'KALI User', email: 'user@kali.com', phone: '+3333333333', password: 'user123', role: 'User' }
  ];

  for (const user of demoUsers) {
    try {
      const checkRes = await pool.query('SELECT id FROM users WHERE email = $1', [user.email]);
      if (checkRes.rows.length === 0) {
        const passwordHash = await bcrypt.hash(user.password, 10);
        await pool.query(
          'INSERT INTO users (name, email, phone, password_hash, role) VALUES ($1, $2, $3, $4, $5)',
          [user.name, user.email, user.phone, passwordHash, user.role]
        );
        console.log(`Demo user seeded: ${user.email} (${user.role})`);
      }
    } catch (err) {
      console.error(`Error seeding demo user ${user.email}:`, err);
    }
  }
}

module.exports = initDb;

