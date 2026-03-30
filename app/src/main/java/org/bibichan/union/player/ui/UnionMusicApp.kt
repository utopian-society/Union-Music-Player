package org.bibichan.union.player.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.bibichan.union.player.data.Album
import org.bibichan.union.player.data.Song
import org.bibichan.union.player.ui.components.MiniPlayer
import org.bibichan.union.player.ui.components.UnionTopAppBar
import org.bibichan.union.player.ui.screens.LibraryScreen
import org.bibichan.union.player.ui.screens.MoreScreen

/**
 * UnionMusicApp - Main App Composable
 *
 * This is the root composable that combines all components:
 * - TopAppBar (header)
 * - LibraryScreen or MoreScreen (content based on selected tab)
 * - MiniPlayer (always visible above bottom navigation)
 *
 * KEY CONCEPTS:
 *
 * 1. Scaffold:
 *    - Material Design layout structure
 *    - Provides slots for: topBar, bottomBar, content, floatingActionButton
 *    - Automatically handles padding and safe areas
 *
 * 2. State Management with remember:
 *    - remember { mutableStateOf(...) } creates observable state
 *    - When state changes, UI automatically recomposes
 *    - This is how Compose handles dynamic UIs!
 *
 * 3. mutableStateOf:
 *    - Creates a mutable value that Compose can observe
 *    - When value changes, all composables reading it update automatically
 *
 * 4. by keyword:
 *    - val state by mutableStateOf("library")
 *    - Same as: val state = mutableStateOf("library").value
 *    - More concise syntax!
 */
