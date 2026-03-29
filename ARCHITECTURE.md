# Union Music Player - Architecture Documentation

## 📁 Complete Project Structure

```
app/src/main/java/org/bibichan/union/player/
│
├── data/                           # Data Layer
│   └── MusicModels.kt              # Data classes (Song, Album, Screen, PlayerState)
│
├── ui/                             # UI Layer
│   ├── MainActivity.kt             # App entry point (Android Activity)
│   ├── UnionMusicApp.kt            # Main app composable (root of UI tree)
│   │
│   ├── theme/                      # Theme System
│   │   ├── Color.kt                # Color definitions
│   │   ├── Type.kt                 # Typography styles
│   │   └── Theme.kt                # Theme wrapper (MusicTheme)
│   │
│   ├── components/                 # Reusable UI Components
│   │   ├── MiniPlayer.kt           # Bottom mini player bar
│   │   ├── AlbumCard.kt            # Album grid card
│   │   ├── UnionTopAppBar.kt       # Top app bar (header)
│   │   └── UnionBottomNavigation.kt # Bottom navigation tabs
│   │
│   └── screens/                    # Screen-level Components
│       ├── LibraryScreen.kt        # Library page (album grid)
│       └── MoreScreen.kt           # More page (settings list)
│
└── viewmodel/                      # ViewModel Layer (future)
    └── (to be implemented)
```

---

## 🔄 App Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    APP LAUNCH                                │
│                                                              │
│  Android System → MainActivity.onCreate()                   │
│                      ↓                                       │
│                  setContent { }                              │
│                      ↓                                       │
│                MusicTheme { }                                │
│                      ↓                                       │
│              UnionMusicApp()                                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│              UNIONMUSICAPP STRUCTURE                         │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │              Scaffold (Layout Frame)                │    │
│  │                                                      │    │
│  │  ┌────────────────────────────────────────────┐    │    │
│  │  │         topBar = UnionTopAppBar()           │    │    │
│  │  │         🎵 音乐播放器                        │    │    │
│  │  └────────────────────────────────────────────┘    │    │
│  │                                                      │    │
│  │  ┌────────────────────────────────────────────┐    │    │
│  │  │           Content Area                     │    │    │
│  │  │                                            │    │    │
│  │  │   when (currentRoute):                     │    │    │
│  │  │   ┌──────────────┐  ┌──────────────┐      │    │    │
│  │  │   │ "library"    │  │ "more"       │      │    │    │
│  │  │   │ LibraryScreen│  │ MoreScreen   │      │    │    │
│  │  │   │ (Album Grid) │  │ (Settings)   │      │    │    │
│  │  │   └──────────────┘  └──────────────┘      │    │    │
│  │  │                                            │    │    │
│  │  └────────────────────────────────────────────┘    │    │
│  │                                                      │    │
│  │  ┌────────────────────────────────────────────┐    │    │
│  │  │   bottomBar = Column {                     │    │    │
│  │  │     MiniPlayer()                           │    │    │
│  │  │     UnionBottomNavigation()                │    │    │
│  │  │   }                                        │    │    │
│  │  └────────────────────────────────────────────┘    │    │
│  └────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│              STATE MANAGEMENT FLOW                           │
│                                                              │
│  var currentRoute by mutableStateOf("library")              │
│                      ↓                                       │
│  ┌──────────────────────────────────────────────┐           │
│  │  User taps "More" tab                        │           │
│  │      ↓                                       │           │
│  │  onNavigate("more") called                   │           │
│  │      ↓                                       │           │
│  │  currentRoute = "more"                       │           │
│  │      ↓                                       │           │
│  │  Compose detects state change                │           │
│  │      ↓                                       │           │
│  │  UI Recomposes                               │           │
│  │      ↓                                       │           │
│  │  when (currentRoute) now matches "more"      │           │
│  │      ↓                                       │           │
│  │  MoreScreen displayed                        │           │
│  └──────────────────────────────────────────────┘           │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎨 Component Hierarchy

```
MusicTheme (Root)
│
└── Surface (Background)
    │
    └── UnionMusicApp
        │
        └── Scaffold
            │
            ├── topBar: UnionTopAppBar
            │   └── TopAppBar
            │       └── Text("🎵 音乐播放器")
            │
            ├── bottomBar: Column
            │   │
            │   ├── MiniPlayer
            │   │   ├── Box (Gradient background)
            │   │   │   └── Row
            │   │   │       ├── Column (Song info)
            │   │   │       │   ├── Text (Title)
            │   │   │       │   └── Text (Artist)
            │   │   │       └── Row (Buttons)
            │   │   │           ├── IconButton (Previous)
            │   │   │           ├── IconButton (Play/Pause)
            │   │   │           └── IconButton (Next)
            │   │
            │   └── UnionBottomNavigation
            │       └── NavigationBar
            │           ├── NavigationBarItem (Library)
            │           └── NavigationBarItem (More)
            │
            └── Content Area
                │
                ├── LibraryScreen (when currentRoute = "library")
                │   └── Column
                │       ├── Text ("精选推荐")
                │       └── LazyVerticalGrid
                │           └── AlbumCard (repeated)
                │               └── Card
                │                   ├── AsyncImage (Cover)
                │                   └── Column (Title, Artist)
                │
                └── MoreScreen (when currentRoute = "more")
                    └── LazyColumn
                        └── ListItem (repeated)
                            ├── Icon
                            ├── Text (Title)
                            └── Icon (Chevron)
```

