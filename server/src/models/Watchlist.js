import mongoose from 'mongoose';

const watchlistSchema = new mongoose.Schema({
  userId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  animeId: {
    type: String,
    required: true
  },
  malId: Number,
  title: {
    type: String,
    required: true
  },
  image: String,
  status: {
    type: String,
    enum: ['watching', 'plan_to_watch', 'completed', 'on_hold', 'dropped'],
    default: 'plan_to_watch'
  },
  currentEpisode: {
    type: Number,
    default: 0
  },
  totalEpisodes: Number,
  score: {
    type: Number,
    min: 0,
    max: 10
  },
  syncedToMal: {
    type: Boolean,
    default: false
  }
}, { timestamps: true });

watchlistSchema.index({ userId: 1, animeId: 1 }, { unique: true });

export default mongoose.model('Watchlist', watchlistSchema);
