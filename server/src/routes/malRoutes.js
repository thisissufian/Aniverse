import express from 'express';
import { protect } from '../middleware/auth.js';
import User from '../models/User.js';
import WatchProgress from '../models/WatchProgress.js';
import Watchlist from '../models/Watchlist.js';
import {
  generateCodeVerifier,
  generateCodeChallenge,
  getAuthorizationUrl,
  exchangeCodeForTokens,
  getValidToken,
  getMalUserProfile,
  getMalAnimeList,
  updateMalAnimeStatus,
  syncProgressToMal,
} from '../services/malService.js';

const router = express.Router();

// Store code verifiers temporarily (in-memory, per user session)
const codeVerifiers = new Map();

/**
 * GET /api/mal/auth - Redirect to MAL OAuth2 authorization
 */
router.get('/auth', protect, (req, res) => {
  try {
    const codeVerifier = generateCodeVerifier();
    const codeChallenge = generateCodeChallenge(codeVerifier);
    const state = req.user._id.toString();

    // Store verifier for this user (expires in 10 min)
    codeVerifiers.set(state, { verifier: codeVerifier, timestamp: Date.now() });
    setTimeout(() => codeVerifiers.delete(state), 10 * 60 * 1000);

    const authUrl = getAuthorizationUrl(codeChallenge, state);
    res.json({ success: true, url: authUrl });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Failed to generate auth URL' });
  }
});

/**
 * GET /api/mal/callback - OAuth2 callback from MAL
 */
router.get('/callback', async (req, res) => {
  const { code, state } = req.query;

  if (!code || !state) {
    return res.redirect('http://localhost:5173/profile?mal=error&reason=missing_params');
  }

  const stored = codeVerifiers.get(state);
  if (!stored) {
    return res.redirect('http://localhost:5173/profile?mal=error&reason=expired_session');
  }

  try {
    const tokens = await exchangeCodeForTokens(code, stored.verifier);
    codeVerifiers.delete(state);

    // Get MAL user profile
    const malProfile = await getMalUserProfile(tokens.access_token);

    // Update user with MAL tokens
    await User.findByIdAndUpdate(state, {
      'mal.accessToken': tokens.access_token,
      'mal.refreshToken': tokens.refresh_token,
      'mal.tokenExpiresAt': new Date(Date.now() + tokens.expires_in * 1000),
      'mal.malUserId': malProfile.id,
      'mal.malUsername': malProfile.name,
      'mal.connected': true,
    });

    res.redirect('http://localhost:5173/profile?mal=success');
  } catch (error) {
    console.error('MAL callback error:', error.message);
    res.redirect('http://localhost:5173/profile?mal=error&reason=token_exchange_failed');
  }
});

/**
 * GET /api/mal/status - Check MAL connection status
 */
router.get('/status', protect, (req, res) => {
  const mal = req.user.mal || {};
  res.json({
    success: true,
    connected: mal.connected || false,
    malUsername: mal.malUsername || null,
    malUserId: mal.malUserId || null,
  });
});

/**
 * POST /api/mal/disconnect - Remove MAL connection
 */
router.post('/disconnect', protect, async (req, res) => {
  try {
    await User.findByIdAndUpdate(req.user._id, {
      'mal.accessToken': null,
      'mal.refreshToken': null,
      'mal.tokenExpiresAt': null,
      'mal.malUserId': null,
      'mal.malUsername': null,
      'mal.connected': false,
    });

    res.json({ success: true, message: 'MAL disconnected successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Failed to disconnect MAL' });
  }
});

/**
 * GET /api/mal/list - Get user's MAL anime list
 */
router.get('/list', protect, async (req, res) => {
  try {
    const user = await User.findById(req.user._id);
    const accessToken = await getValidToken(user);
    const { status, limit = 100, offset = 0 } = req.query;

    const list = await getMalAnimeList(accessToken, status, parseInt(limit), parseInt(offset));
    res.json({ success: true, data: list });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * POST /api/mal/sync/:animeId - Sync single anime to MAL
 */
router.post('/sync/:animeId', protect, async (req, res) => {
  try {
    const user = await User.findById(req.user._id);
    const { malId, episodeNumber, status, score, totalEpisodes } = req.body;

    if (!malId) {
      return res.status(400).json({ success: false, message: 'MAL ID required for sync' });
    }

    const result = await syncProgressToMal(user, {
      malId,
      episodeNumber,
      status,
      score,
      totalEpisodes,
      completed: episodeNumber >= totalEpisodes,
    });

    if (result) {
      // Mark local entries as synced
      await WatchProgress.updateMany(
        { userId: req.user._id, animeId: req.params.animeId },
        { syncedToMal: true }
      );
      await Watchlist.updateOne(
        { userId: req.user._id, animeId: req.params.animeId },
        { syncedToMal: true }
      );
    }

    res.json({ success: true, data: result });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * POST /api/mal/sync-all - Bulk sync all unsynced progress to MAL
 */
router.post('/sync-all', protect, async (req, res) => {
  try {
    const user = await User.findById(req.user._id);
    if (!user.mal?.connected) {
      return res.status(400).json({ success: false, message: 'MAL not connected' });
    }

    const unsyncedItems = await Watchlist.find({
      userId: req.user._id,
      malId: { $ne: null },
      syncedToMal: false,
    });

    let synced = 0;
    let failed = 0;

    for (const item of unsyncedItems) {
      try {
        const latestProgress = await WatchProgress.findOne({
          userId: req.user._id,
          animeId: item.animeId,
        }).sort({ episodeNumber: -1 });

        await syncProgressToMal(user, {
          malId: item.malId,
          episodeNumber: latestProgress?.episodeNumber || item.currentEpisode || 0,
          status: item.status,
          score: item.score,
          totalEpisodes: item.totalEpisodes,
          completed: item.status === 'completed',
        });

        item.syncedToMal = true;
        await item.save();
        synced++;
      } catch {
        failed++;
      }
    }

    res.json({
      success: true,
      message: `Synced ${synced} items, ${failed} failed`,
      synced,
      failed,
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

export default router;
