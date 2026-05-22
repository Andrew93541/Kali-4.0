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

// Security
app.use(helmet({ contentSecurityPolicy: false }));

// CORS
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

// Rate limiting on auth endpoints
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100,
  message: { error: 'Too many authentication attempts. Please try again in 15 minutes.' },
  standardHeaders: true,
  legacyHeaders: false
});
app.use('/api/auth', authLimiter);

// Attach socket.io to all requests
app.use((req, res, next) => {
  req.io = io;
  next();
});

// Routes
app.use('/api/auth', require('./routes/auth'));
app.use('/api/alerts', require('./routes/alerts'));
app.use('/api/locations', require('./routes/locations'));
app.use('/api/media', require('./routes/media'));
app.use('/api/simulate', require('./routes/simulation'));
app.use('/api/contacts', require('./routes/contacts'));
app.use('/api/guardian', require('./routes/guardian'));

// Health check
app.get('/api/health', (req, res) => res.json({ status: 'ok', time: new Date().toISOString() }));

// Error handler
app.use(errorHandler);

// Socket.io — users join their personal room by userId so we can target notifications
io.on('connection', (socket) => {
  console.log('🔌 Client connected:', socket.id);

  socket.on('join', (userId) => {
    socket.join(`user_${userId}`);
    console.log(`👤 Socket ${socket.id} joined room user_${userId}`);
  });

  socket.on('disconnect', () => console.log('🔌 Client disconnected:', socket.id));
});

const PORT = process.env.PORT || 3000;
if (process.env.NODE_ENV !== 'test') {
  initDb().then(() => {
    server.listen(PORT, '0.0.0.0', () => {
      console.log(`🚀 KALI Backend running on port ${PORT}`);
      console.log(`📡 Accepting connections from all network interfaces`);
    });
  });
}

module.exports = { app, server };
