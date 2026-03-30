# Union Music Player - Changelog

## [Unreleased] - 2026-03-30

### 🔧 Fixed - Gradle Build Errors

#### Problems
1. **Gradle Wrapper Error**: "Could not find or load main class org.gradle.wrapper.GradleWrapperMain"
2. **Kotlin Plugin Conflict**: "Cannot add extension with name 'kotlin'"
3. **Compose Compiler Missing**: "Starting in Kotlin 2.0, the Compose Compiler Gradle plugin is required"
4. **JVM Target Error**: "Unknown Kotlin JVM target: 25"
5. **Compilation Errors**: Missing imports in UI components

#### Root Causes
- `gradle/wrapper/` directory was completely missing
- `org.jetbrains.kotlin.android` plugin was incorrectly applied (AGP 9.0+ has built-in Kotlin)
- Kotlin 2.0+ requires separate Compose Compiler plugin for Jetpack Compose
- **Kotlin 2.0.0 does not support JVM target 25** (max is JVM 24)
- Wildcard imports (`import androidx.compose.*`) caused unresolved references

#### Solution Implemented

**1. Created Gradle Wrapper Configuration**
- Created `gradle/wrapper/gradle-wrapper.properties`
- Configured Gradle 9.4.1 (latest stable, released March 2026)
- Downloaded `gradle-wrapper.jar` (48,966 bytes) from official Gradle repository

**2. Created Root Build Configuration**
- Created `build.gradle.kts` at project root
- Configured plugins:
  - `com.android.application` version 9.1.0
  - `org.jetbrains.kotlin.android` version **2.3.0** (required for JVM target 25)
  - `org.jetbrains.kotlin.plugin.compose` version **2.3.0**
- All plugins use `apply false` for root-level declaration

**3. Created Settings Configuration**
- Created `settings.gradle.kts`
- Configured plugin repositories (Google, Maven Central, Gradle Plugin Portal)
- Set project name to "UnionMusicPlayer"
- Included `:app` module

**4. Updated App Module Configuration**
- Updated `app/build.gradle.kts` for AGP 9.1.0 compatibility
- Applied plugins:
  - `id("com.android.application")` - inherited from root
  - `id("org.jetbrains.kotlin.plugin.compose")` version 2.3.0
- Added `kotlin {}` block with explicit JVM target 25:
  ```kotlin
  kotlin {
      compilerOptions {
          jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
      }
  }
  ```
- Updated `compileOptions` to use **JDK 25**:
  ```kotlin
  sourceCompatibility = JavaVersion.VERSION_25
  targetCompatibility = JavaVersion.VERSION_25
  ```
- Updated dependencies to latest versions:
  - `androidx.core:core-ktx:1.15.0`
  - `androidx.lifecycle:lifecycle-runtime-ktx:2.8.7`
  - `androidx.activity:activity-compose:1.10.0`
  - `androidx.compose.material3:material3:1.3.1`
  - `androidx.compose.ui:*:1.7.8`
  - `androidx.navigation:navigation-compose:2.8.8`
  - `io.coil-kt:coil-compose:2.7.0`

**5. Created gradle.properties**
- Configured JVM arguments: `-Xmx2048m -Dfile.encoding=UTF-8`
- Enabled AndroidX: `android.useAndroidX=true`
- Enabled configuration cache: `org.gradle.configuration-cache=true`
- Enabled non-transitive R class: `android.nonTransitiveRClass=true`

**6. Fixed UI Component Compilation Errors**

*AlbumCard.kt:*
- Replaced wildcard import with explicit imports
- Added missing `import androidx.compose.foundation.layout.aspectRatio`
- Added missing `import androidx.compose.material3.Text`
- **Fixed `aspectRatio` usage**: Moved from standalone parameter to modifier chain (`.aspectRatio(1f)`)

*UnionBottomNavigation.kt:*
- Added missing `import androidx.compose.ui.unit.dp`

*UnionTopAppBar.kt:*
- Added `@OptIn(ExperimentalMaterial3Api::class)` annotation to suppress experimental API warnings

**7. Updated GitHub Actions Workflows**

*build_release_apk.yml:*
- JDK 25 configured (user preference)
- Updated Android SDK components to API 36, build-tools 36.0.0
- Added Gradle wrapper validation step
- **Fixed APK signing configuration:**
  - Added `signingConfigs` block to `app/build.gradle.kts`
  - **Moved `signingConfigs` before `buildTypes`** (required by Gradle)
  - **Removed duplicate `debug` signing config** (AGP auto-creates it)
  - Added step to setup signing properties from secrets
  - Build steps now use `--stacktrace` for better error reporting
  - Added APK output discovery using `find` command
  - Improved release artifact preparation with error handling
  - Added debug APK build step with output listing
  - Added secret validation and debugging output

*build_ffmpeg_decoder.yml:*
- Updated NDK to 28.2.13676358, CMake to 3.31.6
- Added Gradle wrapper validation step

### 📚 Learning Points

#### AGP 9.0+ Built-in Kotlin Support
Starting with Android Gradle Plugin 9.0, Kotlin support is **built-in**:
- ✅ **Remove** `id("org.jetbrains.kotlin.android")` from module `build.gradle.kts`
- ❌ Applying it separately causes: `Cannot add extension with name 'kotlin'`

#### Kotlin 2.0+ Compose Compiler Plugin
From Kotlin 2.0, Compose Compiler is **separate** from the Kotlin plugin:
- ✅ **Apply** `id("org.jetbrains.kotlin.plugin.compose")` in modules using Compose
- ✅ Version must match Kotlin version (e.g., 2.3.0)
- ❌ No need for `kotlinCompilerExtensionVersion` in `composeOptions` block

#### Kotlin JVM Target 25 Support
**Kotlin 2.3.0** is required for JVM target 25:
- Kotlin 2.0.x - 2.2.x: Max JVM target = 24
- Kotlin 2.3.0+: JVM target 25 supported

Configure using the new `compilerOptions` DSL:
```kotlin
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_25
    }
}
```

#### Plugin Configuration Summary

| Plugin | Where to Apply | Version | Purpose |
|--------|---------------|---------|---------|
| `com.android.application` | Root (`apply false`) + App module | 9.1.0 | Android build |
| `org.jetbrains.kotlin.android` | Root only (`apply false`) | 2.3.0 | Kotlin support (built-in to AGP 9.0+) |
| `org.jetbrains.kotlin.plugin.compose` | Root (`apply false`) + App module | 2.3.0 | Compose Compiler for Kotlin 2.0+ |

#### JDK Version Selection
| JDK Version | Kotlin Requirement | AGP Requirement | Use Case |
|-------------|-------------------|-----------------|----------|
| JDK 17 | 1.8+ | 8.0+ | Stable, LTS (minimum for AGP 9.0+) |
| JDK 21 | 1.9+ | 8.2+ | LTS, recommended for production |
| JDK 24 | 2.1+ | 9.0+ | Non-LTS |
| JDK 25 | **2.3.0+** | 9.0+ | Latest features, non-LTS (your choice) |

#### Version Compatibility Matrix (2026)
| Component | Version | Requirement |
|-----------|---------|-------------|
| Android Gradle Plugin | 9.1.0 | Requires Gradle 9.3.1+ |
| Gradle | 9.4.1 | Latest stable (March 2026) |
| Kotlin | **2.3.0** | Required for JVM target 25 |
| Compose Compiler | 2.3.0 | Matches Kotlin version |
| JDK | 25 | User selected |
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
