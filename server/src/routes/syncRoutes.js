import express from 'express';
import googleDriveService from '../services/googleDriveService.js';

const router = express.Router();

// Get Auth URL for initiating login
router.get('/google/auth-url', (req, res) => {
  try {
    const url = googleDriveService.getAuthUrl();
    res.json({ success: true, url });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

// OAuth Callback from Google
router.get('/google/callback', async (req, res) => {
  const { code, error } = req.query;

  if (error) {
    return res.send(`
      <!DOCTYPE html>
      <html>
      <body style="background:#0a0a0f;color:#ff5555;font-family:sans-serif;text-align:center;padding:50px;">
        <h2>Google Authentication Cancelled</h2>
        <p>${error}</p>
        <button onclick="window.close()" style="background:#8B5CF6;color:white;border:none;padding:10px 20px;border-radius:8px;cursor:pointer;">Close Window</button>
      </body>
      </html>
    `);
  }

  try {
    const user = await googleDriveService.handleCallback(code);
    return res.send(`
      <!DOCTYPE html>
      <html>
      <head>
        <title>Google Drive Connected - Aniverse</title>
        <style>
          body {
            background: #0a0a0f;
            color: #ffffff;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100vh;
            margin: 0;
            flex-direction: column;
          }
          .card {
            background: #13131a;
            border: 1px solid #26263A;
            padding: 32px 40px;
            border-radius: 16px;
            text-align: center;
            max-width: 420px;
          }
          .title { font-size: 20px; font-weight: bold; margin-bottom: 8px; color: #8B5CF6; }
          .sub { color: #888; font-size: 14px; margin-bottom: 24px; }
          .btn {
            background: #8B5CF6;
            color: white;
            border: none;
            padding: 12px 24px;
            border-radius: 10px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
          }
        </style>
      </head>
      <body>
        <div class="card">
          <div class="title">Google Drive Connected!</div>
          <div class="sub">Logged in as <b>${user?.email || 'User'}</b>.<br/>Cross-device cloud sync is now enabled.</div>
          <button class="btn" onclick="window.close()">Back to Aniverse</button>
        </div>
        <script>
          if (window.opener) {
            window.opener.postMessage({ type: 'GOOGLE_DRIVE_CONNECTED', user: ${JSON.stringify(user)} }, '*');
          }
          setTimeout(() => { window.close(); }, 2500);
        </script>
      </body>
      </html>
    `);
  } catch (err) {
    res.status(500).send(`Authentication failed: ${err.message}`);
  }
});

// Check Connection Status
router.get('/google/status', async (req, res) => {
  try {
    const status = await googleDriveService.getStatus();
    res.json({ success: true, ...status });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

// Disconnect
router.post('/google/disconnect', (req, res) => {
  try {
    googleDriveService.clearTokens();
    res.json({ success: true, message: 'Google Drive disconnected' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

// Download remote sync data
router.get('/google/download', async (req, res) => {
  try {
    const data = await googleDriveService.downloadSync();
    res.json({ success: true, data });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

// Upload local data to Google Drive
router.post('/google/upload', async (req, res) => {
  try {
    const { syncData } = req.body;
    if (!syncData) {
      return res.status(400).json({ success: false, message: 'syncData is required' });
    }
    const result = await googleDriveService.uploadSync(syncData);
    res.json(result);
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

export default router;
