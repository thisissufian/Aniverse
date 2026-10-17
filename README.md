# 🌌 Aniverse — Mega Edition (v2.0.0)

A sleek, ad-free cross-platform application for streaming **Anime**, **K-Drama & Korean Movies**, and **Hollywood TV Series & Cinema** with zero setup, local save support, multi-watchlists, floating mini-player, and instant mode switching. Built for **Android**, **Windows Desktop**, and **macOS** (Apple Silicon & Intel).

<div align="center">

[![GitHub release](https://img.shields.io/github/v/release/thisissufian/Aniverse?style=for-the-badge&color=8B5CF6)](https://github.com/thisissufian/Aniverse/releases)
[![Platform: Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#-android-mobile-apks)
[![Platform: Windows](https://img.shields.io/badge/Platform-Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white)](#-windows-desktop)
[![Platform: macOS](https://img.shields.io/badge/Platform-macOS-000000?style=for-the-badge&logo=apple&logoColor=white)](#-macos-apple-silicon--intel)
[![Zero Ads](https://img.shields.io/badge/Zero-Ads-success?style=for-the-badge)](#)
[![No Account Needed](https://img.shields.io/badge/Account-Not%20Required-blueviolet?style=for-the-badge)](#)

</div>

---

## 🎭 3-in-1 Entertainment Universe

Aniverse v2.0.0 introduces instant mode switching from the desktop topbar pill or mobile settings drawer:

1. **🎌 Anime Mode**: AniList GraphQL metadata + Anikoto & MegaPlay multi-server fast streaming with Sub/Dub toggles, Chinese Donghua support, and auto-skip intros.
2. **🥢 K-Drama & Korean Movies Mode**: Trending and top-rated Korean series and cinema with English localized titles, ultra-low-latency HLS streams, and anime-style numbered episode grids.
3. **🎬 Hollywood Movies & TV Series Mode**: Blockbuster movies and binge-worthy global television series with complete season and episode selectors.

---

## 🛠️ Tech Stack & Architecture

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![ExoPlayer](https://img.shields.io/badge/Media3_ExoPlayer-E50914?style=for-the-badge)
![Electron](https://img.shields.io/badge/Electron-47848F?style=for-the-badge&logo=electron&logoColor=white)
![React](https://img.shields.io/badge/React_18-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Vite](https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![GraphQL](https://img.shields.io/badge/AniList_GraphQL-E10098?style=for-the-badge&logo=graphql&logoColor=white)

</div>

<br />

| Layer | Technologies | Purpose |
| :--- | :--- | :--- |
| **Android Native App** | Kotlin, Jetpack Compose, Media3 ExoPlayer, SQLite, Jsoup | 60/120fps hardware-accelerated mobile streaming with YouTube gestures, persistent bottom mini-player, and local watch history |
| **Desktop & macOS Wrapper** | Electron 44, electron-builder, NSIS, Apple DMG | Cross-platform desktop app for Windows (Installer + Portable) and macOS (Apple Silicon `arm64` + Intel `x64`) |
| **Frontend UI** | React 18, Vite, Tailwind CSS v4, Framer Motion | High-performance AniWave-inspired dark UI (`#0a0a0f`, `#13131a`, `#8B5CF6`) with smooth animations and layout transitions |
| **Embedded Backend** | Node.js, Express | Self-contained Express server running locally on `localhost:5000` (zero remote server hosting costs) |
| **Metadata Engine** | AniList GraphQL + TMDB API | Real-time airing schedules, posters, scores, and localized English descriptions for all media modes |
| **Streaming Pipeline** | MegaPlay, VixSrc, VsEmbed, VidLink, AnyEmbed | Multi-server stream resolvers extracting high-speed video embeds, multi-audio tracks, and skip timestamps |
| **Local Storage & Sync** | SQLite, LocalStorage | Client-side tracking for watch history, multiple custom watchlists, and JSON export/import |

---

## ✨ Features

- 🚫 **Universal Ad & Popup Prevention** — Native window and webContents request interceptors deny popups (`window.open`), banners, and tracker networks, keeping streams clean and undisturbed.
- ⚡ **Ultra-Low Latency Streaming** — Powered by `VixSrc` and `VsEmbed` direct HTML5/HLS pipelines for instant playback start with custom theme colors and multilingual subtitle tracks.
- 🔢 **Anime-Style Numbered Episode Grid** — Switch between a compact numbered button grid (`1`, `2`, `3`...) and a detailed list view for effortless multi-episode navigation.
- 🪟 **Floating Mini Player & Spotify-Style Bottom Bar** — Minimize videos into a floating window while continuing to browse, with synchronized playback controls in the bottom bar.
- 📱 **Native YouTube-Smooth Gestures (Android)** — Double-tap seek (±10s), vertical swipe gestures for volume and scoped brightness (strictly isolated to the video player), and picture-in-picture.
- ⏩ **Auto Skip Intro & Outro** — Native skip buttons appear automatically during opening and ending songs.
- 📋 **Universal Watchlists & Local Save** — Organize items into customizable watchlists. Export and import your complete watch history as a JSON backup anytime with zero login required.
- 🌐 **Clean English Titles Everywhere** — All K-Drama and Movie titles, descriptions, and episode guides are strictly localized in English for effortless searching.

---

## 📦 Downloads & Releases (v2.0.0)

All official binaries are available on the [GitHub Releases Page](https://github.com/thisissufian/Aniverse/releases/tag/v2.0.0):

### 🍎 macOS (Apple Silicon & Intel)
| Package | Architecture | Size | Description |
| :--- | :--- | :--- | :--- |
| 🚀 **[Aniverse-2.0.0-arm64.dmg](https://github.com/thisissufian/Aniverse/releases/download/v2.0.0/Aniverse-2.0.0-arm64.dmg)** | Apple Silicon (`arm64`) | **134.90 MB** | Drag-and-drop DMG for M1, M2, M3, M4 Macs |
| 📦 **[Aniverse-2.0.0-arm64-mac.zip](https://github.com/thisissufian/Aniverse/releases/download/v2.0.0/Aniverse-2.0.0-arm64-mac.zip)** | Apple Silicon (`arm64`) | **138.17 MB** | Standalone ZIP archive for Apple Silicon |
| 💿 **[Aniverse-2.0.0.dmg](https://github.com/thisissufian/Aniverse/releases/download/v2.0.0/Aniverse-2.0.0.dmg)** | Intel (`x64`) | **138.58 MB** | Drag-and-drop DMG for Intel Macs |
| 💼 **[Aniverse-2.0.0-mac.zip](https://github.com/thisissufian/Aniverse/releases/download/v2.0.0/Aniverse-2.0.0-mac.zip)** | Intel (`x64`) | **141.89 MB** | Standalone ZIP archive for Intel Macs |

> 💡 **First-time macOS Setup**: Because this open-source build is self-signed, right-click (or Control-click) `Aniverse.app` → select **Open** → click **Open** to run it.

### 💻 Windows Desktop
| Package | Format | Size | Description |
| :--- | :--- | :--- | :--- |
| 💿 **[Aniverse Setup 2.0.0.exe](https://github.com/thisissufian/Aniverse/releases/download/v2.0.0/Aniverse.Setup.2.0.0.exe)** | NSIS Setup | **118.45 MB** | Windows Installer with custom directory picker |
| 💼 **[Aniverse 2.0.0.exe](https://github.com/thisissufian/Aniverse/releases/download/v2.0.0/Aniverse.2.0.0.exe)** | Portable EXE | **118.18 MB** | Zero-install standalone executable (create `aniverse-data/` folder for portable saves) |

### 📱 Android Mobile APKs
| Package | Architecture | Size | Description |
| :--- | :--- | :--- | :--- |
| 🚀 **[Aniverse-v2.0.0-arm64.apk](https://github.com/thisissufian/Aniverse/releases/download/v2.0.0/Aniverse-v2.0.0-arm64.apk)** | `arm64-v8a` | **4.01 MB** | Optimized for modern 64-bit Android devices |
| 📦 **[Aniverse-v2.0.0-armv7.apk](https://github.com/thisissufian/Aniverse/releases/download/v2.0.0/Aniverse-v2.0.0-armv7.apk)** | `armeabi-v7a` | **4.01 MB** | Compatible with older 32-bit Android devices & emulators |

---

## 🔌 Streaming Architecture

```
                    ┌────────────────────────┐
                    │    Mode Switcher       │
                    └───┬────────────────┬───┘
                        │                │
            [Anime Mode]│                │[K-Drama & Hollywood]
                        ▼                ▼
             AniList GraphQL API       TMDB API (ko/en-US)
                        │                │
                        ▼                ▼
              Anikoto Scraper        Stream Provider Dispatch
                        │                │
                        ▼                ▼
                 MegaPlay HLS      VixSrc / VsEmbed / VidLink
                        │                │
                        └───────┬────────┘
                                ▼
               Ad-Blocked Embedded Video Player
```

---

## 🛠️ Local Development & Building

### Prerequisites
- [Node.js](https://nodejs.org/) (v20 or higher)
- npm

### 1. Run in Development Mode
```bash
# Clone the repository
git clone https://github.com/thisissufian/Aniverse.git
cd Aniverse

# Install dependencies
npm run install-all

# Start both backend server and React client concurrently
npm run dev
```

### 2. Package Desktop Apps
```bash
# Build React client and package Windows installer + portable EXE
npm run dist

# Build macOS binaries (run on macOS or via GitHub Actions workflow)
npm run dist:mac
```
Packaged binaries will be located in `release/`.

### 3. Build Android App
```powershell
# Build release APKs (located in release/android/)
powershell -ExecutionPolicy Bypass -File "SupportingSofts\build_aniverse_android.ps1"

# Launch in connected emulator or physical device
powershell -ExecutionPolicy Bypass -File "SupportingSofts\run_aniverse_android.ps1"
```

---

## 📄 License
Personal & educational use only. Built with open-source tools, AniList public GraphQL, and TMDB public discovery API.
