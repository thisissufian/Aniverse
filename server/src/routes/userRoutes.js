import express from 'express';
import * as userController from '../controllers/userController.js';
import { protect } from '../middleware/auth.js';

const router = express.Router();

router.use(protect);

router.get('/watchlist', userController.getWatchlist);
router.post('/watchlist', userController.addToWatchlist);
router.put('/watchlist/:animeId', userController.updateWatchlistEntry);
router.delete('/watchlist/:animeId', userController.removeFromWatchlist);

router.get('/history', userController.getHistory);
router.post('/progress', userController.saveProgress);
router.get('/progress/:animeId', userController.getProgress);

export default router;
