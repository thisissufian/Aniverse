import mongoose from 'mongoose';
import { MONGODB_URI } from './env.js';
import dns from 'node:dns';

// Fix for Windows DNS resolution of MongoDB SRV records
dns.setServers(['8.8.8.8', '8.8.4.4']);

const connectDB = async (retries = 5, delay = 5000) => {
  while (retries > 0) {
    try {
      mongoose.set('strictQuery', false);
      const conn = await mongoose.connect(MONGODB_URI);
      console.log(`MongoDB Connected: ${conn.connection.host}`);
      return;
    } catch (error) {
      console.error(`Error connecting to MongoDB: ${error.message}`);
      retries -= 1;
      console.log(`Retries left: ${retries}`);
      if (retries === 0) {
        throw new Error('Could not connect to MongoDB after multiple attempts');
      }
      await new Promise(res => setTimeout(res, delay));
    }
  }
};

mongoose.connection.on('disconnected', () => {
  console.log('MongoDB disconnected');
});

mongoose.connection.on('error', (err) => {
  console.error(`MongoDB connection error: ${err}`);
});

export default connectDB;
