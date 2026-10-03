# 🌌 Aniverse

A personal, ad-free anime streaming desktop application. Built with the MERN stack and packaged with Electron into a standalone Windows `.exe` installer.

> ⚠️ **Personal project** — for private use only. Not intended for public distribution.

---

## ✨ Features

- 🎬 **Sub & Dub Streaming** — Watch anime in Japanese (Sub) or English (Dub) with one-click switching
- 🖥️ **Desktop App** — Installs as a native Windows application via `Aniverse Setup.exe`
- 🔍 **Search & Browse** — Search anime with genre, format, status, and sort filters powered by AniList
- 📋 **Watchlist** — Track anime as Watching, Plan to Watch, Completed, On Hold, or Dropped
- 📊 **Watch Progress** — Auto-saves your position so you can resume where you left off
- 🔗 **MAL Sync** — Connect MyAnimeList to auto-sync your watch progress
- ⏭️ **Skip Intro/Outro** — Native skip buttons appear automatically during intros and outros
- 🖼️ **AniWave-Style UI** — Hero carousel, trending grid, top anime sidebar, and compact anime cards
- 🎨 **Purple Dark Theme** — Sleek dark UI with purple accents and glassmorphism
- 📱 **Responsive** — Works on desktop, tablet, and mobile
- 🚫 **Zero Ads** — Clean, distraction-free viewing

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 18, Vite, Tailwind CSS v4, Framer Motion, Radix UI |
| Backend | Express.js, Node.js |
| Database | MongoDB Atlas (Mongoose) |
| Video | MegaPlay embeds via iframe with postMessage bridge |
| Scraping | Cheerio + Axios (Anikoto pipeline) |
| Metadata | AniList GraphQL API |
| Sync | MyAnimeList API v2 (OAuth) |
| Auth | JWT + bcrypt |
| Desktop | Electron + electron-builder (NSIS installer) |

---

## 🚀 Getting Started

### Prerequisites

- **Node.js 18+**
- **MongoDB Atlas** account (or local MongoDB)
- *(Optional)* MAL API Client ID from [myanimelist.net/apiconfig](https://myanimelist.net/apiconfig)

### 1. Clone & Install

```bash
git clone https://github.com/thisissufian/Aniverse.git
cd Aniverse
npm run install-all
```

### 2. Configure Environment

```bash
cp .env.example server/.env
```

Edit `server/.env` with your MongoDB connection string and (optionally) MAL API keys. The app works without MAL keys.

### 3. Run in Development Mode

```bash
npm run dev
```

Or simply double-click **`Run-Dev.bat`** — it starts the servers and opens your browser automatically.

- **Backend** → http://localhost:5000
- **Frontend** → http://localhost:5173

### 4. Build Desktop App (.exe)

```bash
npm run dist
```

This compiles the React frontend, bundles it with the Express backend, and generates a Windows installer at:

```
release/Aniverse Setup 1.0.0.exe
```

Run the installer and launch Aniverse from your Start Menu like any other desktop app.

---

## 📁 Project Structure

```
Aniverse/
├── client/                    # React Frontend (Vite)
│   ├── src/
│   │   ├── components/
│   │   │   ├── layout/        # Navbar, Footer
│   │   │   ├── player/        # VideoPlayer (iframe + Artplayer)
│   │   │   └── ui/            # AnimeCard, AnimeGrid, HeroBanner, etc.
│   │   ├── context/           # AuthContext
│   │   ├── pages/             # Home, Watch, Search, AnimeDetail, etc.
│   │   └── services/          # API client (axios)
│   └── vite.config.js
├── server/                    # Express.js Backend
│   ├── src/
│   │   ├── config/            # DB connection, env vars
│   │   ├── controllers/       # Route handlers
│   │   ├── middleware/        # Auth, CORS proxy, cache, error handler
│   │   ├── models/            # User, WatchProgress, Watchlist
│   │   ├── routes/            # API route definitions
│   │   └── services/
│   │       ├── anilistService.js    # AniList GraphQL queries
│   │       ├── consumetService.js   # Anikoto streaming pipeline
│   │       └── malService.js        # MyAnimeList OAuth + sync
│   └── server.js
├── electron.js                # Electron main process
├── Run-Dev.bat                # One-click dev launcher
├── package.json               # Root config + electron-builder
└── .env.example
```

---

## 🔌 Streaming Pipeline

Aniverse uses a custom 5-step scraping pipeline to fetch real anime streams:

```
AniList ID → Anikoto Search → Episode List → Server List → MegaPlay Embed URL
```

1. **Search** — Queries `anikototv.to/filter` with title + format type (TV/Movie/OVA)
2. **Data ID** — Extracts the internal anime ID from the watch page
3. **Episodes** — Fetches episode list with sub/dub availability flags
4. **Servers** — Gets available servers (HD-1, Vidstream, etc.) for both Sub and Dub
5. **Embed** — Returns the final `megaplay.buzz` embed URL + skip timestamps

The video player communicates with MegaPlay via `postMessage` to pass skip intro/outro timestamps, enabling native skip buttons inside the player.

---

## 🔗 API Routes

### Anime (Public)
| Method | Route | Description |
|--------|-------|-------------|
| GET | `/api/anime/home` | Home page data (trending, popular, top airing) |
| GET | `/api/anime/trending` | Trending anime |
| GET | `/api/anime/popular` | Popular anime |
| GET | `/api/anime/search` | Search with `?search=&genres=&format=&sort=&status=` |
| GET | `/api/anime/:id` | Anime details |
| GET | `/api/anime/:id/episodes` | Episode list with sub/dub flags and dataIds |

### Streaming
| Method | Route | Description |
|--------|-------|-------------|
| GET | `/api/stream/:dataIds/sources` | Embed URL + servers + skip data |
| GET | `/api/stream/:dataIds/servers` | Available servers list |
| GET | `/api/stream/embed/:linkId` | Specific server embed URL |

### Auth
| Method | Route | Description |
|--------|-------|-------------|
| POST | `/api/auth/register` | Create account |
| POST | `/api/auth/login` | Login |
| GET | `/api/auth/me` | Current user |

### User (Protected)
| Method | Route | Description |
|--------|-------|-------------|
| GET/POST/PUT/DELETE | `/api/user/watchlist` | Watchlist CRUD |
| GET | `/api/user/history` | Watch history |
| POST | `/api/user/progress` | Save watch progress |

### MyAnimeList
| Method | Route | Description |
|--------|-------|-------------|
| GET | `/api/mal/auth` | Start OAuth flow |
| GET | `/api/mal/callback` | OAuth callback |
| GET | `/api/mal/status` | Connection status |
| POST | `/api/mal/sync-all` | Bulk sync to MAL |

---

## 🎨 Theme

| Color | Hex | Usage |
|-------|-----|-------|
| Background | `#0a0a0f` | Main background |
| Card | `#13131a` | Card backgrounds |
| Surface | `#1a1a2e` | Elevated surfaces |
| Purple Primary | `#8B5CF6` | Buttons, accents |
| Purple Dark | `#7C3AED` | Hover states |
| Purple Light | `#A78BFA` | Links, highlights |

---

## 📝 License

Personal use only. Built with open-source APIs for educational purposes.
