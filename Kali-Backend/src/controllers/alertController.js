const pool = require('../config/db');
const { logAudit } = require('../utils/audit');

async function createAlert(req, res) {
  const { latitude, longitude } = req.body;
  const userId = req.user.id;

  const result = await pool.query(
    'INSERT INTO alerts (user_id) VALUES ($1) RETURNING *',
    [userId]
  );
  // Handle SQLite (insertId) vs pg (rows[0])
  const alertId = result.rows[0]?.id || result.insertId;
  const alert = result.rows[0] || { id: alertId, user_id: userId, status: 'active', timestamp: new Date().toISOString() };

  if (latitude && longitude) {
    await pool.query(
      'INSERT INTO locations (alert_id, latitude, longitude) VALUES ($1,$2,$3)',
      [alertId, latitude, longitude]
    );
  }

  await logAudit(alertId, 'alert_created', userId);

  // Get user info for broadcast
  const userRes = await pool.query('SELECT name, phone, guardian_id FROM users WHERE id=$1', [userId]);
  const user = userRes.rows[0] || {};

  const payload = {
    alertId,
    userId,
    userName: user.name || 'Unknown',
    userPhone: user.phone || null,
    guardianId: user.guardian_id || null,
    latitude,
    longitude,
    timestamp: alert.timestamp
  };

  req.io.emit('newAlert', payload);
  // Notify guardian's room specifically
  if (user.guardian_id) req.io.to(`user_${user.guardian_id}`).emit('newAlert', payload);

  res.status(201).json({ alert_id: alertId, status: alert.status });
}

async function getAlert(req, res) {
  const result = await pool.query(
    `SELECT a.*, u.name, u.phone, u.guardian_id FROM alerts a JOIN users u ON a.user_id=u.id WHERE a.id=$1`,
    [req.params.id]
  );
  if (!result.rows[0]) return res.status(404).json({ error: 'Not found' });
  res.json(result.rows[0]);
}

async function listAlerts(req, res) {
  const { user_id, status } = req.query;
  const role = req.user.role;

  let query = `SELECT a.*, u.name, u.phone FROM alerts a JOIN users u ON a.user_id=u.id`;
  const params = [];
  const where = [];

  if (role === 'User') {
    // Users only see their own alerts
    where.push(`a.user_id=$${params.length + 1}`);
    params.push(req.user.id);
  } else if (role === 'Guardian') {
    // Guardians see alerts from their linked user(s)
    where.push(`u.guardian_id=$${params.length + 1}`);
    params.push(req.user.id);
  }
  // Admin sees all

  if (status) {
    where.push(`a.status=$${params.length + 1}`);
    params.push(status);
  }

  if (where.length > 0) query += ' WHERE ' + where.join(' AND ');
  query += ' ORDER BY a.timestamp DESC';

  const result = await pool.query(query, params);
  res.json(result.rows);
}

async function getMyAlerts(req, res) {
  const result = await pool.query(
    `SELECT a.*, u.name FROM alerts a JOIN users u ON a.user_id=u.id WHERE a.user_id=$1 ORDER BY a.timestamp DESC`,
    [req.user.id]
  );
  res.json(result.rows);
}

async function resolveAlert(req, res) {
  const { note } = req.body;
  await pool.query(
    'UPDATE alerts SET status=$1, resolved_at=CURRENT_TIMESTAMP, resolve_note=$2 WHERE id=$3',
    ['resolved', note || null, req.params.id]
  );
  await logAudit(req.params.id, 'alert_resolved', req.user.id);
  req.io.emit('alertResolved', { alertId: req.params.id, resolvedBy: req.user.id });
  res.json({ status: 'resolved' });
}

async function assignOfficer(req, res) {
  const { officer_id } = req.body;
  await logAudit(req.params.id, `assigned_officer_${officer_id}`, req.user.id);
  req.io.emit('officerAssigned', { alertId: req.params.id, officerId: officer_id });
  res.json({ status: 'assigned', officer_id });
}

module.exports = { createAlert, getAlert, listAlerts, getMyAlerts, resolveAlert, assignOfficer };
