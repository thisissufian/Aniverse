import { app, BrowserWindow, Menu, session } from 'electron';
import path from 'path';
import { fileURLToPath } from 'url';
import { spawn } from 'child_process';
import http from 'http';
import fs from 'fs';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Support Portable Mode: if a folder named "aniverse-data" exists next to the .exe, use it!
const exeDir = path.dirname(process.execPath);
const portableDataDir = path.join(exeDir, 'aniverse-data');
if (fs.existsSync(portableDataDir)) {
  app.setPath('userData', portableDataDir);
}

let mainWindow;
let serverProcess;

const LOADING_HTML = `
<!DOCTYPE html>
<html>
<head>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body {
      background: #0a0a0f;
      color: white;
      font-family: 'Segoe UI', sans-serif;
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100vh;
      flex-direction: column;
      gap: 24px;
    }
    .title { font-size: 32px; font-weight: 700; letter-spacing: 4px; color: #8B5CF6; }
    .spinner {
      width: 40px; height: 40px;
      border: 3px solid #1a1a2e;
      border-top: 3px solid #8B5CF6;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }
    .status { color: #666; font-size: 14px; }
    @keyframes spin { to { transform: rotate(360deg); } }
  </style>
</head>
<body>
  <div class="title">ANIVERSE</div>
  <div class="spinner"></div>
  <div class="status">Starting server...</div>
</body>
</html>
`;

function waitForServer(url, timeout = 20000) {
  return new Promise((resolve, reject) => {
    const start = Date.now();
    const check = () => {
      http.get(url, (res) => {
        resolve();
      }).on('error', () => {
        if (Date.now() - start > timeout) {
          resolve(); // Load anyway after timeout
        } else {
          setTimeout(check, 500);
        }
      });
    };
    check();
  });
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 720,
    minWidth: 900,
    minHeight: 600,
    title: "Aniverse",
    autoHideMenuBar: true,
    show: false, // Don't show until ready
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      webSecurity: false
    }
  });

  Menu.setApplicationMenu(null);

  mainWindow.on('closed', function () {
    mainWindow = null;
  });

  // AD-BLOCKER: Block all popups/new windows requested by iframe ads
  mainWindow.webContents.setWindowOpenHandler((details) => {
    console.log('Blocked popup ad to:', details.url);
    return { action: 'deny' };
  });

  // AD-BLOCKER: Prevent iframe ads from redirecting the main application window
  mainWindow.webContents.on('will-navigate', (event, url) => {
    // Only allow top-level navigation to localhost (our app) and MyAnimeList (for login)
    if (!url.startsWith('http://localhost') && !url.includes('myanimelist.net/v1/oauth2/authorize')) {
      event.preventDefault();
      console.log('Blocked redirect ad to:', url);
    }
  });
}

app.whenReady().then(async () => {
  // Fix cross-origin headers so MegaPlay embeds work inside Electron
  session.defaultSession.webRequest.onBeforeSendHeaders((details, callback) => {
    const url = details.url;
    if (url.includes('megaplay.buzz') || url.includes('megacloud') || url.includes('rapid-cloud')) {
      details.requestHeaders['Referer'] = 'https://anikototv.to/';
      details.requestHeaders['Origin'] = 'https://anikototv.to';
    }
    callback({ requestHeaders: details.requestHeaders });
  });

  // Remove restrictive response headers that block iframe embedding
  session.defaultSession.webRequest.onHeadersReceived((details, callback) => {
    const headers = details.responseHeaders;
    if (headers) {
      delete headers['x-frame-options'];
      delete headers['X-Frame-Options'];
      delete headers['content-security-policy'];
      delete headers['Content-Security-Policy'];
    }
    callback({ responseHeaders: headers });
  });

  // Resolve paths
  const rootDir = __dirname.replace('app.asar', 'app.asar.unpacked');
  const serverDir = path.join(rootDir, 'server');
  const serverPath = path.join(serverDir, 'server.js');

  // Create window and show loading screen immediately
  createWindow();
  mainWindow.loadURL('data:text/html;charset=utf-8,' + encodeURIComponent(LOADING_HTML));
  mainWindow.show();

  // Spawn the Express server
  serverProcess = spawn(process.execPath, [serverPath], {
    cwd: serverDir,
    env: { 
      ...process.env, 
      PORT: '5000', 
      NODE_ENV: 'production',
      ELECTRON_RUN_AS_NODE: '1' 
    }
  });

  serverProcess.stdout.on('data', (data) => console.log(`Server: ${data}`));
  serverProcess.stderr.on('data', (data) => console.error(`Server Error: ${data}`));

  // Wait for server to be ready, then load the real app
  await waitForServer('http://localhost:5000/api/anime/home');
  
  if (mainWindow) {
    mainWindow.loadURL('http://localhost:5000');
  }
});

app.on('window-all-closed', function () {
  if (process.platform !== 'darwin') app.quit();
});

app.on('will-quit', () => {
  if (serverProcess) {
    serverProcess.kill();
  }
});
