import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import errorHandler from './middleware/errorHandler.js';

import authRoutes from './routes/authRoutes.js';
import animeRoutes from './routes/animeRoutes.js';
import streamRoutes from './routes/streamRoutes.js';
import userRoutes from './routes/userRoutes.js';
import malRoutes from './routes/malRoutes.js';
import syncRoutes from './routes/syncRoutes.js';

const app = express();

// Security middleware
app.use(helmet({
  contentSecurityPolicy: false,
  crossOriginEmbedderPolicy: false
}));
app.use(cors({
  origin: true, // Allow all origins for dev
  credentials: true
}));
app.use(express.json());

// Rate limiters
const generalLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 1000, // Limit each IP to 1000 requests per windowMs
  message: { success: false, message: 'Too many requests, please try again later.' }
});

const streamLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 500, // More lenient for streaming/segments
  message: { success: false, message: 'Too many stream requests, please try again later.' }
});

import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Routes
app.use('/api/auth', generalLimiter, authRoutes);
app.use('/api/anime', generalLimiter, animeRoutes);
app.use('/api/stream', streamLimiter, streamRoutes);
app.use('/api/user', generalLimiter, userRoutes);
app.use('/api/mal', generalLimiter, malRoutes);
app.use('/api/sync', generalLimiter, syncRoutes);

// Serve static frontend in production/electron
import fs from 'fs';

// Try multiple possible locations for client/dist
const possiblePaths = [
  path.join(__dirname, '../../client/dist'),           // dev: server/src/ -> ../../client/dist
  path.join(process.cwd(), '../client/dist'),          // electron: server/ -> ../client/dist
  path.join(process.cwd(), 'client/dist'),             // if CWD is root
];

let clientDistPath = possiblePaths.find(p => fs.existsSync(path.join(p, 'index.html')));

if (clientDistPath) {
  app.use(express.static(clientDistPath));
  app.get('*', (req, res) => {
    res.sendFile(path.join(clientDistPath, 'index.html'));
  });
} else {
  console.warn('Could not find client/dist. Tried:', possiblePaths);
}

// Global Error Handler
app.use(errorHandler);

export default app;
