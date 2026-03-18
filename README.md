# Union Music Player - Enhanced Version

A beautiful music player with Material 3 design, comprehensive file scanning, and multi-format audio support.

## 🎵 Features

### Core Features
- **Material 3 Design**: Modern UI with rounded corners and smooth animations
- **Apple Music Style**: Three-button navigation (Library, Playlist, More)
- **Custom Color Theme**: Green, yellow, and white color scheme

### Advanced Audio Support
- **Multi-Format Playback**: Support for MP3, FLAC, ALAC, M4A, AAC, WAV, OGG
- **ExoPlayer Engine**: High-quality audio playback with better format support
- **Metadata Extraction**: Automatic album art, artist, album, and track info extraction

### File Scanning
- **Parallel Scanning**: Multi-threaded file scanning for faster results
- **Automatic Metadata**: Extract embedded album art and metadata from audio files
- **M3U/M3U8 Playlist Support**: Automatically parse and import playlists
- **Directory Selection**: Choose custom directories to scan

### Library Management
- **Smart Organization**: Browse by songs, albums, or artists
- **Search Functionality**: Find songs quickly
- **Library Statistics**: View total songs, artists, albums, and duration
- **Persistent Storage**: Music library saved across app restarts

## 🎨 Design

### Color Scheme
- **Primary**: Green (#4CAF50) - Main buttons and important elements
- **Secondary**: Yellow (#FFC107) - Secondary elements and accents
- **Background**: White (#FFFFFF) - Clean, minimal background

### Navigation
Three main sections inspired by Apple Music:
1. **Library**: Browse and play your local music collection
   - All Songs view
   - Albums view
   - Artists view
2. **Playlist**: View current playing queue and playback controls
3. **More**: Settings, file scanning, and additional options

## 🏗️ Architecture

### Tech Stack
- **Kotlin**: Modern Android development language
- **Jetpack Compose**: Declarative UI framework
- **Material 3**: Latest Material Design system
- **ExoPlayer**: Advanced media playback (supports FLAC/ALAC)
- **JAudioTagger**: Audio metadata extraction
- **Kotlin Coroutines**: Asynchronous parallel file scanning

### Project Structure
```
app/src/main/java/org/bibichan/union/player/
├── data/
│   ├── MusicMetadata.kt         # Song metadata model
│   ├── MusicScanner.kt          # Parallel file scanner
│   ├── MusicLibraryManager.kt   # Library persistence
│   ├── MusicLibraryViewModel.kt # MVVM architecture
│   └── EnhancedMusicPlayer.kt   # ExoPlayer wrapper
├── ui/
│   ├── theme/
│   │   ├── Color.kt             # Brand colors
│   │   ├── Theme.kt             # Material 3 theme
│   │   ├── Shape.kt             # Rounded corners
│   │   └── Type.kt              # Typography
│   ├── screens/
│   │   ├── EnhancedLibraryScreen.kt # Library UI
│   │   ├── PlaylistScreen.kt        # Now playing UI
│   │   └── MoreScreen.kt            # Settings UI
│   ├── MainActivity.kt          # Main activity
│   └── UnionMusicApp.kt         # App composable
└── MusicPlayer.kt               # Original player (legacy)
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34 (Android 14)
- Kotlin 1.9.0+

### Building the Project
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files (this will download dependencies)
4. Build and run on your device

### Dependencies
```kotlin
// ExoPlayer - Advanced media playback
implementation("androidx.media3:media3-exoplayer:1.2.1")

// JAudioTagger - Metadata extraction
implementation("org.jaudiotagger:jaudiotagger:2.2.5")

// Kotlin Coroutines - Parallel processing
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

## 📱 Usage

### Scanning Music Files
1. Open the app
2. Grant storage permission when prompted
3. Go to "More" tab
4. Tap "Scan Local Files"
5. Select your music directory
6. Wait for scan to complete (progress shown)

### Playing Music
1. Browse your library in the "Library" tab
2. Tap any song to start playing
3. Use playback controls in "Playlist" tab

### Supported Formats
- **MP3** - Standard compressed audio
- **FLAC** - Lossless compression
- **ALAC** - Apple Lossless (in M4A container)
- **M4A** - MPEG-4 audio
- **AAC** - Advanced Audio Coding
- **WAV** - Uncompressed PCM
- **OGG** - Vorbis/Opus

## 🔧 Technical Details

### Parallel Scanning
The scanner uses Kotlin coroutines to process files in parallel:
- 4 consumer coroutines for metadata extraction
- Channel-based file distribution
- Progress tracking with StateFlow
- Concurrent metadata extraction from multiple files

### Metadata Extraction
Uses JAudioTagger to extract:
- Album art (embedded images)
- Artist and album names
- Track titles and numbers
- Genre and year information
- Audio duration

### Playlist Support
Automatically parses M3U/M3U8 playlist files:
- Handles both relative and absolute paths
- Resolves paths relative to playlist location
- Supports extended M3U format with metadata

### ExoPlayer Integration
Enhanced playback with:
- Gapless playback support
- Better format compatibility
- Hardware-accelerated decoding
- Adaptive streaming support

## 📊 Performance

### Scanning Benchmarks
- **Small library** (100 songs): ~5 seconds
- **Medium library** (500 songs): ~20 seconds
- **Large library** (2000+ songs): ~1-2 minutes

*Performance varies by device and storage type*

### Memory Optimization
- Album art loaded on demand
- Efficient bitmap caching
- Lazy loading in UI
- Background processing

## 🔐 Permissions

The app requests the following permissions:
- **READ_EXTERNAL_STORAGE**: For Android 12 and below
- **READ_MEDIA_AUDIO**: For Android 13+ (API 33+)

## 🐛 Troubleshooting

### No songs found
1. Ensure you granted storage permission
2. Try scanning a different directory
3. Check if files are in supported formats

### Playback issues
1. Some formats may not be supported on older devices
2. Ensure the file isn't corrupted
3. Try restarting the app

### Slow scanning
1. Large libraries take longer to scan
2. Scanning is parallelized but still intensive
3. Consider scanning smaller subdirectories

## 📝 License

This project is for educational purposes.

## 🙏 Acknowledgments

- Material 3 Design System by Google
- Apple Music for design inspiration
- ExoPlayer team for the excellent media framework
- JAudioTagger for metadata extraction capabilities