---

## 📊 Data Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    DATA LAYER                                │
│                                                              │
│  data class Song                                            │
│  ├── id: Long                                               │
│  ├── title: String                                          │
│  ├── artist: String                                         │
│  ├── album: String                                          │
│  ├── duration: Long                                         │
│  ├── coverUrl: String                                       │
│  └── filePath: String                                       │
│                                                              │
│  data class Album                                           │
│  ├── id: Long                                               │
│  ├── title: String                                          │
│  ├── artist: String                                         │
│  ├── coverUrl: String                                       │
│  └── songs: List<Song>                                      │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│                    UI LAYER                                  │
│                                                              │
│  UnionMusicApp holds sample data:                           │
│  val sampleAlbums = listOf(                                 │
│      Album(...),                                            │
│      Album(...),                                            │
│      ...                                                    │
│  )                                                          │
│                                                              │
│  Pass data to screens:                                      │
│  LibraryScreen(                                             │
│      albums = sampleAlbums,                                 │
│      onAlbumClick = { ... }                                 │
│  )                                                          │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔧 Key Technologies Used

| Technology | Purpose | Version |
|------------|---------|---------|
| **Jetpack Compose** | Modern UI toolkit | 1.5.0 |
| **Material 3** | Design system | 1.1.0 |
| **Kotlin** | Programming language | 1.9+ |
| **Coil** | Image loading | 2.4.0 |
| **Navigation Compose** | Screen navigation | 2.6.0 |

---

## 🎯 Component Responsibilities

### MainActivity
- **Purpose**: App entry point
- **Responsibilities**:
  - Initialize Android Activity
  - Enable edge-to-edge display
  - Set up Compose UI with `setContent`
  - Wrap with theme

### UnionMusicApp
- **Purpose**: Root composable
- **Responsibilities**:
  - Manage app state (current screen, player state)
  - Provide Scaffold structure
  - Coordinate between components
  - Hold sample data (for now)

### UnionTopAppBar
- **Purpose**: App header
- **Responsibilities**:
  - Display app title
  - Show brand color (green)
  - Provide consistent top bar

### MiniPlayer
- **Purpose**: Always-visible player controls
- **Responsibilities**:
  - Show current song info
  - Display play/pause/prev/next buttons
  - Handle user interaction callbacks

### UnionBottomNavigation
- **Purpose**: Tab switcher
- **Responsibilities**:
  - Display navigation tabs
  - Show selected state
  - Notify parent of tab changes

### LibraryScreen
- **Purpose**: Main content view
- **Responsibilities**:
  - Display album grid
  - Show "Featured" header
  - Handle album clicks

### MoreScreen
- **Purpose**: Settings/options view
- **Responsibilities**:
  - Display settings list
  - Show menu items with icons
  - Handle item clicks

---

## 🚀 Future Enhancements

1. **ViewModel Implementation**
   - Move state management to ViewModel
   - Add LiveData/StateFlow for reactive UI
   - Separate business logic from UI

2. **Music Scanning**
   - Implement file scanner
   - Extract metadata with JAudioTagger
   - Support M3U playlists

3. **Playback Engine**
   - Integrate ExoPlayer (Media3)
   - Support FLAC/ALAC formats
   - Add gapless playback

4. **Database**
   - Add Room database
   - Persist music library
   - Store play history

5. **Full Player Screen**
   - Full-screen player view
   - Seek bar with progress
   - Lyrics display
   - Equalizer

---

## 📝 Learning Resources

### Jetpack Compose
- Official Guide: https://developer.android.com/jetpack/compose
- Compose Pathways: https://developer.android.com/courses/pathways/compose

### Material 3
- Material 3 Guide: https://m3.material.io
- Compose Material 3: https://developer.android.com/jetpack/compose/material3

### Kotlin
- Kotlin Docs: https://kotlinlang.org/docs/home.html
- Kotlin for Android: https://developer.android.com/kotlin

---

*This documentation was generated to help bibichan understand the UnionMusicPlayer architecture.*
