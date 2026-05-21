const { Pool } = require('pg');
const { DatabaseSync } = require('node:sqlite');
require('dotenv').config();

let pool;
let isSqlite = false;

// Determine if we should use SQLite fallback
// If DB_DIALECT is 'sqlite', or PG credentials are left as default, or connection configuration is empty
if (
  process.env.DB_DIALECT === 'sqlite' || 
  !process.env.DB_PASSWORD || 
  process.env.DB_PASSWORD === 'your_db_password'
) {
  isSqlite = true;
}

if (isSqlite) {
  console.log('⚡ [KALI DB] PostgreSQL not configured or running. Auto-falling back to native SQLite (kali.db)');
  const db = new DatabaseSync('kali.db');

  pool = {
    async query(sql, params = []) {
      // 1. Convert PostgreSQL parameters ($1, $2, etc.) to SQLite (?)
      let sqliteSql = sql.replace(/\$\d+/g, '?');

      // 2. Translate PG specific types in DDL queries
      if (/CREATE\s+TABLE/i.test(sqliteSql)) {
        sqliteSql = sqliteSql
          .replace(/SERIAL\s+PRIMARY\s+KEY/gi, 'INTEGER PRIMARY KEY AUTOINCREMENT')
          .replace(/DOUBLE\s+PRECISION/gi, 'REAL');
      }

      // 3. Determine if it is a DDL command (like CREATE TABLE, CREATE INDEX)
      const isDDL = /^\s*(CREATE|DROP|ALTER|PRAGMA)/i.test(sqliteSql);

      try {
        if (isDDL) {
          db.exec(sqliteSql);
          return { rows: [] };
        } else {
          // Check if it's a SELECT query or has a RETURNING clause
          const isQuery = /^\s*SELECT/i.test(sqliteSql) || /RETURNING/i.test(sqliteSql);
          const stmt = db.prepare(sqliteSql);

          if (isQuery) {
            const result = stmt.all(...params);
            return { rows: result };
          } else {
            const result = stmt.run(...params);
            return { 
              rows: [], 
              insertId: result.lastInsertRowid, 
              changes: result.changes 
            };
          }
        }
      } catch (err) {
        console.error('❌ [SQLite Error] Query:', sqliteSql, 'Params:', params, err);
        throw err;
      }
    }
  };
} else {
  console.log('⚡ [KALI DB] PostgreSQL selected. Establishing connection pool...');
  pool = new Pool({
    host: process.env.DB_HOST,
    port: process.env.DB_PORT,
    database: process.env.DB_NAME,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
  });
}

module.exports = pool;
