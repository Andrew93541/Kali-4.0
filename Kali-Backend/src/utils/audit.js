const crypto = require('crypto');
const pool = require('../config/db');

async function logAudit(alertId, action, userId) {
  const raw = `${alertId}|${action}|${userId}|${Date.now()}`;
  const hash = crypto.createHash('sha256').update(raw).digest('hex');
  await pool.query(
    'INSERT INTO audit_log (alert_id, action, user_id, entry_hash) VALUES ($1,$2,$3,$4)',
    [alertId, action, userId, hash]
  );
}

module.exports = { logAudit };
