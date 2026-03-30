# Union Music Player - Changelog

## [Unreleased] - 2026-03-30

### 🔧 Fixed - Gradle Wrapper Build Error

#### Problem
- GitHub Actions build failed with error: "Could not find or load main class org.gradle.wrapper.GradleWrapperMain"
- Exit code 1 indicated corrupted or missing Gradle wrapper

#### Root Cause
- `gradle/wrapper/` directory was completely missing
- No `gradle-wrapper.jar` or `gradle-wrapper.properties` files present
- Root-level `build.gradle.kts` and `settings.gradle.kts` files were missing

#### Solution Implemented

**1. Created Gradle Wrapper Configuration**
- Created `gradle/wrapper/gradle-wrapper.properties`
- Configured Gradle 9.4.1 (latest stable, released March 2026)
- Set distribution URL to official Gradle services
- Added network timeout and validation settings

**2. Downloaded Gradle Wrapper JAR**
- Downloaded `gradle-wrapper.jar` from official Gradle GitHub repository
- Placed in `gradle/wrapper/` directory
- File size: 48,966 bytes

**3. Created Root Build Configuration**
- Created `build.gradle.kts` at project root
- Configured Android Gradle Plugin 9.1.0 (March 2026 release)
- Configured Kotlin 2.0.0
- Added clean task for build directory management

**4. Created Settings Configuration**
- Created `settings.gradle.kts`
- Configured plugin repositories (Google, Maven Central, Gradle Plugin Portal)
- Configured dependency resolution management
- Set project name to "UnionMusicPlayer"
- Included `:app` module

**5. Updated App Module Configuration**
- Updated `app/build.gradle.kts` for AGP 9.1.0 compatibility
- Changed `compileSdk` from 34 to 36
- Changed `targetSdk` from 34 to 36
- Updated Kotlin compiler extension to 1.5.15
- Updated all AndroidX and Compose dependencies to latest versions:
  - `androidx.core:core-ktx:1.15.0`
  - `androidx.lifecycle:lifecycle-runtime-ktx:2.8.7`
  - `androidx.activity:activity-compose:1.10.0`
  - `androidx.compose.material3:material3:1.3.1`
  - `androidx.compose.ui:*:1.7.8`
  - `androidx.navigation:navigation-compose:2.8.8`
  - `io.coil-kt:coil-compose:2.7.0`

**6. Updated GitHub Actions Workflows**

*build_release_apk.yml:*
- Changed JDK from 25 to 17 (AGP 9.1.0 requirement)
- Updated Android SDK components to API 36
- Updated build-tools to 36.0.0
- Added comprehensive Gradle wrapper validation step:
  - Lists `gradle/wrapper/` directory contents
  - Displays `gradle-wrapper.properties` content
  - Runs `./gradlew --version` for verification

*build_ffmpeg_decoder.yml:*
- Updated NDK from 26.1.10909125 to 28.2.13676358
- Updated CMake from 3.22.1 to 3.31.6
- Updated SDK components to API 36
- Added Gradle wrapper validation step
- Fixed comment to reference JDK 17

### 📚 Learning Points

#### Gradle Wrapper Architecture
- **gradle-wrapper.jar**: Bootstrap class loader that downloads and runs Gradle
- **gradle-wrapper.properties**: Configuration file specifying Gradle version
- **gradlew/graflew.bat**: Shell scripts that invoke the wrapper JAR

#### Version Compatibility Matrix (2026)
| Component | Version | Requirement |
|-----------|---------|-------------|
| Android Gradle Plugin | 9.1.0 | Requires Gradle 9.3.1+ |
| Gradle | 9.4.1 | Latest stable (March 2026) |
| JDK | 17 | Minimum and default for AGP 9.1.0 |
| compileSdk | 36 | Latest Android API |
| build-tools | 36.0.0 | Matches compileSdk |
| NDK | 28.2.x | Latest for native builds |

#### Verification Commands
```bash
# Check wrapper files exist
ls -la gradle/wrapper/

# Verify Gradle version
./gradlew --version

# Clean build test
./gradlew clean build
```

---

## [0.1.0] - 2026-03-29

### 🎨 Added - Complete UI Implementation

