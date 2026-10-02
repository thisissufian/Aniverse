import mongoose from 'mongoose';
import bcrypt from 'bcryptjs';

const userSchema = new mongoose.Schema({
  username: {
    type: String,
    required: true,
    unique: true,
    minLength: 3,
    maxLength: 30,
    lowercase: true,
    trim: true
  },
  email: {
    type: String,
    unique: true,
    sparse: true
  },
  password: {
    type: String,
    required: true,
    minLength: 6,
    select: false
  },
  avatar: {
    type: String,
    default: ''
  },
  mal: {
    accessToken: String,
    refreshToken: String,
    malUserId: Number,
    malUsername: String,
    tokenExpiresAt: Date,
    connected: { type: Boolean, default: false }
  },
  preferences: {
    autoSkipIntro: { type: Boolean, default: false },
    autoNextEpisode: { type: Boolean, default: true },
    defaultQuality: { type: String, default: 'auto' },
    malAutoSync: { type: Boolean, default: true }
  }
}, { timestamps: true });

userSchema.pre('save', async function (next) {
  if (!this.isModified('password')) return next();
  const salt = await bcrypt.genSalt(10);
  this.password = await bcrypt.hash(this.password, salt);
  next();
});

userSchema.methods.matchPassword = async function (enteredPassword) {
  return await bcrypt.compare(enteredPassword, this.password);
};

export default mongoose.model('User', userSchema);
