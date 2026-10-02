import Watchlist from '../models/Watchlist.js';
import WatchProgress from '../models/WatchProgress.js';

export const getWatchlist = async (req, res, next) => {
  try {
    const { status } = req.query;
    const filter = { userId: req.user._id };
    if (status) filter.status = status;

    const watchlist = await Watchlist.find(filter).sort({ updatedAt: -1 });
    res.json({ success: true, data: watchlist });
  } catch (error) {
    next(error);
  }
};

export const addToWatchlist = async (req, res, next) => {
  try {
    const { animeId, malId, title, image, status, currentEpisode, totalEpisodes, score } = req.body;
    
    const entry = await Watchlist.findOneAndUpdate(
      { userId: req.user._id, animeId },
      { malId, title, image, status, currentEpisode, totalEpisodes, score },
      { new: true, upsert: true }
    );
    
    res.json({ success: true, data: entry });
  } catch (error) {
    next(error);
  }
};

export const updateWatchlistEntry = async (req, res, next) => {
  try {
    const { animeId } = req.params;
    const { status, score, currentEpisode } = req.body;
    
    const entry = await Watchlist.findOneAndUpdate(
      { userId: req.user._id, animeId },
      { status, score, currentEpisode },
      { new: true }
    );
    
    if (!entry) return res.status(404).json({ success: false, message: 'Not found in watchlist' });
    res.json({ success: true, data: entry });
  } catch (error) {
    next(error);
  }
};

export const removeFromWatchlist = async (req, res, next) => {
  try {
    const { animeId } = req.params;
    await Watchlist.findOneAndDelete({ userId: req.user._id, animeId });
    res.json({ success: true, message: 'Removed from watchlist' });
  } catch (error) {
    next(error);
  }
};

export const getHistory = async (req, res, next) => {
  try {
    const history = await WatchProgress.find({ userId: req.user._id })
      .sort({ updatedAt: -1 })
      .limit(50);
    res.json({ success: true, data: history });
  } catch (error) {
    next(error);
  }
};

export const saveProgress = async (req, res, next) => {
  try {
    const { animeId, malId, title, image, episodeNumber, timestamp, duration } = req.body;
    
    let completed = false;
    if (duration > 0 && (timestamp / duration) > 0.9) {
      completed = true;
    }

    const progress = await WatchProgress.findOneAndUpdate(
      { userId: req.user._id, animeId, episodeNumber },
      { malId, title, image, timestamp, duration, completed },
      { new: true, upsert: true }
    );

    res.json({ success: true, data: progress });
  } catch (error) {
    next(error);
  }
};

export const getProgress = async (req, res, next) => {
  try {
    const { animeId } = req.params;
    const progress = await WatchProgress.find({ userId: req.user._id, animeId });
    res.json({ success: true, data: progress });
  } catch (error) {
    next(error);
  }
};
