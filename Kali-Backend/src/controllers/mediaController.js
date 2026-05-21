const pool = require('../config/db');
const crypto = require('crypto');
const fs = require('fs');

async function uploadMedia(req, res) {
  const { alert_id, media_type } = req.body;
  if (!req.file) return res.status(400).json({ error: 'No file uploaded' });
  const fileBuffer = fs.readFileSync(req.file.path);
  const fileHash = crypto.createHash('sha256').update(fileBuffer).digest('hex');
  const fileUrl = `/uploads/${req.file.filename}`;
  const result = await pool.query(
    'INSERT INTO media (alert_id, file_url, media_type, file_hash, upload_status) VALUES ($1,$2,$3,$4,$5) RETURNING *',
    [alert_id, fileUrl, media_type, fileHash, 'uploaded']
  );
  res.status(201).json({ file_url: fileUrl, file_hash: fileHash, id: result.rows[0].id });
}

async function getMedia(req, res) {
  const result = await pool.query(
    'SELECT * FROM media WHERE alert_id=$1 ORDER BY timestamp ASC',
    [req.params.alert_id]
  );
  res.json(result.rows);
}

module.exports = { uploadMedia, getMedia };
