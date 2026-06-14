# LocalReels — Android Video Player

A local Reels/Shorts-style video player for Android. No internet required.

## Features
- Pick any folder from your device
- Swipe up/down to move between videos (like Instagram Reels)
- Fullscreen playback with ExoPlayer (hardware-accelerated)
- Preloads next/prev video for instant swipe response
- Remembers recent folders (Room database)
- Mute, play/pause, share controls

## Project structure

```
app/src/main/java/com/localreels/
├── data/
│   ├── Models.kt          # VideoFolder, VideoItem, PlaybackState
│   ├── Database.kt        # Room DB + DAO
│   └── VideoRepository.kt # MediaStore scanner + folder persistence
├── player/
│   └── VideoPlayerManager.kt  # ExoPlayer singleton wrapper
├── ui/
│   ├── PlayerViewModel.kt     # State management (StateFlow)
│   ├── FolderPickerScreen.kt  # Screen 1: pick a folder
│   └── PlayerScreen.kt        # Screen 2: swipe video player
├── AppModule.kt           # Hilt DI bindings
├── LocalReelsApp.kt       # @HiltAndroidApp
└── MainActivity.kt        # Nav graph entry point
```

## Tech stack
| Layer | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Video player | Media3 ExoPlayer |
| Swipe pager | `VerticalPager` (Compose Foundation) |
| State | ViewModel + StateFlow |
| Database | Room |
| DI | Hilt |
| Thumbnails | Coil + coil-video |
| Permissions | ActivityResultContracts |

## Setup

1. Open in Android Studio Hedgehog or newer.
2. Sync Gradle. All dependencies pull from Maven Central / Google Maven.
3. Run on a device or emulator with Android 8+ (API 26+).
4. Grant storage permission when prompted on first launch.

## Key decisions

**One ExoPlayer instance** — `VideoPlayerManager` is a Hilt `@Singleton`. A single player is
reused across all videos so there's no startup latency on each swipe.

**Preloading** — When the active page changes, the next and previous `MediaItem`s are prepared
in the background so swiping feels instant.

**VerticalPager** — Uses `androidx.compose.foundation.pager.VerticalPager` with snap scrolling,
exactly how Instagram/YouTube Shorts work under the hood.

**MediaStore** — Videos are read directly via `ContentResolver` — no file copying, no temp
files. Works with scoped storage on Android 10+.

**Room for recent folders only** — `VideoItem` is never persisted; it's always freshly scanned
from `MediaStore` to stay in sync with what's actually on disk.
