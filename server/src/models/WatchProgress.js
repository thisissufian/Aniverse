import mongoose from 'mongoose';

const watchProgressSchema = new mongoose.Schema({
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
  title: String,
  image: String,
  episodeNumber: {
    type: Number,
    required: true
  },
  timestamp: {
    type: Number,
    default: 0
  },
  duration: {
    type: Number,
    default: 0
  },
  completed: {
    type: Boolean,
    default: false
  },
  syncedToMal: {
    type: Boolean,
    default: false
  }
}, { timestamps: true });

watchProgressSchema.index({ userId: 1, animeId: 1, episodeNumber: 1 }, { unique: true });

export default mongoose.model('WatchProgress', watchProgressSchema);
