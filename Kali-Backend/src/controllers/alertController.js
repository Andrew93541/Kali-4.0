const pool = require('../config/db');
const { logAudit } = require('../utils/audit');

async function createAlert(req, res) {
  const { latitude, longitude } = req.body;
  const userId = req.user.id;
  const result = await pool.query(
    'INSERT INTO alerts (user_id) VALUES ($1) RETURNING *',
    [userId]
  );
  const alert = result.rows[0];
  if (latitude && longitude) {
    await pool.query(
      'INSERT INTO locations (alert_id, latitude, longitude) VALUES ($1,$2,$3)',
      [alert.id, latitude, longitude]
    );
  }
  await logAudit(alert.id, 'alert_created', userId);
  req.io.emit('newAlert', { alertId: alert.id, userId, latitude, longitude, timestamp: alert.timestamp });
  res.status(201).json({ alert_id: alert.id, status: alert.status });
}

async function getAlert(req, res) {
  const result = await pool.query(
    `SELECT a.*, u.name, u.phone FROM alerts a JOIN users u ON a.user_id=u.id WHERE a.id=$1`,
    [req.params.id]
  );
  if (!result.rows[0]) return res.status(404).json({ error: 'Not found' });
  res.json(result.rows[0]);
}

async function listAlerts(req, res) {
  const { user_id } = req.query;
  let query = `SELECT a.*, u.name FROM alerts a JOIN users u ON a.user_id=u.id`;
  const params = [];
  if (user_id && req.user.role !== 'Admin') {
    query += ' WHERE a.user_id=$1';
    params.push(user_id);
  }
  query += ' ORDER BY a.timestamp DESC';
  const result = await pool.query(query, params);
  res.json(result.rows);
}

async function resolveAlert(req, res) {
  await pool.query('UPDATE alerts SET status=$1 WHERE id=$2', ['resolved', req.params.id]);
  await logAudit(req.params.id, 'alert_resolved', req.user.id);
  req.io.emit('alertResolved', { alertId: req.params.id });
  res.json({ status: 'resolved' });
}

async function assignOfficer(req, res) {
  const { officer_id } = req.body;
  await logAudit(req.params.id, `assigned_officer_${officer_id}`, req.user.id);
  req.io.emit('officerAssigned', { alertId: req.params.id, officerId: officer_id });
  res.json({ status: 'assigned', officer_id });
}

module.exports = { createAlert, getAlert, listAlerts, resolveAlert, assignOfficer };