#### Theme System (`ui/theme/`)
- **Color.kt**: Defined complete color palette
  - `GreenPrimary` (#4CAF50) - Main brand color
  - `GreenSecondary` (#8BC34A) - Secondary green
  - `YellowAccent` (#FFC107) - Accent color
  - `TranslucentWhite` (0x80FFFFFF) - Glass effect
  - `DarkBackground` and `LightBackground` for theme support
  - Added detailed comments explaining color theory and alpha channels

- **Type.kt**: Created typography system
  - `titleLarge` - For page headers and album titles
  - `titleMedium` - For card titles
  - `bodyLarge` - For main content text
  - `bodySmall` - For secondary/description text
  - `labelSmall` - For navigation labels
  - All styles use Material 3 conventions

- **Theme.kt**: Implemented theme wrapper
  - `MusicTheme` composable function
  - Light and dark color schemes
  - Automatic system theme detection
  - Status bar color management
  - SideEffect for Android view integration

#### Data Models (`data/`)
- **MusicModels.kt**: Core data structures
  - `Song` data class - Represents individual tracks
  - `Album` data class - Represents albums with song lists
  - `Screen` enum - Navigation screen states
  - `PlayerState` data class - Playback state tracking
  - All properties use `val` for immutability
  - Default values provided for all properties

#### UI Components (`ui/components/`)
- **MiniPlayer.kt**: Bottom player bar
  - Song title and artist display
  - Play/Pause/Previous/Next buttons
  - Gradient background with translucency
  - Dynamic icon switching (Play ↔ Pause)
  - Green and yellow color scheme
  - Detailed comments on Modifier usage

- **AlbumCard.kt**: Album grid cards
  - Square aspect ratio (1:1)
  - AsyncImage for album cover loading
  - Rounded corners with clipping
  - Album title and artist display
  - Card elevation (shadow effect)
  - Click interaction handling

- **UnionTopAppBar.kt**: Top app bar
  - Green background (GreenPrimary)
  - White title text
  - Music emoji (🎵) and Chinese title
  - TopAppBarDefaults for theming
  - No action buttons (clean design)

- **UnionBottomNavigation.kt**: Bottom navigation
  - Two tabs: Library (资料库) and More (更多)
  - Different selected colors (Green/Yellow)
  - Icon and label for each tab
  - State-based selection
  - Indicator pill on selection

#### Screen Components (`ui/screens/`)
- **LibraryScreen.kt**: Main library view
  - "精选推荐" (Featured) header
  - LazyVerticalGrid for album display
  - Adaptive grid columns (min 180dp)
  - Spacing and padding configuration
  - Album click handling

- **MoreScreen.kt**: Settings/options view
  - LazyColumn for scrollable list
  - Settings item (⚙️ green)
  - Play History item (🕐 yellow)
  - About item (ℹ️ blue)
  - Help item (❓ purple)
  - Divider separators
  - Chevron trailing icons

#### Main App Structure (`ui/`)
- **UnionMusicApp.kt**: Root composable
  - Scaffold layout structure
  - State management with `mutableStateOf`
  - Sample album data (6 demo albums)
  - Player state tracking
  - Conditional screen rendering
  - Navigation handling
  - Comprehensive comments on state flow

- **MainActivity.kt**: Entry point
  - ComponentActivity subclass
  - `enableEdgeToEdge()` for immersive display
  - setContent with theme wrapping
  - Detailed lifecycle documentation
  - Comparison with old XML approach

#### Android Configuration (`res/`)
- **AndroidManifest.xml**: App manifest
  - Permission declarations
  - READ_EXTERNAL_STORAGE (Android 12-)
  - READ_MEDIA_AUDIO (Android 13+)
  - MainActivity launcher configuration
  - Theme reference

- **values/strings.xml**: String resources
  - App name definition

- **values/themes.xml**: Base theme
  - Material Light NoActionBar parent
  - Status bar color
  - Edge-to-edge configuration

- **values/colors.xml**: Launcher colors
  - Icon background (GreenPrimary)

- **xml/backup_rules.xml**: Backup configuration
  - Shared preferences backup
  - Device-specific exclusion

- **xml/data_extraction_rules.xml**: Data extraction
  - Cloud backup rules for Android 12+

- **mipmap-anydpi-v26/**: Adaptive icons
  - ic_launcher.xml
  - ic_launcher_round.xml

- **drawable/ic_launcher_foreground.xml**: Launcher icon
  - Vector drawable music note
  - White color on green background

#### Documentation
- **ARCHITECTURE.md**: Complete architecture guide
  - Project structure diagram
  - App flow visualization
  - Component hierarchy
  - Data flow explanation
  - Technology stack table
  - Future enhancements roadmap

### 🔧 Technical Details

#### Build Configuration
- Using Kotlin DSL (`.kts` files)
- JDK 25 configured
- Jetpack Compose enabled
- Material 3 dependencies
- Coil for image loading
- Navigation Compose

#### Code Quality
- **100% commented code** - Every function, class, and concept explained
- **Beginner-friendly** - Designed for learning
- **Best practices** - Immutability, separation of concerns
- **Material 3 guidelines** - Following official design system

#### Design Features
- Green and yellow color scheme
- Semi-transparent glass effects
- Rounded corners throughout
- Proper elevation and shadows
- Adaptive layouts
- Dark theme ready

---

## Future Milestones

### [0.2.0] - Planned
- [ ] ViewModel implementation
- [ ] Music file scanning
- [ ] Metadata extraction
- [ ] ExoPlayer integration

### [0.3.0] - Planned
- [ ] Full player screen
- [ ] Seek bar implementation
- [ ] Playlist management
- [ ] Search functionality

### [1.0.0] - Planned
- [ ] Complete music playback
- [ ] Database persistence
- [ ] Settings screen implementation
- [ ] Play history tracking
- [ ] Production release

---

*All code includes detailed educational comments to help bibichan learn Android development.*
