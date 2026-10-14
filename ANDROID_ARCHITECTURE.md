# 📱 Aniverse Android Architecture & Development Roadmap

## 🌟 Executive Summary
Aniverse Android is a high-performance native Android application built with **100% Kotlin**, **Jetpack Compose**, and **Media3 ExoPlayer**. It is designed to deliver a YouTube-smooth, ad-free anime streaming experience with zero remote server dependency, offline history persistence, and hardware-accelerated playback.

---

## 🏗️ Architecture Overview

```
                      ┌─────────────────────────────────────────┐
                      │             MainActivity               │
                      └────────────────────┬────────────────────┘
                                           │
                                  ┌────────▼────────┐
                                  │    NavGraph     │
                                  └────────┬────────┘
                                           │
         ┌──────────────────┬──────────────┼──────────────┬──────────────────┐
         │                  │              │              │                  │
┌────────▼─────────┐ ┌──────▼──────┐ ┌─────▼─────┐ ┌──────▼──────┐ ┌─────────▼─────────┐
│   HomeScreen     │ │SearchScreen │ │Watchlist  │ │ProfileScreen│ │   PlayerScreen     │
│ (Hero + Shelves) │ │(Filters+Grid│ │ (Db Grid) │ │ (Settings)  │ │ (ExoPlayer+Gestures│
└────────┬─────────┘ └──────┬──────┘ └─────┬─────┘ └─────────────┘ └─────────┬──────────┘
         │                  │              │                                 │
         └──────────────────┼──────────────┴─────────────────────────────────┘
                            │
               ┌────────────▼────────────┐
               │       Data Layer        │
               ├─────────────────────────┤
               │ • AniList GraphQL API   │
               │ • Anikoto Native Scraper│
               │ • SQLite Database       │
               └─────────────────────────┘
```

The app follows **Modern Android Architecture (MAD)** guidelines:
- **UI Layer**: 100% Jetpack Compose with Material 3. Declarative, reactive, and hardware-accelerated.
- **State Management**: StateFlow and Coroutines within ViewModels (`HomeViewModel`).
- **Data Layer**:
  - `AniListApi`: Direct client to AniList GraphQL for trending, popular, and donghua feeds.
  - `AnikotoResolver`: Standalone in-app HTML parser and scraper using OkHttp and Jsoup to resolve episode lists, streaming servers, and skip timestamps.
  - `AniverseDbHelper`: High-performance SQLite database storing watch history (exact second timestamps) and bookmarked watchlists.
- **Playback Engine**: Media3 ExoPlayer (`1.3.1`) with custom gesture detectors and hardware-accelerated WebView fallback for protected iframe embeds.

---

## 🎨 Design System & Aesthetic (AniWave Theme)

The UI adheres strictly to the AniWave dark theme design tokens:
- **Canvas / Background**: `#0A0A10`
- **Surface Elevation**: `#12121D`
- **Card Background**: `#181826`
- **Primary Violet**: `#8B5CF6` (accent buttons, active tabs, progress)
- **Violet Dark**: `#7C3AED`
- **Accent Cyan**: `#06B6D4` (Chinese Donghua badge & server badge)
- **Accent Amber**: `#F59E0B` (Dub badge, ratings, brightness HUD)
- **Border / Outline**: `#26263A`

---

## 🛠️ Work Done So Far

