## 🌌 Aniverse v1.1.0 — Native Android Launch & Google Drive Cloud Sync

We are thrilled to release **Aniverse v1.1.0**, featuring our brand-new standalone **Native Android Mobile App** and seamless **Cross-Device Google Drive Cloud Sync**!

---

### 📦 Download Android APKs

| Architecture | Package | Description | Size |
| :--- | :--- | :--- | :--- |
| **ARM64 (64-bit)** | **[`Aniverse-v1.0.0-arm64.apk`](https://github.com/thisissufian/Aniverse/releases/download/v1.1.0/Aniverse-v1.0.0-arm64.apk)** | **Recommended** for modern Android phones and tablets | **3.95 MB** |
| **ARMv7 (32-bit)** | **[`Aniverse-v1.0.0-armv7.apk`](https://github.com/thisissufian/Aniverse/releases/download/v1.1.0/Aniverse-v1.0.0-armv7.apk)** | For older 32-bit devices and legacy emulators | **3.95 MB** |

> 💡 **Android Installation Note**: Since Aniverse is an open-source project without a commercial signing certificate, Android may show an *"Install from unknown sources"* or *"Unrecognized app"* prompt. Tap **Settings** $\rightarrow$ **Allow from this source** $\rightarrow$ **Install anyway**.

---

### 🌟 What's New in v1.1.0

#### 📱 1. Standalone Native Android Mobile App
- **Ultralight 3.95 MB APK**: Engineered with **Native Kotlin**, **Jetpack Compose (BOM 2024.06.00)**, and **Media3 ExoPlayer**. Full R8 ProGuard code & resource minification.
- **YouTube-Smooth 120Hz Gesture Controls**:
  - **Double-Tap Seek**: Rewind 10s on the left, forward 10s on the right with animated HUD pills.
  - **Vertical Edge Swiping**: Left side adjusts screen brightness; right side adjusts media volume.
  - **Quick Episode Navigation**: Floating Previous $\vert\blacktriangleleft\blacktriangleleft$ and Next $\blacktriangleright\blacktriangleright\vert$ buttons.
- **Picture-in-Picture (PiP)**: Keep watching in a floating mini-player while multitasking, with automatic OS transition on swipe-to-home.
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
