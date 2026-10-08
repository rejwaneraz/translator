# ReelsLocal

A production-quality, **fully offline** short-video ("reels") app for Android — a local, self-contained
clone of the vertical-feed experience. No backend, no Firebase, no network calls: every user, video,
comment, like, follow and notification lives in an on-device Room database seeded with deterministic
sample content.

Built from scratch with **Kotlin + Jetpack Compose**, following **MVVM + Repository** and clean,
layered architecture.

---

## Features

- **Vertical video feed** (For You / Following) with autoplay, pause-when-off-screen, looping,
  tap-to-pause, double-tap-to-like and mute toggle — powered by a single shared Media3/ExoPlayer.
- **Bottom navigation**: Home, Explore, Inbox, Profile, with a live unread badge on Inbox.
- **Explore** 3-column grid and **Search** across users, videos and hashtags.
- **Profiles** with follower/following/likes stats, Videos / Liked / Saved / Following tabs, and a
  working local follow system.
- **Comments** bottom sheet with pre-seeded Bangla & English comments, likes, pinning and posting.
- **Inbox / activity feed** for likes, comments, follows, saves and shares.
- **Settings**: theme (system/light/dark), playback defaults, and local account switching.
- **Content Manager**: full local CRUD editor for users, videos and comments — create, edit,
  duplicate, delete, pin/hide, plus one-tap **reset & reseed**.
- **Media import**: pick real video/image files from the device (Storage Access Framework) into
  app-private storage — no runtime storage permission required.
- **JSON backup & restore**: export the entire library to a file and restore it atomically.
- **Never crashes on missing media**: bundled sample binaries are optional; absent files render a
  graceful "Video unavailable" placeholder with poster thumbnails.

## Tech stack

| Area | Choice |
|------|--------|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.10.01), Material 3 |
| Architecture | MVVM + Repository, unidirectional data flow |
| Persistence | Room 2.6.1 (single source of truth) |
| Preferences | DataStore (Preferences) |
| DI | Hilt 2.52 |
| Navigation | Navigation-Compose 2.8.4 |
| Media | Media3 / ExoPlayer 1.4.1 |
| Images | Coil 2.7.0 |
| Serialization | kotlinx-serialization-json 1.7.3 |
| Build | Gradle 8.10.2 (Kotlin DSL), AGP 8.7.3, JDK 17 |

`minSdk 26` · `targetSdk/compileSdk 35` · package `com.rejwane.reelslocal`.

## Project structure

```
app/src/main/java/com/rejwane/reelslocal/
├── data/
│   ├── backup/       # Versioned JSON export/restore (DTOs, mapper, repository)
│   ├── database/     # Room entities, DAOs, relations, AppDatabase
│   ├── prefs/        # DataStore settings repository
│   ├── repository/   # Domain repositories + local implementations
│   └── seed/         # Deterministic sample data + reset/reseed
├── di/               # Hilt modules (database, repositories, datastore, initializer)
├── ui/
│   ├── components/   # Reusable Compose UI (bottom nav, action rail, grids, avatars)
│   ├── navigation/   # Nav host + routes
│   ├── player/       # Media3 player holder + surface
│   └── screens/      # home, explore, search, profile, comments, video, inbox,
│                     # settings, content manager, backup
└── utils/            # Formatting, media path resolution, media importer
```

## Building

```bash
./gradlew assembleDebug        # -> app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest    # JVM unit tests
```

Opening in Android Studio (Koala+) and pressing **Run** also works out of the box.

## Adding your own media

Sample videos/images are **not** committed (they'd bloat the repo). The app runs fine without them,
showing placeholders. To use real media:

1. Open **Profile → Edit Profile → Settings → Content Manager → Videos**.
2. Edit (or create) a video and tap **Pick file** next to *Video path* / *Thumbnail path*.
3. The file is copied into app-private storage and its path stored; it plays immediately.

Media paths follow a simple convention: `asset:videos/foo.mp4` for bundled assets, otherwise an
absolute path inside app storage for imported files.

## Continuous integration

GitHub Actions (`.github/workflows/android-ci.yml`) runs on every push and pull request to `main`:

- **build** — JDK 17, runs unit tests + lint, assembles a debug APK and uploads it as an artifact.
- **instrumented** — Compose UI tests on an emulator (manual trigger via `workflow_dispatch`).
- **release** — on a `v*` tag, signs and uploads a release APK.

### Release signing (optional)

Release builds are signed only when these repository **Secrets** are present — never commit keys:

| Secret | Purpose |
|--------|---------|
| `KEYSTORE_BASE64` | Base64-encoded `.jks` keystore |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

Without them, `assembleRelease` falls back to the debug key so the build always succeeds.

## Privacy

ReelsLocal is 100% on-device. It declares no internet permission and makes no network calls. All
content is local sample data you can edit, export, reset or delete at any time.

## License

MIT — see [LICENSE](LICENSE).
