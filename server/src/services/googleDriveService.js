import axios from 'axios';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Client Credentials for Desktop
const CLIENT_ID = process.env.GOOGLE_CLIENT_ID || '';
const CLIENT_SECRET = process.env.GOOGLE_CLIENT_SECRET || '';
const REDIRECT_URI = 'http://localhost:5000/api/sync/google/callback';
const SCOPE = 'https://www.googleapis.com/auth/drive.appdata email profile';

// Path for storing tokens safely
const DATA_DIR = path.join(__dirname, '../../../aniverse-data');
if (!fs.existsSync(DATA_DIR)) {
  try {
    fs.mkdirSync(DATA_DIR, { recursive: true });
  } catch (e) {
    // fallback if non-writable
  }
}
const TOKEN_FILE = path.join(DATA_DIR, 'google_token.json');

class GoogleDriveService {
  constructor() {
    this.tokens = this.loadTokens();
  }

  loadTokens() {
    try {
      if (fs.existsSync(TOKEN_FILE)) {
        return JSON.parse(fs.readFileSync(TOKEN_FILE, 'utf8'));
      }
    } catch (e) {
      console.error('Failed to load Google Drive token', e);
    }
    return null;
  }

  saveTokens(tokens) {
    this.tokens = { ...this.tokens, ...tokens, updatedAt: Date.now() };
    try {
      fs.writeFileSync(TOKEN_FILE, JSON.stringify(this.tokens, null, 2), 'utf8');
    } catch (e) {
      console.error('Failed to save Google Drive token', e);
    }
  }

  clearTokens() {
    this.tokens = null;
    try {
      if (fs.existsSync(TOKEN_FILE)) {
        fs.unlinkSync(TOKEN_FILE);
      }
    } catch (e) {
      console.error('Failed to remove token file', e);
    }
  }

  getAuthUrl() {
    const params = new URLSearchParams({
      client_id: CLIENT_ID,
      redirect_uri: REDIRECT_URI,
      response_type: 'code',
      scope: SCOPE,
      access_type: 'offline',
      prompt: 'consent'
    });
    return `https://accounts.google.com/o/oauth2/v2/auth?${params.toString()}`;
  }

  async handleCallback(code) {
    const response = await axios.post('https://oauth2.googleapis.com/token', {
      code,
      client_id: CLIENT_ID,
      client_secret: CLIENT_SECRET,
      redirect_uri: REDIRECT_URI,
      grant_type: 'authorization_code'
    });

    const { access_token, refresh_token, expires_in } = response.data;
    const expiryTime = Date.now() + (expires_in * 1000) - 60000;

    // Fetch user info
    let userInfo = null;
    try {
      const userRes = await axios.get('https://www.googleapis.com/oauth2/v2/userinfo', {
        headers: { Authorization: `Bearer ${access_token}` }
      });
      userInfo = userRes.data;
    } catch (e) {
      console.error('Failed to fetch user info', e);
    }

    this.saveTokens({
      access_token,
      refresh_token: refresh_token || this.tokens?.refresh_token,
      expiryTime,
      user: userInfo
    });

    return userInfo;
  }

  async getValidAccessToken() {
    if (!this.tokens) return null;

    if (Date.now() < this.tokens.expiryTime && this.tokens.access_token) {
      return this.tokens.access_token;
    }

    if (!this.tokens.refresh_token) {
      return null;
    }

    try {
      const response = await axios.post('https://oauth2.googleapis.com/token', {
        client_id: CLIENT_ID,
        client_secret: CLIENT_SECRET,
        refresh_token: this.tokens.refresh_token,
        grant_type: 'refresh_token'
      });

      const { access_token, expires_in } = response.data;
      const expiryTime = Date.now() + (expires_in * 1000) - 60000;
      this.saveTokens({ access_token, expiryTime });
      return access_token;
    } catch (e) {
      console.error('Failed to refresh Google access token', e);
      return null;
    }
  }

  async getStatus() {
    const token = await this.getValidAccessToken();
    return {
      connected: !!token,
      user: this.tokens?.user || null,
      lastSynced: this.tokens?.lastSynced || null
    };
  }

  async findSyncFileId(accessToken) {
    const res = await axios.get('https://www.googleapis.com/drive/v3/files', {
      headers: { Authorization: `Bearer ${accessToken}` },
      params: {
        spaces: 'appDataFolder',
        q: "name = 'aniverse_sync.json' and trashed = false",
        fields: 'files(id, name, modifiedTime)'
      }
    });
    const files = res.data.files || [];
    return files.length > 0 ? files[0].id : null;
  }

  async downloadSync() {
    const token = await this.getValidAccessToken();
    if (!token) throw new Error('Not connected to Google Drive');

    const fileId = await this.findSyncFileId(token);
    if (!fileId) return null;

    const res = await axios.get(`https://www.googleapis.com/drive/v3/files/${fileId}?alt=media`, {
      headers: { Authorization: `Bearer ${token}` }
    });

    this.saveTokens({ lastSynced: Date.now() });
    return res.data;
  }

  async uploadSync(syncData) {
    const token = await this.getValidAccessToken();
    if (!token) throw new Error('Not connected to Google Drive');

    const fileId = await this.findSyncFileId(token);
    const boundary = '-------314159265358979323846';
    const delimiter = `\r\n--${boundary}\r\n`;
    const closeDelimiter = `\r\n--${boundary}--`;

    const metadata = {
      name: 'aniverse_sync.json',
      mimeType: 'application/json',
      ...(!fileId ? { parents: ['appDataFolder'] } : {})
    };

    const multipartRequestBody =
      delimiter +
      'Content-Type: application/json; charset=UTF-8\r\n\r\n' +
      JSON.stringify(metadata) +
      delimiter +
      'Content-Type: application/json\r\n\r\n' +
      JSON.stringify(syncData, null, 2) +
      closeDelimiter;

    const url = fileId
      ? `https://www.googleapis.com/upload/drive/v3/files/${fileId}?uploadType=multipart`
      : 'https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart';

    const method = fileId ? 'PATCH' : 'POST';

    await axios({
      method,
      url,
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': `multipart/related; boundary=${boundary}`
      },
      data: multipartRequestBody
    });

    this.saveTokens({ lastSynced: Date.now() });
    return { success: true, lastSynced: Date.now() };
  }
}

export default new GoogleDriveService();
