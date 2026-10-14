# Aniverse v2.0.0 — Mega Edition: Anime, K-Drama & Hollywood

Welcome to **Aniverse v2.0.0 (Mega Edition)**! This major release transforms Aniverse into a unified, cross-platform entertainment universe across Android and Windows Desktop, introducing interactive mode switching between Anime, K-Drama & Movies, and Hollywood Movies & TV Series.

---

### 🌟 What's New in v2.0.0

#### 1. 🎭 3-in-1 Entertainment Mode Switcher
Switch instantly between 3 dedicated entertainment modes from the Navbar pill on Desktop and Settings on Android:
- **Anime Mode**: AniList GraphQL metadata + Anikoto & MegaPlay multi-server fast streaming with Sub/Dub toggles and auto-skip intros.
- **K-Drama & K-Movies Mode**: Korean dramas and cinema powered by TMDB Korean discovery API (`ko`) with VidLink, MultiEmbed, and clean VidSrc streaming.
- **Hollywood Movies & TV Series Mode**: Global Hollywood blockbusters and trending television series with full season/episode selectors.

#### 2. ⚡ Verified Streaming & English Localization
- **English Titles for K-Drama**: All titles and details are localized in English across Android and Desktop so you can easily browse and search without Korean hangul characters.
- **Guaranteed Playable Streams**: Filtered out unreleased promotional entries and 600+ episode daytime variety shows. Catalogs prioritize popular scripted Korean dramas (*Squid Game*, *True Beauty*, *Queen of Tears*, *Crash Landing on You*) and hit movies/series (*Deadpool & Wolverine*, *Reacher*, *Spider-Man*, *Breaking Bad*).
- **Direct Embed Routing & Multi-Server**:
  - `HD-1 (VidLink)`: Premium high-definition embed with custom styling and multilingual subtitles.
  - `HD-2 (MultiEmbed)`: Robust multi-server stream with automatic fallback.
  - `HD-3 (AutoEmbed)`: High-speed backup server.
  - `HD-4 (2Embed)`: Multi-stream provider.
  - `HD-5 (VidSrc)`: Fast mirror on `vidsrc.pm`.

#### 3. 🛡️ Comprehensive Ad-Block & Popup Sandbox
- **Universal Ad & Popup Prevention**: Intercepts and denies all popups (`window.open`) at both the webContents and window level on Desktop and Android, stopping ad redirects while keeping media streams playing smoothly.
- **Player-Scoped Screen Brightness (Android)**: Brightness slider adjustments now strictly apply to the video player surface. When you minimize to mini player or leave the player, system brightness is automatically restored for the rest of the app.
- **Now Playing Spotify-style Bar**: Seamlessly tracks playback progress across Anime, K-Drama, and Hollywood modes.

---

### 📦 Release Assets & Exact File Sizes

| Platform | File | Size | Bytes | Architecture |
|---|---|---|---|---|
| **Windows Desktop** | `Aniverse Setup 2.0.0.exe` | **118.45 MB** | 124,206,918 bytes | x64 (NSIS Installer) |
| **Windows Desktop** | `Aniverse 2.0.0.exe` | **118.18 MB** | 123,921,720 bytes | x64 (Portable Executable) |
| **Android Mobile** | `Aniverse-v2.0.0-arm64.apk` | **4.01 MB** | 4,204,403 bytes | `arm64-v8a` (Modern Phones) |
| **Android Mobile** | `Aniverse-v2.0.0-armv7.apk` | **4.01 MB** | 4,204,403 bytes | `armeabi-v7a` (Older Devices) |
