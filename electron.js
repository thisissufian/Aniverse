import { app, BrowserWindow, Menu } from 'electron';
import path from 'path';
import { fileURLToPath } from 'url';
import { spawn } from 'child_process';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

let mainWindow;
let serverProcess;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 720,
    minWidth: 900,
    minHeight: 600,
    title: "Aniverse",
    autoHideMenuBar: true, // Hides the default menu bar
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      webSecurity: false // Necessary if API is on a different origin, but they are same origin here. Useful for embeds though.
    }
  });

  Menu.setApplicationMenu(null); // Completely remove the menu bar

  // Give the Express server a moment to start, then load the URL
  // Since we run it locally, it binds quickly.
  setTimeout(() => {
    mainWindow.loadURL('http://localhost:5000');
  }, 2000);

  mainWindow.on('closed', function () {
    mainWindow = null;
  });
}

app.whenReady().then(() => {
  // Spawn the server as a child process using Electron's built-in Node runtime
  const rootDir = __dirname.replace('app.asar', 'app.asar.unpacked');
  const serverPath = path.join(rootDir, 'server', 'server.js');
  serverProcess = spawn(process.execPath, [serverPath], {
    cwd: rootDir,
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
});

app.on('window-all-closed', function () {
  if (process.platform !== 'darwin') app.quit();
});

app.on('will-quit', () => {
  // Kill the server when the app is quitting
  if (serverProcess) {
    serverProcess.kill();
  }
});
