# Aniverse v2.0.0 — Mega Edition: Anime, K-Drama & Hollywood

Welcome to **Aniverse v2.0.0 (Mega Edition)**! This major release transforms Aniverse into a unified, cross-platform entertainment universe across Android and Windows Desktop, introducing interactive mode switching between Anime, K-Drama & Movies, and Hollywood Movies & TV Series.

---

### 🌟 What's New in v2.0.0

#### 1. 🎭 3-in-1 Entertainment Mode Switcher
Switch instantly between 3 dedicated entertainment modes from the Navbar pill on Desktop and Settings on Android:
- **Anime Mode**: AniList GraphQL metadata + Anikoto & MegaPlay multi-server fast streaming with Sub/Dub toggles and auto-skip intros.
- **K-Drama & K-Movies Mode**: Korean dramas and cinema powered by TMDB Korean discovery API (`ko`) with VidLink, MultiEmbed, and clean VidSrc streaming.
- **Hollywood Movies & TV Series Mode**: Global Hollywood blockbusters and trending television series with full season/episode selectors.

#### 2. ⚡ Verified Streaming & Smart Catalogs
- **Guaranteed Playable Streams**: Filtered out unreleased promotional entries and 600+ episode daytime variety shows. Catalogs prioritize popular scripted Korean dramas (*Squid Game*, *True Beauty*, *Queen of Tears*, *Crash Landing on You*) and hit movies/series (*Deadpool & Wolverine*, *Reacher*, *Spider-Man*, *Breaking Bad*).
- **Multi-Server Streaming**:
  - `HD-1 (VidLink)`: Premium high-definition embed with custom styling.
  - `HD-2 (MultiEmbed)`: Robust multi-server stream with automatic fallback.
  - `HD-3 (VidSrc)`: Clean VidSrc player source without popup injections.
  - `HD-4 (AutoEmbed)`: High-speed backup server.
  - `HD-5 (2Embed)`: Multi-stream provider.

#### 3. 🛡️ Comprehensive Ad-Block & Popup Sandbox
- **Network-Level Ad Filter**: Electron intercepts and cancels requests to all known ad networks, tracking scripts, and popunders before they can download.
- **Iframe Sandboxing**: Stripped `allow-popups` and `allow-top-navigation` so video iframes can never spawn new windows or hijack parent pages.
- **Android Protection**: WebViews enforce `javaScriptCanOpenWindowsAutomatically = false`, `setSupportMultipleWindows(false)`, and cancel ad requests in `shouldInterceptRequest`.

---

### 📦 Release Assets & Exact File Sizes

| Platform | File | Size | Bytes | Architecture |
|---|---|---|---|---|
| **Windows Desktop** | `Aniverse Setup 2.0.0.exe` | **118.45 MB** | 124,206,806 bytes | x64 (NSIS Installer) |
| **Windows Desktop** | `Aniverse 2.0.0.exe` | **118.18 MB** | 123,921,600 bytes | x64 (Portable Executable) |
| **Android Mobile** | `Aniverse-v2.0.0-arm64.apk` | **4.01 MB** | 4,204,047 bytes | `arm64-v8a` (Modern Phones) |
| **Android Mobile** | `Aniverse-v2.0.0-armv7.apk` | **4.01 MB** | 4,204,047 bytes | `armeabi-v7a` (Older Devices) |
