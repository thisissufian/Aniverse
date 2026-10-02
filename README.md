# 🌌 Aniverse — Personal Ad-Free Anime Streaming Platform

A personal, ad-free anime watching platform built with the MERN stack. Features a sleek purple & black dark theme, smooth HLS video streaming, watchlist management, and MyAnimeList sync.

> ⚠️ This is a **personal project** for private use only. Not intended for public distribution.

## ✨ Features

- 🎬 **Smooth Streaming** — Artplayer + HLS.js with adaptive quality, web workers, and auto error recovery
- 🔍 **Search & Browse** — Search anime with genre, format, status, and sort filters via AniList API
- 📋 **Watchlist** — Track anime as Watching, Plan to Watch, Completed, On Hold, or Dropped
- 📊 **Watch Progress** — Auto-saves your position so you can resume where you left off
- 🔗 **MAL Sync** — Connect MyAnimeList to auto-sync your watch progress and list status
- 🔐 **User Accounts** — Simple username/password authentication with JWT
- 🎨 **Purple Dark Theme** — Sleek glassmorphism UI with purple accents
- 📱 **Responsive** — Works on desktop, tablet, and mobile
- 🚫 **Zero Ads** — Clean, distraction-free viewing experience

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 18, Vite, Tailwind CSS v4, Framer Motion |
| Backend | Express.js, Node.js |
| Database | MongoDB with Mongoose |
| Video | Artplayer, HLS.js, artplayer-plugin-hls-control |
| APIs | AniList GraphQL (metadata), Consumet (streaming), MAL v2 (sync) |
| Auth | JWT + bcrypt |

## 🚀 Quick Start

### Prerequisites
- Node.js 18+
- Docker & Docker Compose (for MongoDB + Consumet API)
- (Optional) MAL API Client ID from [myanimelist.net/apiconfig](https://myanimelist.net/apiconfig)

### 1. Clone & Install
```bash
cd "f:\cool projects\Aniverse"
npm run install-all
```

### 2. Start Infrastructure
```bash
docker-compose up -d
```
This starts:
- **MongoDB** on port 27017
- **Consumet API** on port 3001

### 3. Configure Environment
```bash
cp .env.example server/.env
```
Edit `server/.env` with your settings. The app works without MAL keys — just leave them empty.

### 4. Start Development
```bash
npm run dev
```
This starts both:
- **Backend** at http://localhost:5000
- **Frontend** at http://localhost:5173

## 📁 Project Structure

```
Aniverse/
├── client/                 # React Frontend (Vite)
│   ├── src/
│   │   ├── components/     # UI components (layout, anime, player, ui)
│   │   ├── context/        # AuthContext
│   │   ├── pages/          # Route pages
│   │   └── services/       # API client
│   └── vite.config.js
├── server/                 # Express.js Backend
│   ├── src/
│   │   ├── config/         # DB, env config
│   │   ├── controllers/    # Route handlers
│   │   ├── middleware/      # Auth, CORS proxy, cache, errors
│   │   ├── models/         # User, WatchProgress, Watchlist
│   │   ├── routes/         # API routes
│   │   └── services/       # AniList, Consumet, MAL API wrappers
│   └── server.js
├── docker-compose.yml
└── package.json
```

## 🔗 API Routes

### Anime (Public)
- `GET /api/anime/home` — Home page data
- `GET /api/anime/trending` — Trending anime
- `GET /api/anime/popular` — Popular anime
- `GET /api/anime/search?q=&genres=&format=&sort=` — Search
- `GET /api/anime/:id` — Anime details
- `GET /api/anime/:id/episodes` — Episode list

### Streaming
- `GET /api/stream/:episodeId/sources` — Video sources (m3u8)
- `GET /api/stream/:episodeId/servers` — Available servers

### Auth
- `POST /api/auth/register` — Create account
- `POST /api/auth/login` — Login
- `GET /api/auth/me` — Current user

### User (Protected)
- `GET/POST/PUT/DELETE /api/user/watchlist` — Watchlist CRUD
- `GET /api/user/history` — Watch history
- `POST /api/user/progress` — Save progress

### MyAnimeList
- `GET /api/mal/auth` — Start OAuth flow
- `GET /api/mal/callback` — OAuth callback
- `GET /api/mal/status` — Connection status
- `POST /api/mal/sync-all` — Bulk sync to MAL

## 🎨 Theme Colors

| Color | Hex | Usage |
|-------|-----|-------|
| Background | `#0a0a0f` | Main background |
| Card | `#13131a` | Card backgrounds |
| Surface | `#1a1a2e` | Elevated surfaces |
| Purple Primary | `#8B5CF6` | Buttons, accents |
| Purple Dark | `#7C3AED` | Hover states |
| Purple Light | `#A78BFA` | Links, highlights |

## 📝 License

Personal use only. Built with open-source APIs for educational purposes.
