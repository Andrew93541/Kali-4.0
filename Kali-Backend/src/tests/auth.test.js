// Force test environment
process.env.NODE_ENV = 'test';
process.env.DB_DIALECT = 'sqlite';
process.env.JWT_SECRET = 'test_jwt_secret_token_key_kali';

const request = require('supertest');
const { app, server } = require('../server');
const pool = require('../config/db');

describe('🔑 Authentication Routes - Integration Tests', () => {
  const testEmail = `testuser_${Date.now()}@example.com`;
  const testPassword = 'SecurePassword123!';
  const testName = 'Test User';
  const testPhone = '1234567890';

  beforeAll(async () => {
    // Clear user table or insert test constraints if needed
    try {
      await pool.query('DELETE FROM users WHERE email LIKE $1', ['testuser_%']);
    } catch (e) {
      console.log('Setup notice: Table might not exist yet. It will be created by server startup.');
    }
  });

  afterAll(async () => {
    // Close the HTTP server to prevent open handle hangs
    await new Promise((resolve) => server.close(resolve));
  });

  describe('📥 POST /api/auth/register', () => {
    it('should successfully register a new user', async () => {
      const res = await request(app)
        .post('/api/auth/register')
        .send({
          name: testName,
          phone: testPhone,
          email: testEmail,
          password: testPassword,
          role: 'User'
        });

      expect(res.statusCode).toEqual(201);
      expect(res.body).toHaveProperty('id');
      expect(res.body).toHaveProperty('email', testEmail);
      expect(res.body).not.toHaveProperty('password_hash');
    });

    it('should return 409 conflict when registering with an existing email', async () => {
      const res = await request(app)
        .post('/api/auth/register')
        .send({
          name: testName,
          phone: testPhone,
          email: testEmail,
          password: testPassword,
          role: 'User'
        });

      expect(res.statusCode).toEqual(409);
      expect(res.body).toHaveProperty('error', 'Email already registered');
    });
  });

  describe('🚪 POST /api/auth/login', () => {
    it('should successfully login and return a JWT token', async () => {
      const res = await request(app)
        .post('/api/auth/login')
        .send({
          email: testEmail,
          password: testPassword
        });

      expect(res.statusCode).toEqual(200);
      expect(res.body).toHaveProperty('token');
      expect(res.body).toHaveProperty('role', 'User');
      expect(res.body).toHaveProperty('name', testName);
    });

    it('should return 401 unauthorized for invalid credentials', async () => {
      const res = await request(app)
        .post('/api/auth/login')
        .send({
          email: testEmail,
          password: 'WrongPassword!'
        });

      expect(res.statusCode).toEqual(401);
      expect(res.body).toHaveProperty('error', 'Invalid credentials');
    });
  });

  describe('🌐 POST /api/auth/google', () => {
    it('should successfully login/register via Google OAuth payload', async () => {
      const googleEmail = `googleuser_${Date.now()}@example.com`;
      const res = await request(app)
        .post('/api/auth/google')
        .send({
          email: googleEmail,
          name: 'Google Test User',
          googleId: 'g_test_12345'
        });

      expect(res.statusCode).toEqual(200);
      expect(res.body).toHaveProperty('token');
      expect(res.body).toHaveProperty('role', 'User');
      expect(res.body).toHaveProperty('name', 'Google Test User');
    });
  });
});
