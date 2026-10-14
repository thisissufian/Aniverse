## 🌌 Aniverse v1.1.0 — Native Android Launch, Floating Mini-Player & Google Drive Sync

We are thrilled to release **Aniverse v1.1.0**, featuring our brand-new standalone **Native Android Mobile App**, **YouTube-Style Floating Mini-Player**, dedicated **Watch History Navbar**, and seamless **Cross-Device Google Drive Cloud Sync**!

---

### 📦 Download Android APKs (v1.1.0)

| Architecture | Package | Description | Exact Size |
| :--- | :--- | :--- | :--- |
| **ARM64 (64-bit)** | **[`Aniverse-v1.1.0-arm64.apk`](https://github.com/thisissufian/Aniverse/releases/download/v1.1.0/Aniverse-v1.1.0-arm64.apk)** | **Recommended** for modern Android phones and tablets | **4.00 MB** (4,190,635 bytes) |
| **ARMv7 (32-bit)** | **[`Aniverse-v1.1.0-armv7.apk`](https://github.com/thisissufian/Aniverse/releases/download/v1.1.0/Aniverse-v1.1.0-armv7.apk)** | For older 32-bit devices and legacy emulators | **4.00 MB** (4,190,635 bytes) |

> 💡 **Android Installation Note**: Since Aniverse is an open-source project without a commercial signing certificate, Android may show an *"Install from unknown sources"* or *"Unrecognized app"* prompt. Tap **Settings** $\rightarrow$ **Allow from this source** $\rightarrow$ **Install anyway**.

---

### 🌟 What's New in v1.1.0

#### 📱 1. Standalone Native Android Mobile App
- **Ultralight 4.00 MB APK**: Engineered with **Native Kotlin**, **Jetpack Compose (BOM 2024.06.00)**, and **Media3 ExoPlayer**. Full R8 ProGuard code & resource minification.
- **Floating Mini-Player with Transparent Controls**:
  - Video **continues playing live** in a floating 16:9 window in the bottom-right corner while you browse Home, Search, History, Watchlist, or Profile.
  - **Center Expand Button**: 4-corner viewfinder icon to return directly to the player page.
  - **Top-Right Close Cross (`X`)**: Instantly closes and stops playback.
  - **Bottom Controls**: Previous episode $\vert\blacktriangleleft\blacktriangleleft$, Play/Pause $\blacktriangleright/\vert\vert$, Next episode $\blacktriangleright\blacktriangleright\vert$.
  - **Bottom Red Progress Bar**: Real-time progress indicator.
  - Powered by Jetpack Compose `movableContentOf` with zero reloads, zero buffering, and zero stutter.
- **Streamlined Player View**:
  - Removed top minimize and PiP overlay icons for an unobstructed viewing experience.
  - Auto-minimizes when pressing Android system Back.
  - Tap the in-player mini-player button on the video player itself to minimize.
  - Tap the floating mini-player anytime to expand smoothly back into full view.
- **Dedicated Watch History Tab in Bottom Navbar**:
  - 5-item navigation bar: **Home**, **Search**, **History**, **Watchlist**, **Profile**.
  - YouTube-style watch history with exact timestamps (`14:25 / 24:00`), completion badges, red progress bars, and one-tap resume.
- **YouTube-Smooth 120Hz Gesture Controls**:
  - **Double-Tap Seek**: Rewind 10s on the left, forward 10s on the right with animated HUD pills.
  - **Vertical Edge Swiping**: Left side adjusts screen brightness; right side adjusts media volume.
  - **Orientation Lock**: Lock landscape mode with on-screen toggle.
- **Picture-in-Picture (PiP)**: Keep watching in floating OS mini-player when exiting the app.
- **Auto-Skip Intro & Outro**: Automatic detection and skip button for opening and ending songs.
- **Auto-Play Next Episode**: Seamless binge-watching automatically advancing to the next episode when playback ends.
- **Local SQLite Persistence**: Full offline tracking of exact second timestamps and customizable watchlists.
- **Custom Pirate App Icon**: High-resolution custom One Piece pirate emblem packaged into all screen densities (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`, adaptive).

#### ☁️ 2. Cross-Device Google Drive AppData Sync
- **100% Serverless & Private**: Uses Google Drive's hidden `appDataFolder` (`drive.appdata` scope) to sync between your PC and your phone with zero monthly server costs.
- **Exact-Second Resume**: Start watching on PC, pause at 14m 23s, and resume on your Android phone at the exact same second.
- **Two-Way Synchronization**: Universal watch history and custom watchlists automatically merged using Last-Write-Wins (LWW) conflict resolution.

#### 📡 3. Live Broadcasts & Donghua
- Live AniList GraphQL `airingSchedules` integration with real episode badges (`SUB <ep>`).
- Dedicated filter for Chinese anime (Donghua) releases.
