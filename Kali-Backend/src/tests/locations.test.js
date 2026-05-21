// Force test environment
process.env.NODE_ENV = 'test';
process.env.DB_DIALECT = 'sqlite';
process.env.JWT_SECRET = 'test_jwt_secret_token_key_kali';

const request = require('supertest');
const { app, server } = require('../server');
const pool = require('../config/db');

describe('📍 Location & Simulation Routes - Integration Tests', () => {
  let authToken;
  let testUserId;
  let testAlertId;

  beforeAll(async () => {
    // Register and login a test user for authenticated requests
    const regEmail = `loctest_${Date.now()}@example.com`;
    const regRes = await request(app)
      .post('/api/auth/register')
      .send({ name: 'LocTest User', email: regEmail, phone: '5555555555', password: 'Test1234!', role: 'User' });

    testUserId = regRes.body.id;

    const loginRes = await request(app)
      .post('/api/auth/login')
      .send({ email: regEmail, password: 'Test1234!' });

    authToken = `Bearer ${loginRes.body.token}`;

    // Create an alert for location tests
    const alertRes = await request(app)
      .post('/api/alerts')
      .set('Authorization', authToken)
      .send({ latitude: 28.6139, longitude: 77.2090 });

    testAlertId = alertRes.body.alert_id;
  });

  afterAll(async () => {
    await new Promise((resolve) => server.close(resolve));
  });

  describe('📌 POST /api/locations', () => {
    it('should add a single location point for an alert', async () => {
      const res = await request(app)
        .post('/api/locations')
        .set('Authorization', authToken)
        .send({ alert_id: testAlertId, latitude: 28.6145, longitude: 77.2095 });

      expect(res.statusCode).toEqual(201);
      expect(res.body).toHaveProperty('latitude');
      expect(res.body).toHaveProperty('longitude');
      expect(res.body).toHaveProperty('alert_id');
    });

    it('should reject location without required fields', async () => {
      const res = await request(app)
        .post('/api/locations')
        .set('Authorization', authToken)
        .send({ alert_id: testAlertId });

      expect(res.statusCode).toEqual(400);
      expect(res.body).toHaveProperty('error');
    });

    it('should require authentication', async () => {
      const res = await request(app)
        .post('/api/locations')
        .send({ alert_id: testAlertId, latitude: 28.6145, longitude: 77.2095 });

      expect(res.statusCode).toEqual(401);
    });
  });

  describe('📦 POST /api/locations/batch', () => {
    it('should accept a batch of location points', async () => {
      const points = [
        { latitude: 28.6150, longitude: 77.2100 },
        { latitude: 28.6155, longitude: 77.2105 },
        { latitude: 28.6160, longitude: 77.2110 }
      ];

      const res = await request(app)
        .post('/api/locations/batch')
        .set('Authorization', authToken)
        .send({ alert_id: testAlertId, points });

      expect(res.statusCode).toEqual(201);
      expect(res.body).toHaveProperty('count', 3);
      expect(res.body.points).toHaveLength(3);
    });

    it('should reject batch without points array', async () => {
      const res = await request(app)
        .post('/api/locations/batch')
        .set('Authorization', authToken)
        .send({ alert_id: testAlertId });

      expect(res.statusCode).toEqual(400);
    });
  });

  describe('📊 GET /api/locations/:alert_id', () => {
    it('should return all locations for an alert in chronological order', async () => {
      const res = await request(app)
        .get(`/api/locations/${testAlertId}`)
        .set('Authorization', authToken);

      expect(res.statusCode).toEqual(200);
      expect(Array.isArray(res.body)).toBe(true);
      // We inserted 1 initial + 1 single + 3 batch = 5 points
      expect(res.body.length).toBeGreaterThanOrEqual(4);
    });
  });

  describe('📈 GET /api/locations/:alert_id/stats', () => {
    it('should return computed telemetry statistics', async () => {
      const res = await request(app)
        .get(`/api/locations/${testAlertId}/stats`)
        .set('Authorization', authToken);

      expect(res.statusCode).toEqual(200);
      expect(res.body).toHaveProperty('totalDistance');
      expect(res.body).toHaveProperty('avgSpeed');
      expect(res.body).toHaveProperty('maxSpeed');
      expect(res.body).toHaveProperty('duration');
      expect(res.body).toHaveProperty('pointCount');
      expect(res.body).toHaveProperty('lastBearing');
      expect(res.body.pointCount).toBeGreaterThanOrEqual(4);
      expect(typeof res.body.totalDistance).toBe('number');
    });

    it('should handle alert with no locations gracefully', async () => {
      // Create a new alert with no additional locations
      const alertRes = await request(app)
        .post('/api/alerts')
        .set('Authorization', authToken)
        .send({});

      const emptyAlertId = alertRes.body.alert_id;

      const res = await request(app)
        .get(`/api/locations/${emptyAlertId}/stats`)
        .set('Authorization', authToken);

      expect(res.statusCode).toEqual(200);
      expect(res.body.totalDistance).toBe(0);
      expect(res.body.avgSpeed).toBe(0);
    });
  });

  describe('🎮 GET /api/simulate/status', () => {
    it('should return simulation status', async () => {
      const res = await request(app)
        .get('/api/simulate/status')
        .set('Authorization', authToken);

      expect(res.statusCode).toEqual(200);
      expect(res.body).toHaveProperty('active');
      expect(res.body).toHaveProperty('simulations');
      expect(Array.isArray(res.body.simulations)).toBe(true);
    });
  });

  describe('🚀 POST /api/simulate/start & POST /api/simulate/stop', () => {
    it('should start and then stop a simulation for an alert', async () => {
      const startRes = await request(app)
        .post('/api/simulate/start')
        .set('Authorization', authToken)
        .send({ alert_id: testAlertId, pattern: 'walk', startLat: 28.6139, startLng: 77.2090 });

      expect(startRes.statusCode).toEqual(200);
      expect(startRes.body).toHaveProperty('status', 'started');
      expect(startRes.body).toHaveProperty('pattern', 'Walking');

      // Stop it immediately
      const stopRes = await request(app)
        .post('/api/simulate/stop')
        .set('Authorization', authToken)
        .send({ alert_id: testAlertId });

      expect(stopRes.statusCode).toEqual(200);
      expect(stopRes.body).toHaveProperty('status', 'stopped');
    });

    it('should reject starting a duplicate simulation', async () => {
      // Start first
      await request(app)
        .post('/api/simulate/start')
        .set('Authorization', authToken)
        .send({ alert_id: testAlertId, pattern: 'run' });

      // Try to start again
      const dupRes = await request(app)
        .post('/api/simulate/start')
        .set('Authorization', authToken)
        .send({ alert_id: testAlertId, pattern: 'vehicle' });

      expect(dupRes.statusCode).toEqual(409);

      // Cleanup
      await request(app)
        .post('/api/simulate/stop')
        .set('Authorization', authToken)
        .send({ alert_id: testAlertId });
    });
  });
});