@Composable
fun UnionMusicApp() {
    // ─────────────────────────────────────────────────────
    // STATE MANAGEMENT
    // ─────────────────────────────────────────────────────

    /**
     * currentRoute: Tracks which screen is currently visible
     *
     * - "library" = Show LibraryScreen
     * - "more" = Show MoreScreen
     *
     * remember: Keeps the value across recompositions
     * mutableStateOf: Makes the value observable (UI updates when changed)
     */
    var currentRoute by remember { mutableStateOf("library") }

    /**
     * Sample data for demonstration
     * In a real app, this would come from a database or file scanner
     */
    val sampleAlbums = remember {
        listOf(
            Album(
                id = 1,
                title = "Abbey Road",
                artist = "The Beatles",
                coverUrl = "https://picsum.photos/seed/album1/600/600",
                songs = listOf(
                    Song(title = "Come Together", artist = "The Beatles"),
                    Song(title = "Something", artist = "The Beatles")
                )
            ),
            Album(
                id = 2,
                title = "Thriller",
                artist = "Michael Jackson",
                coverUrl = "https://picsum.photos/seed/album2/600/600",
                songs = listOf(
                    Song(title = "Billie Jean", artist = "Michael Jackson"),
                    Song(title = "Thriller", artist = "Michael Jackson")
                )
            ),
            Album(
                id = 3,
                title = "Dark Side of the Moon",
                artist = "Pink Floyd",
                coverUrl = "https://picsum.photos/seed/album3/600/600",
                songs = listOf(
                    Song(title = "Time", artist = "Pink Floyd"),
                    Song(title = "Money", artist = "Pink Floyd")
                )
            ),
            Album(
                id = 4,
                title = "Back in Black",
                artist = "AC/DC",
                coverUrl = "https://picsum.photos/seed/album4/600/600",
                songs = listOf(
                    Song(title = "Hells Bells", artist = "AC/DC"),
                    Song(title = "Back in Black", artist = "AC/DC")
                )
            ),
            Album(
                id = 5,
                title = "Rumours",
                artist = "Fleetwood Mac",
                coverUrl = "https://picsum.photos/seed/album5/600/600",
                songs = listOf(
                    Song(title = "Dreams", artist = "Fleetwood Mac"),
                    Song(title = "Go Your Own Way", artist = "Fleetwood Mac")
                )
            ),
            Album(
                id = 6,
                title = "Nevermind",
                artist = "Nirvana",
                coverUrl = "https://picsum.photos/seed/album6/600/600",
                songs = listOf(
                    Song(title = "Smells Like Teen Spirit", artist = "Nirvana"),
                    Song(title = "Come As You Are", artist = "Nirvana")
                )
            )
        )
    }

    /**
     * Player state - tracks current song and playback status
     * In a real app, this would be managed by a ViewModel
     */
    var currentSong by remember { mutableStateOf(Song(title = "Demo Song", artist = "Demo Artist")) }
    var isPlaying by remember { mutableStateOf(false) }

    // ─────────────────────────────────────────────────────
    // MAIN APP STRUCTURE (Scaffold)
    // ─────────────────────────────────────────────────────

    /**
     * Scaffold provides the basic Material Design layout structure.
     *
     * Think of it like a frame with predefined slots:
     * - topBar: Slot for app bar at the top
     * - bottomBar: Slot for navigation at the bottom (we're not using this)
     * - content: Main content area (automatically sized between bars)
     * - floatingActionButton: Optional FAB (we're not using this)
     *
     * Scaffold automatically:
     * - Adds padding for system bars (status bar, navigation bar)
     * - Handles keyboard insets
     * - Ensures proper spacing between components
     */
    Scaffold(
        // topBar: The green header at the top
        topBar = {
            UnionTopAppBar()
        }
    ) { paddingValues ->
        // ─────────────────────────────────────────────────────
        // CONTENT AREA (changes based on selected tab)
        // ─────────────────────────────────────────────────────

        /**
         * paddingValues: Inset values from Scaffold
         * We apply these to ensure content doesn't go under system bars
         */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)  // Apply Scaffold's calculated padding
        ) {
            // Show different content based on currentRoute
            when (currentRoute) {
                // ─────────────────────────────────────────────
                // LIBRARY SCREEN
                // ─────────────────────────────────────────────
                "library" -> {
                    LibraryScreen(
                        albums = sampleAlbums,  // Pass sample data
                        onAlbumClick = { album ->
                            // Handle album tap
                            // In a real app: Navigate to album detail screen
                            // For now: Just print to log
                            println("Clicked album: ${album.title} by ${album.artist}")
                        },
                        onNavigate = { newRoute ->
                            // Update currentRoute when user taps a tab in LibraryScreen
                            currentRoute = newRoute
                        }
                    )
                }

                // ─────────────────────────────────────────────
                // MORE SCREEN
                // ─────────────────────────────────────────────
                "more" -> {
                    MoreScreen(
                        onSettingsClick = {
                            // Handle settings tap
                            println("Settings clicked")
                        },
                        onHistoryClick = {
                            // Handle history tap
                            println("Play history clicked")
                        },
                        onNavigate = { newRoute ->
                            // Update currentRoute when user taps a tab in MoreScreen
                            currentRoute = newRoute
                        }
                    )
                }

                // Fallback (shouldn't happen, but good practice)
                else -> {
                    // Default to library if route is unknown
                    LibraryScreen(
                        albums = sampleAlbums,
                        onAlbumClick = { },
                        onNavigate = { }
                    )
                }
            }

            // ─────────────────────────────────────────────────────
            // MINIPLAYER (always visible, above bottom navigation)
            // ─────────────────────────────────────────────────────
            // MiniPlayer is now rendered on top of the screen content
            // using a Box with alignment
            MiniPlayer(
                currentSong = currentSong,
                isPlaying = isPlaying,
                onPlayPause = {
                    // Toggle play/pause state
                    isPlaying = !isPlaying
                    // In a real app, this would call the music player
                },
                onPrevious = {
                    // Go to previous song
                    // In a real app: player.playPrevious()
                },
                onNext = {
                    // Go to next song
                    // In a real app: player.playNext()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 88.dp) // Space for bottom navigation (80dp nav + 8dp margin)
            )
        }
    }
}

/**
 * HOW STATE FLOWS THROUGH THE APP:
 *
 * 1. User taps "More" tab in BottomNavigationBar (inside LibraryScreen)
 *    ↓
 * 2. onNavigate("more") callback is triggered
 *    ↓
 * 3. currentRoute changes to "more"
 *    ↓
 * 4. Compose detects state change → Recomposes UI
 *    ↓
 * 5. when (currentRoute) now matches "more" branch
 *    ↓
 * 6. MoreScreen is displayed instead of LibraryScreen
 *
 * VISUAL STRUCTURE:
 * ┌─────────────────────────────────────────┐
 * │  🎵 音乐播放器              (TopAppBar) │
 * ├─────────────────────────────────────────┤
 * │                                         │
 * │     [LibraryScreen OR MoreScreen]       │ ← Content (changes)
 * │                                         │
 * ├─────────────────────────────────────────┤
 * │  [MiniPlayer - current song + controls] │ ← 16dp radius, 85% white
 * ├─────────────────────────────────────────┤
 * │  ┌─────────────────────────────────┐    │
 * │  │  📚 Library    ⋮ More           │    │ ← 12dp radius, 90% white
 * │  └─────────────────────────────────┘    │
 * └─────────────────────────────────────────┘
 */
