# 🌌 Aniverse

A sleek, ad-free desktop application for streaming anime with zero setup, local save support, multi-watchlists, and live airing schedule integration. Packaged as a standalone Windows installer and portable executable.

<div align="center">

[![GitHub release](https://img.shields.io/github/v/release/thisissufian/Aniverse?style=for-the-badge&color=8B5CF6)](https://github.com/thisissufian/Aniverse/releases)
[![Platform: Windows](https://img.shields.io/badge/Platform-Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white)](#)
[![Zero Ads](https://img.shields.io/badge/Zero-Ads-success?style=for-the-badge)](#)
[![No Account Needed](https://img.shields.io/badge/Account-Not%20Required-blueviolet?style=for-the-badge)](#)

</div>

---

## 🛠️ Tech Stack & Architecture

<div align="center">

![Electron](https://img.shields.io/badge/Electron-47848F?style=for-the-badge&logo=electron&logoColor=white)
![React](https://img.shields.io/badge/React_18-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Vite](https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![TailwindCSS](https://img.shields.io/badge/Tailwind_CSS_v4-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)
![Node.js](https://img.shields.io/badge/Node.js-43853D?style=for-the-badge&logo=node.js&logoColor=white)
![Express.js](https://img.shields.io/badge/Express.js-000000?style=for-the-badge&logo=express&logoColor=white)
![GraphQL](https://img.shields.io/badge/AniList_GraphQL-E10098?style=for-the-badge&logo=graphql&logoColor=white)
![Cheerio](https://img.shields.io/badge/Cheerio-E88C1F?style=for-the-badge&logo=cheerio&logoColor=white)
![Framer Motion](https://img.shields.io/badge/Framer_Motion-0055FF?style=for-the-badge&logo=framer&logoColor=white)

</div>

<br />

| Layer | Technologies & Icons | Purpose |
| :--- | :--- | :--- |
| **Desktop Wrapper** | ![Electron](https://img.shields.io/badge/Electron-47848F?style=flat-square&logo=electron&logoColor=white) ![NSIS](https://img.shields.io/badge/NSIS-005571?style=flat-square) | Standalone Windows application with custom install path and zero-dependency portable `.exe` mode |
| **Frontend UI** | ![React](https://img.shields.io/badge/React_18-20232A?style=flat-square&logo=react&logoColor=61DAFB) ![Vite](https://img.shields.io/badge/Vite-646CFF?style=flat-square&logo=vite&logoColor=white) | High performance, responsive single-page client built with modern JSX and Vite bundling |
| **Styling & Effects** | ![Tailwind](https://img.shields.io/badge/Tailwind_CSS_v4-38B2AC?style=flat-square&logo=tailwind-css&logoColor=white) ![Framer Motion](https://img.shields.io/badge/Framer_Motion-0055FF?style=flat-square&logo=framer&logoColor=white) | AniWave-inspired dark UI (`#0a0a0f`, `#13131a`, `#8B5CF6`) with smooth animations and layout transitions |
| **Embedded Backend** | ![Node.js](https://img.shields.io/badge/Node.js-43853D?style=flat-square&logo=node.js&logoColor=white) ![Express](https://img.shields.io/badge/Express.js-000000?style=flat-square&logo=express&logoColor=white) | Self-contained Express server running locally on `localhost:5000` (eliminates remote server hosting costs) |
| **Metadata Engine** | ![GraphQL](https://img.shields.io/badge/AniList_GraphQL-E10098?style=flat-square&logo=graphql&logoColor=white) | Fetches rich anime details, posters, scores, and real-time live **Airing Schedules** |
| **Streaming Scraper** | ![Cheerio](https://img.shields.io/badge/Cheerio-E88C1F?style=flat-square) ![Axios](https://img.shields.io/badge/Axios-5A29E4?style=flat-square&logo=axios&logoColor=white) | Multi-server link resolution extracting high-speed video embeds and intro/outro skip timestamps |
| **Local Storage & Sync** | ![LocalStorage](https://img.shields.io/badge/LocalStorage-FFA500?style=flat-square) ![JSON Export](https://img.shields.io/badge/JSON_Backup-2ea44f?style=flat-square) | Client-side data management for watch history, multiple custom watchlists, and JSON export/import |

---

## ✨ Features

- 🚫 **Ad & Popup Blocker** — Custom Electron window handlers deny iframe ads, popups, and malicious external redirects.
- 📡 **Real Latest Airing Episodes** — Directly connected to live broadcast schedules (`airingSchedules`) showing the freshest newly aired episodes with badge numbers (`SUB 162`, `SUB 3`, etc.).
- 🎭 **Sub, Dub & Chinese (Donghua) Toggles** — Instant filtering for Japanese subbed releases, English dubs, and Chinese anime (*Renegade Immortal*, *Tales of Herding Gods*, *The Eternal Supreme*).
- 🏆 **Top Anime Filters** — Filter popular series by **Today**, **Week**, and **Month**.
- 📋 **Universal Watchlists** — Add anime to customizable, separate watchlists (e.g. for you and family members) right from the card hover, player sidebar, or history page.
- 💾 **100% Local Save File Support** — No login or database connection needed. Export and import your complete watch history and watchlists anytime.
- ⏩ **Auto Skip Intro & Outro** — Native skip buttons appear automatically during opening and ending songs.
- 🏴‍☠️ **Custom App Icon** — Custom One Piece pirate-themed branding integrated directly into the `.exe` and taskbar.

---

## 📦 Download

Grab the latest release from the [GitHub Releases](https://github.com/thisissufian/Aniverse/releases):

1. **`Aniverse Setup 1.0.0.exe`** (Standard installer — prompts you to pick your install directory)
2. **`Aniverse 1.0.0.exe`** (Portable version — keep all data off the `C:` drive by placing an `aniverse-data` folder next to it)

> 💡 **Windows Defender SmartScreen**: Because this is an open-source project without a paid Microsoft certificate, Windows may show a blue warning. Click **"More info"** → **"Run anyway"**.

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

## 📄 License
Personal & educational use only. Built with open-source tools and AniList public API.
