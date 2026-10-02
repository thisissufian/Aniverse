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

const app = express();

// Security middleware
app.use(helmet());
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

// Routes
app.use('/api/auth', generalLimiter, authRoutes);
app.use('/api/anime', generalLimiter, animeRoutes);
app.use('/api/stream', streamLimiter, streamRoutes);
app.use('/api/user', generalLimiter, userRoutes);
app.use('/api/mal', generalLimiter, malRoutes);

// Global Error Handler
app.use(errorHandler);

export default app;
