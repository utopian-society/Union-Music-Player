package org.bibichan.union.player.data

/**
 * Union Music Player Data Models
 * 
 * This file defines the data structures used throughout the app.
 * 
 * KEY CONCEPTS:
 * 
 * 1. data class:
 *    - Kotlin feature for classes that hold data
 *    - Automatically generates useful methods:
 *      - toString() → "Song(title=..., artist=...)"
 *      - equals() → Compare two songs by content
 *      - copy() → Create modified copy (immutable!)
 * 
 * 2. val vs var:
 *    - val = immutable (cannot change after creation)
 *    - var = mutable (can change later)
 *    - We use val because song data shouldn't change!
 * 
 * 3. Default values:
 *    - Provide fallback values if not specified
 *    - Makes creating objects easier
 */

// ─────────────────────────────────────────────────────────────
// SONG DATA MODEL
// ─────────────────────────────────────────────────────────────

/**
 * Song represents a single music track.
 * 
 * PROPERTIES:
 * - id: Unique identifier for the song (database primary key)
 * - title: Song name (e.g., "Bohemian Rhapsody")
 * - artist: Performer name (e.g., "Queen")
 * - album: Album name (e.g., "A Night at the Opera")
 * - duration: Song length in milliseconds (e.g., 354000 = 5:54)
 * - coverUrl: Path/URL to album cover image
 * - filePath: Full path to audio file on device
 * 
 * WHY USE DATA CLASS?
 * ```
 * val song1 = Song(title = "My Song", artist = "Artist")
 * val song2 = song1.copy(title = "New Title")  // Create modified copy
 * println(song1)  // Automatically prints: Song(title=My Song, artist=Artist)
 * ```
 */
data class Song(
    val id: Long = 0L,                    // Unique ID (0 = default/empty)
    val title: String = "Unknown Title",  // Song title
    val artist: String = "Unknown Artist", // Artist name
    val album: String = "Unknown Album",   // Album name
    val duration: Long = 0L,              // Duration in milliseconds
    val coverUrl: String = "",            // Album cover image path
    val filePath: String = ""             // File system path
)

// ─────────────────────────────────────────────────────────────
// ALBUM DATA MODEL
// ─────────────────────────────────────────────────────────────

/**
 * Album represents a music album containing multiple songs.
 * 
 * PROPERTIES:
 * - id: Unique identifier
 * - title: Album name (e.g., "Abbey Road")
 * - artist: Main artist or "Various Artists"
 * - coverUrl: Path to album cover image
 * - songs: List of all songs in this album
 * 
 * WHY songs: List<Song> = emptyList()?
 * - List<Song> means "a list containing Song objects"
 * - emptyList() provides an empty list as default
 * - Using emptyList() is better than listOf() for empty collections
 *   (it reuses a shared empty instance, saving memory)
 */
data class Album(
    val id: Long = 0L,
    val title: String = "Unknown Album",
    val artist: String = "Unknown Artist",
    val coverUrl: String = "",
    val songs: List<Song> = emptyList()  // List of songs in this album
)

// ─────────────────────────────────────────────────────────────
// NAVIGATION SCREENS
// ─────────────────────────────────────────────────────────────

/**
 * Screen enum defines all navigable screens in the app.
 * 
 * ENUM (Enumeration):
 * - A type that can only have specific named values
 * - Here: Screen can ONLY be Library or More
 * - Type-safe way to represent screen states
 * 
 * USAGE:
 * ```
 * val currentScreen = Screen.Library
 * if (currentScreen == Screen.Library) {
 *     // Show library content
 * }
 * ```
 */
enum class Screen {
    Library,  // Main library screen (album grid)
    More      // Settings and options screen
}

// ─────────────────────────────────────────────────────────────
// PLAYER STATE
// ─────────────────────────────────────────────────────────────

/**
 * PlayerState holds the current playback state.
 * 
 * This is used by the MiniPlayer and full player screen.
 * 
 * PROPERTIES:
 * - currentSong: The song currently loaded in player
 * - isPlaying: Whether music is currently playing (true) or paused (false)
 * - currentPosition: Current playback position in milliseconds
 * - duration: Total song duration in milliseconds
 * 
 * WHY SEPARATE FROM SONG?
 * - Song = static data (doesn't change)
 * - PlayerState = dynamic state (changes during playback)
 * - Separating them follows "single responsibility" principle
 */
data class PlayerState(
    val currentSong: Song = Song(),       // Currently loaded song
    val isPlaying: Boolean = false,       // Playing or paused
    val currentPosition: Long = 0L,       // Current position in ms
    val duration: Long = 0L               // Total duration in ms
)
