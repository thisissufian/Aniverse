# 🌌 Aniverse

A sleek, ad-free desktop application for streaming anime with zero setup, local save support, multi-watchlists, and live airing schedule integration. Packaged as a standalone Windows installer and portable executable.

<div align="center">

[![GitHub release](https://img.shields.io/github/v/release/thisissufian/Aniverse?style=for-the-badge&color=8B5CF6)](https://github.com/thisissufian/Aniverse/releases)
[![Platform: Windows](https://img.shields.io/badge/Platform-Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white)](#)
[![Platform: Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#)
[![Zero Ads](https://img.shields.io/badge/Zero-Ads-success?style=for-the-badge)](#)
[![No Account Needed](https://img.shields.io/badge/Account-Not%20Required-blueviolet?style=for-the-badge)](#)

</div>

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

| Layer | Technologies & Icons | Purpose |
| :--- | :--- | :--- |
| **Android Native App** | ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white) ![Compose](https://img.shields.io/badge/Compose-4285F4?style=flat-square&logo=android&logoColor=white) ![ExoPlayer](https://img.shields.io/badge/ExoPlayer-E50914?style=flat-square) ![SQLite](https://img.shields.io/badge/SQLite-003B57?style=flat-square&logo=sqlite&logoColor=white) | 60/120fps hardware-accelerated mobile streaming with YouTube gestures, Jsoup scraping, and local watch history |
| **Desktop Wrapper** | ![Electron](https://img.shields.io/badge/Electron-47848F?style=flat-square&logo=electron&logoColor=white) ![NSIS](https://img.shields.io/badge/NSIS-005571?style=flat-square) | Standalone Windows application with custom install path and zero-dependency portable `.exe` mode |
| **Frontend UI** | ![React](https://img.shields.io/badge/React_18-20232A?style=flat-square&logo=react&logoColor=61DAFB) ![Vite](https://img.shields.io/badge/Vite-646CFF?style=flat-square&logo=vite&logoColor=white) | High performance, responsive single-page client built with modern JSX and Vite bundling |
| **Styling & Effects** | ![Tailwind](https://img.shields.io/badge/Tailwind_CSS_v4-38B2AC?style=flat-square&logo=tailwind-css&logoColor=white) ![Framer Motion](https://img.shields.io/badge/Framer_Motion-0055FF?style=flat-square&logo=framer&logoColor=white) | AniWave-inspired dark UI (`#0a0a0f`, `#13131a`, `#8B5CF6`) with smooth animations and layout transitions |
| **Embedded Backend** | ![Node.js](https://img.shields.io/badge/Node.js-43853D?style=flat-square&logo=node.js&logoColor=white) ![Express](https://img.shields.io/badge/Express.js-000000?style=flat-square&logo=express&logoColor=white) | Self-contained Express server running locally on `localhost:5000` (eliminates remote server hosting costs) |
| **Metadata Engine** | ![GraphQL](https://img.shields.io/badge/AniList_GraphQL-E10098?style=flat-square&logo=graphql&logoColor=white) | Fetches rich anime details, posters, scores, and real-time live **Airing Schedules** |
| **Streaming Scraper** | ![Cheerio](https://img.shields.io/badge/Cheerio-E88C1F?style=flat-square) ![Jsoup](https://img.shields.io/badge/Jsoup-005A9C?style=flat-square) | Multi-server link resolution extracting high-speed video embeds and intro/outro skip timestamps |
| **Local Storage & Sync** | ![SQLite](https://img.shields.io/badge/SQLite-003B57?style=flat-square&logo=sqlite&logoColor=white) ![LocalStorage](https://img.shields.io/badge/LocalStorage-FFA500?style=flat-square) | Client-side data management for watch history, multiple custom watchlists, and JSON export/import |

---

## ✨ Features

- 🚫 **Ad & Popup Blocker** — Custom Electron window handlers deny iframe ads, popups, and malicious external redirects.
- 📡 **Real Latest Airing Episodes** — Directly connected to live broadcast schedules (`airingSchedules`) showing the freshest newly aired episodes with badge numbers (`SUB 162`, `SUB 3`, etc.).
- 🎭 **Sub, Dub & Chinese (Donghua) Toggles** — Instant filtering for Japanese subbed releases, English dubs, and Chinese anime (*Renegade Immortal*, *Tales of Herding Gods*, *The Eternal Supreme*).
- 🏆 **Top Anime Filters** — Filter popular series by **Today**, **Week**, and **Month**.
- 📋 **Universal Watchlists** — Add anime to customizable, separate watchlists (e.g. for you and family members) right from the card hover, player sidebar, or history page.
- 💾 **100% Local Save File Support** — No login or database connection needed. Export and import your complete watch history and watchlists anytime.
- ⏩ **Auto Skip Intro & Outro** — Native skip buttons appear automatically during opening and ending songs.
- 📱 **Native YouTube-Smooth Android App** — Jetpack Compose + ExoPlayer mobile app with 120Hz smooth scrolling, double-tap seek, vertical swipe brightness/volume gestures, and native Picture-in-Picture (PiP).
- 🏴‍☠️ **Custom App Icon** — Custom One Piece pirate-themed branding integrated directly into the `.exe`, APK launcher, and taskbar.

---

## 📦 Download

### 📱 Android APKs (Ultralight ~3.9 MB)
- 🚀 **[`Aniverse-v1.0.0-arm64.apk`](https://github.com/thisissufian/Aniverse/releases/download/v1.1.0/Aniverse-v1.0.0-arm64.apk)** (Modern 64-bit Android smartphones & tablets)
- 📦 **[`Aniverse-v1.0.0-armv7.apk`](https://github.com/thisissufian/Aniverse/releases/download/v1.1.0/Aniverse-v1.0.0-armv7.apk)** (32-bit Android devices & older emulators)

### 💻 Windows Desktop
- 💿 **[`Aniverse.Setup.1.0.0.exe`](https://github.com/thisissufian/Aniverse/releases/download/v1.0.1/Aniverse.Setup.1.0.0.exe)** (Standard installer — prompts you to pick your install directory)
- 💼 **[`Aniverse.1.0.0.exe`](https://github.com/thisissufian/Aniverse/releases/download/v1.0.1/Aniverse.1.0.0.exe)** (Portable version — keep all data isolated by placing an `aniverse-data` folder next to it)

> 💡 **Notice**: Because this is an open-source project without a paid signing certificate, Windows/Android may show a prompt. On Windows, click **"More info"** → **"Run anyway"**. On Android, allow "Install from unknown sources".

---

## 🔌 Streaming Pipeline

```
AniList ID / Airing Schedule
            │
            ▼
    Anikoto Title & Slug Search
            │
            ▼
    Internal Episode ID Resolution
            │
            ▼
    Server List (Sub / Dub / HD-1 / Vidstream)
            │
            ▼
    MegaPlay Embed URL + Skip Intro/Outro Timestamps
```

---

## 🛠️ Local Development

### Prerequisites
- [Node.js](https://nodejs.org/) (v18 or higher)
- npm

### Installation & Run

```bash
# Clone the repository
git clone https://github.com/thisissufian/Aniverse.git
cd Aniverse

# Install dependencies for both root and client
npm run install-all

# Start both backend and frontend concurrently
npm run dev
```

### Packaging Desktop Apps

```bash
# Compiles React and builds Windows installer + portable EXE
npm run dist
```

Executables will be generated in `release/`.

---

## 📱 Native Android App (Jetpack Compose + ExoPlayer)

The native Android app is located in `android/` and built with **Kotlin, Jetpack Compose, Material 3, and Media3 ExoPlayer** for hardware-accelerated 60/120fps playback.

### ✨ Android Features
- ⚡ **YouTube-Style Gestures** — Double-tap left/right to seek ±10s with ripple animations; vertical swipe on screen edges for brightness and volume HUD controls.
- 🎬 **Dual Playback Engine** — Plays direct HLS/MP4 streams in native ExoPlayer, and iframe embeds (MegaPlay/Vidstream) in hardware-accelerated ad-blocked WebView.
- 📋 **In-Player Episode & Server Drawer** — Bottom sheets to pick any episode or switch between Subbed/Dubbed audio and server mirrors without stopping playback.
- ⏩ **Auto-Skip Intro & Outro** — Automatic OP/ED detection with floating skip chips.
- 💾 **Offline SQLite Persistence** — Automatically tracks watch history with exact second timestamps and powers the "Continue Watching" shelf and Watchlist screen.
- 🛡️ **Zero Server Required** — Embedded client-side Jsoup resolver scrapes Anikoto streams directly on mobile with zero requirement for the desktop PC backend.

> 📖 **Full Architectural Documentation**: See [ANDROID_ARCHITECTURE.md](file:///f:/cool%20projects/Aniverse/ANDROID_ARCHITECTURE.md) for detailed diagrams, data models, and the upcoming Phase 3 & 4 roadmap.

### 🔨 Build APK

```powershell
powershell -ExecutionPolicy Bypass -File "SupportingSofts\build_aniverse_android.ps1"
```

The APK will be generated at:
`android/app/build/outputs/apk/debug/app-debug.apk` (22.26 MB)

### 📲 Launch on Emulator or Phone

```powershell
powershell -ExecutionPolicy Bypass -File "SupportingSofts\run_aniverse_android.ps1"
```

---

## 📄 License
Personal & educational use only. Built with open-source tools and AniList public API.

