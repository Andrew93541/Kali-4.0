require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const http = require('http');
const { Server } = require('socket.io');
const initDb = require('./config/initDb');
const errorHandler = require('./middleware/errorHandler');
const path = require('path');

const app = express();
const server = http.createServer(app);
const io = new Server(server, { cors: { origin: '*' } });

// 1. Basic Security Headers (Helmet)
app.use(helmet());

// 2. Cross-Origin Resource Sharing (CORS) with Production fallbacks
const allowedOrigins = process.env.ALLOWED_ORIGINS ? process.env.ALLOWED_ORIGINS.split(',') : ['*'];
app.use(cors({
  origin: (origin, callback) => {
    if (!origin || allowedOrigins.includes('*') || allowedOrigins.includes(origin)) {
      callback(null, true);
    } else {
      callback(new Error('Blocked by CORS policy'));
    }
  }
}));

app.use(express.json());
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));

// 3. Telemetry Rate Limiter to safeguard core REST routes
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // Limit each IP to 100 authentication requests
  message: { error: 'Too many authentication attempts. Please try again in 15 minutes.' },
  standardHeaders: true,
  legacyHeaders: false
});
app.use('/api/auth', authLimiter);

app.use((req, res, next) => {
  req.io = io;
  next();
});

// Routes Configuration
app.use('/api/auth', require('./routes/auth'));
app.use('/api/alerts', require('./routes/alerts'));
app.use('/api/locations', require('./routes/locations'));
app.use('/api/media', require('./routes/media'));
app.use('/api/simulate', require('./routes/simulation'));

// 4. Centralized Error Handling Middleware (Keep at the bottom of route registrations)
app.use(errorHandler);

io.on('connection', (socket) => {
  console.log('Client connected:', socket.id);
  socket.on('disconnect', () => console.log('Client disconnected:', socket.id));
});

const PORT = process.env.PORT || 3000;
if (process.env.NODE_ENV !== 'test') {
  initDb().then(() => {
    server.listen(PORT, () => console.log(`Server running on port ${PORT}`));
  });
}

module.exports = { app, server };

