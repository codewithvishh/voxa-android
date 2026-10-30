# 📱 Voxa — Native Android Streaming Application

Production-grade, native Android music streaming client engineered with **Kotlin**, **Jetpack Compose**, and **AndroidX Media3 (ExoPlayer & MediaSessionService)**, connecting directly to the deployed Voxa Spring Boot & Neon PostgreSQL backend.

---

## 🚀 Key Features

* **Lossless Audio Streaming Engine (AndroidX Media3 ExoPlayer)**:
  - Hardware-level `MediaSessionService` foreground service.
  - Zero playback interruption when screen is locked or switching between apps.
  - Automatic system Audio Focus handling (smooth ducking for maps, automatic pause for incoming phone calls).
  - Auto-pausing when headphones are unplugged (`setHandleAudioBecomingNoisy(true)`).
* **Native System Media Notification & Lock Screen Controls**:
  - Full Android 13/14/15 media drawer integration displaying album art, scrubbing seekbar, play/pause, prev/next.
* **30-Second Listening Play Count Audit**:
  - Automatically dispatches `POST /api/songs/{id}/play` mutation only after 30 seconds of continuous verified playback.
* **Synchronized Live Karaoke Lyrics**:
  - High-precision LRC parser syncing live line-by-line scrolling with track progress.
  - Tap-to-seek lyric lines directly into playback timeline.
* **Dynamic Era-Station Feed (`/api/home`)**:
  - Personalized sections, Trending carousels, and era-filtered stations.
* **Instant Search & Autocomplete (`/api/search`, `/api/search/suggest`)**:
  - Debounced input querying tracks, artists, and albums in real time.
* **JWT Stateless Authentication (`/api/auth/send-otp`, `/api/auth/verify-otp`)**:
  - Passwordless 6-digit email OTP login with automatic Bearer token injection.
* **Modern Spotify/Apple Music-grade UI**:
  - Floating mini-player bar, bottom navigation, and expandable full-screen player modal styled in Voxa signature red (`#FF2A51`) and deep midnight dark theme (`#0E111B`).

---

## 🛠️ Connecting Your Deployed Render Backend

Open `app/src/main/java/com/voxa/music/util/Constants.kt` and replace the placeholder with your live Render backend URL:

```kotlin
object Constants {
    // Replace with your actual deployed Render URL:
    const val BASE_URL = "https://your-voxa-backend.onrender.com/"
    ...
}
```

> **Note**: Always ensure the URL ends with a trailing slash `/`.

---

## 📂 Project Architecture

```
voxa-android/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/voxa/music/
│   │   │   ├── data/
│   │   │   │   ├── local/          # SharedPreferences TokenManager
│   │   │   │   ├── model/          # Song, Playlist, HomeFeed, Auth models
│   │   │   │   ├── remote/         # Retrofit VoxaApiService & AuthInterceptor
│   │   │   │   └── repository/     # MusicRepository & AuthRepository
│   │   │   ├── playback/
│   │   │   │   ├── PlaybackState.kt
│   │   │   │   ├── PlaybackManager.kt
│   │   │   │   └── VoxaMediaService.kt  # AndroidX MediaSessionService
│   │   │   ├── ui/
│   │   │   │   ├── components/     # TrackRow, MiniPlayerBar, LiveLyricsRibbon
│   │   │   │   ├── navigation/     # NavDestination & VoxaApp Scaffold
│   │   │   │   ├── screens/        # HomeScreen, SearchScreen, LibraryScreen, PlayerScreen, AuthScreen
│   │   │   │   └── theme/          # Voxa Design System (Color, Theme)
│   │   │   ├── util/
│   │   │   │   ├── Constants.kt    # Base URL configuration
│   │   │   │   └── LrcParser.kt    # Live synced lyrics engine
│   │   │   ├── MainActivity.kt
│   │   │   └── VoxaApplication.kt
│   │   └── res/                    # Colors, Themes, Drawables, Strings
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml          # Centralized version catalog
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🏃 Running the Application

1. **Open in Android Studio**:
   - Launch **Android Studio** (Koala / Ladybug or newer recommended).
   - Select **Open** and choose the `voxa-android` directory (`c:\Users\Vishal\.gemini\antigravity-ide\scratch\music-streaming-app\voxa-android`).
2. **Gradle Sync**:
   - Android Studio will automatically sync dependencies from `libs.versions.toml`.
3. **Run on Device / Emulator**:
   - Select an Android Virtual Device (AVD running API 26+) or connect your physical Android phone via USB debugging.
   - Click **Run 'app'** (`Shift + F10`).
