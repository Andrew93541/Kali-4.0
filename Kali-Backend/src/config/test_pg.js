const { Client } = require('pg');
require('dotenv').config({ path: require('path').resolve(__dirname, '../../.env') });

async function testAndSetupPg() {
  console.log('🔍 [PostgreSQL Setup] Reading credentials from .env...');
  console.log(`- Host: ${process.env.DB_HOST}`);
  console.log(`- Port: ${process.env.DB_PORT}`);
  console.log(`- User: ${process.env.DB_USER}`);
  console.log(`- Database Name: ${process.env.DB_NAME}`);
  
  if (!process.env.DB_PASSWORD || process.env.DB_PASSWORD === 'your_db_password') {
    console.error('\n❌ ERROR: Please update your DB_PASSWORD in Kali-Backend/.env to your actual local PostgreSQL password first!\n');
    process.exit(1);
  }

  // 1. Connect to default 'postgres' database to check connection and create database if missing
  console.log('\nStep 1: Connecting to default "postgres" system database...');
  const client = new Client({
    host: process.env.DB_HOST,
    port: process.env.DB_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: 'postgres'
  });

  try {
    await client.connect();
    console.log('✅ Connected to PostgreSQL server successfully!');
  } catch (err) {
    console.error('❌ Failed to connect to PostgreSQL. Please ensure PostgreSQL is running and credentials are correct.');
    console.error('Error Details:', err.message);
    await client.end();
    process.exit(1);
  }

  // 2. Create the target database if it doesn't exist
  const targetDb = process.env.DB_NAME || 'kali_db';
  console.log(`\nStep 2: Checking if target database "${targetDb}" exists...`);
  try {
    const res = await client.query(`SELECT 1 FROM pg_database WHERE datname = $1`, [targetDb]);
    if (res.rows.length === 0) {
      console.log(`- Database "${targetDb}" does not exist. Creating database now...`);
      await client.query(`CREATE DATABASE "${targetDb}"`);
      console.log(`✅ Database "${targetDb}" created successfully!`);
    } else {
      console.log(`- Database "${targetDb}" already exists.`);
    }
  } catch (err) {
    console.error('❌ Error checking/creating target database:', err.message);
    await client.end();
    process.exit(1);
  }

  await client.end();
  console.log('\n🎉 [PostgreSQL Setup] Setup complete! You are now fully connected to PostgreSQL.');
  console.log('You can start/restart your server with "npm start" to initialize tables and seed demo accounts.');
}

testAndSetupPg();
