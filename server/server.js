import 'dotenv/config'; // Must be first to load env vars before other imports

import app from './src/app.js';
import connectDB from './src/config/db.js';
import { PORT } from './src/config/env.js';

const startServer = async () => {
  // Start Express immediately so frontend doesn't get ECONNREFUSED
  app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
  });

  // Try connecting to DB in background
  try {
    await connectDB();
  } catch (error) {
    console.error('Failed to start MongoDB:', error);
    // don't exit process, let the app run (though DB features will fail)
  }
};

startServer();
