import { app, BrowserWindow, Menu } from 'electron';
import path from 'path';
import { fileURLToPath } from 'url';
import { spawn } from 'child_process';
import http from 'http';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

let mainWindow;
let serverProcess;

function waitForServer(url, timeout = 15000) {
  return new Promise((resolve, reject) => {
    const start = Date.now();
    const check = () => {
      http.get(url, (res) => {
        resolve();
      }).on('error', () => {
        if (Date.now() - start > timeout) {
          reject(new Error('Server startup timeout'));
        } else {
          setTimeout(check, 300);
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
}

app.whenReady().then(async () => {
  // Resolve paths — use unpacked dir so Node can access real files
  const rootDir = __dirname.replace('app.asar', 'app.asar.unpacked');
  const serverDir = path.join(rootDir, 'server');
  const serverPath = path.join(serverDir, 'server.js');

  // Spawn the Express server as a child process
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

  createWindow();

  // Wait for the server to actually be ready before loading
  try {
    await waitForServer('http://localhost:5000/api/anime/home');
  } catch (e) {
    console.error('Server did not start in time, loading anyway...');
  }

  mainWindow.loadURL('http://localhost:5000');
});

app.on('window-all-closed', function () {
  if (process.platform !== 'darwin') app.quit();
});

app.on('will-quit', () => {
  if (serverProcess) {
    serverProcess.kill();
  }
});