### ✅ Phase 1: Toolchain Isolation & Scaffolding
- **Strict Storage Isolation**:
  - `GRADLE_USER_HOME` configured to `SupportingSofts\.gradle`.
  - `ANDROID_USER_HOME` & `ANDROID_AVD_HOME` configured to `SupportingSofts\.android`.
  - Android SDK 34/35 and OpenJDK 17 configured in `SupportingSofts\AndroidSDK`.
  - Pixel 6 AVD (`aniverse_avd`, API 34 Google APIs x86_64) created in `SupportingSofts\.android\avd\`.
  - **Guaranteed 0 bytes written to drive `C:`**.
- **Scaffolding (`android/`)**:
  - Root and app `build.gradle.kts` with Compose BOM `2024.06.00`, Media3 ExoPlayer, Coil Compose, OkHttp, Gson.
  - Full theme tokens ([Color.kt](file:///f:/cool%20projects/Aniverse/android/app/src/main/java/com/aniverse/app/ui/theme/Color.kt), [Theme.kt](file:///f:/cool%20projects/Aniverse/android/app/src/main/java/com/aniverse/app/ui/theme/Theme.kt), [Type.kt](file:///f:/cool%20projects/Aniverse/android/app/src/main/java/com/aniverse/app/ui/theme/Type.kt)).
  - Bottom navigation bar ([AniverseBottomBar.kt](file:///f:/cool%20projects/Aniverse/android/app/src/main/java/com/aniverse/app/ui/components/AniverseBottomBar.kt)).
  - Base screens ([HomeScreen.kt](file:///f:/cool%20projects/Aniverse/android/app/src/main/java/com/aniverse/app/ui/screens/home/HomeScreen.kt), [SearchScreen.kt](file:///f:/cool%20projects/Aniverse/android/app/src/main/java/com/aniverse/app/ui/screens/search/SearchScreen.kt), [WatchlistScreen.kt](file:///f:/cool%20projects/Aniverse/android/app/src/main/java/com/aniverse/app/ui/screens/watchlist/WatchlistScreen.kt), [ProfileScreen.kt](file:///f:/cool%20projects/Aniverse/android/app/src/main/java/com/aniverse/app/ui/screens/profile/ProfileScreen.kt)).
  - Compiled and verified Phase 1 APK in 47 seconds.

### ✅ Phase 2: Streaming Engine, YouTube Gestures & Offline Persistence
- **Native Resolver ([AnikotoResolver.kt](file:///f:/cool%20projects/Aniverse/android/app/src/main/java/com/aniverse/app/data/remote/AnikotoResolver.kt))**:
  - Implemented client-side HTML parsing via Jsoup.
  - Queries AniList metadata -> maps to Anikoto slug -> fetches internal episode catalog -> resolves streaming servers (Sub/Dub, HD-1, MegaPlay) -> extracts skip intro/outro data.
  - Self-contained: works on mobile with zero requirement for the desktop Node.js server.
- **YouTube-Grade Player ([PlayerScreen.kt](file:///f:/cool%20projects/Aniverse/android/app/src/main/java/com/aniverse/app/ui/screens/player/PlayerScreen.kt))**:
  - **Double-Tap Seek**: Left double-tap rewinds 10s, right double-tap advances 10s with animated HUD indicator.
  - **Vertical Swipe Controls**: Left-side vertical drag adjusts screen brightness; right-side vertical drag adjusts media volume.
  - **Episode Selector Modal**: ModalBottomSheet allowing users to switch episodes on the fly without exiting playback.
  - **Server & Audio Switcher**: ModalBottomSheet toggling Subbed vs Dubbed audio and alternate mirrors.
  - **Auto-Skip Intro / Outro**: Floating violet pill button appears during OP & ED intervals.
- **SQLite Database ([AniverseDbHelper.kt](file:///f:/cool%20projects/Aniverse/android/app/src/main/java/com/aniverse/app/data/local/AniverseDbHelper.kt))**:
  - `watch_history`: Persists exact playback timestamps, duration, and last watched dates.
  - `watchlist`: Stores user-saved bookmarks.
- **UI Integrations**:
  - "Continue Watching" shelf on Home screen displaying exact percentage progress bars.
  - Dynamic Watchlist screen rendering saved anime from SQLite.
- **Compiled Phase 2 APK**: [app-debug.apk](file:///f:/cool%20projects/Aniverse/android/app/build/outputs/apk/debug/app-debug.apk) (**22.26 MB**) in 19 seconds.

---

## 🔮 Future Roadmap (Phase 3 & Beyond)

### ✅ Phase 3: Multitasking, Production Optimization & Branding (Completed)
- **Picture-in-Picture (PiP) Multitasking**:
  - `enterPictureInPictureMode()` integrated into `MainActivity.kt` (`onUserLeaveHint`) and top-bar button in `PlayerScreen.kt`.
  - Seamless auto-PiP when navigating to the Android home screen or app switcher.
- **R8 Minification & Size Slashed to 3.68 MB**:
  - Full ProGuard shrinking and ABI splits (`arm64-v8a` & `armeabi-v7a`).
  - Minified production APK size reduced from 22.26 MB down to **3.68 MB** (well below the 15 MB limit).
- **Custom One Piece Pirate Branding**:
  - Generated and packaged high-resolution app icon mipmaps (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) and adaptive icon foregrounds.
- **GitHub Distribution**:
  - Released APKs attached directly to GitHub Release `v1.0.1` and linked in `README.md`.

---

## 🔮 Active Roadmap: Seamless Cross-Device Sync & Polish

### 🌟 Phase 4: Google Drive AppData Sync (Cross-Device Cloud Save)
A 100% serverless, private cross-device sync mechanism between **Desktop (Electron)** and **Mobile (Android)**:
1. **Google OAuth 2.0 (`drive.appdata` Scope)**:
   - Uses the user's personal, hidden Google Drive Application Data folder (`appDataFolder`).
   - Requires 0 external database servers, 0 monthly hosting costs, and 100% private to the user.
2. **Unified Data Schema (`aniverse_sync.json`)**:
   - **Exact Second Timestamps**: Full playback positions (`positionMs` & `durationMs`) for every episode.
   - **Universal Watch History**: Seamlessly resume watching on mobile exactly where you paused on PC.
   - **Custom Watchlists**: Custom user-named lists (e.g. *Watching*, *Favorites*, *Plan to Watch*).
   - **User Settings**: Auto-skip intro/outro preferences and audio language choices.
3. **Conflict Resolution**:
   - Two-way timestamp synchronization using Last-Write-Wins (LWW) based on `updatedAt` timestamps.

### 🛠️ Immediate Foundations (Phase 4A Prerequisites)
1. **Watchlist Bookmark Toggle in UI**:
   - Add bookmark icon toggle button on `PlayerScreen.kt` top bar and `AnimeCard.kt` to easily save/remove shows in the SQLite database.
2. **Persistent Settings in Profile**:
   - Store `autoSkipIntro`, `autoPlayNext`, and `preferDub` permanently in `SharedPreferences`.
3. **Auto-Play Next Episode**:
   - Listen for ExoPlayer playback completion to automatically advance to the next episode.
4. **Next / Prev Episode Player Buttons**:
   - Add quick navigation arrows directly on the player HUD overlay.

### 📦 Future Ecosystem Features
1. **AniList OAuth (Optional Community Tracking)**:
   - Sync episode counts and scores directly with public AniList accounts.
2. **Offline Download Manager**:
   - Download video segments using Android `WorkManager` for 100% offline playback.
3. **Chromecast & Miracast Casting**:
   - Stream video directly from phone to Google TV and Chromecast devices.

---

## 🛠️ Instructions for Developers & AI Agents

### Environment Configuration
Never execute standard `gradle` commands without sourcing environment variables first, or Gradle will download files to `C:\Users\<user>\.gradle`. Always use the provided helper scripts:

- **Configure Environment**:
  ```powershell
  . "SupportingSofts\setup_env.ps1"
  ```
- **Build Android APK**:
  ```powershell
  powershell -ExecutionPolicy Bypass -File "SupportingSofts\build_aniverse_android.ps1"
  ```
- **Run on Emulator or Device**:
  ```powershell
  powershell -ExecutionPolicy Bypass -File "SupportingSofts\run_aniverse_android.ps1"
  ```
- **Start Emulator**:
  ```powershell
  powershell -ExecutionPolicy Bypass -File "SupportingSofts\launch_emulator.ps1"
  ```
