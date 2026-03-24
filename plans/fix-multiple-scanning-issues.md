# Fix Multiple Scanning Issues - Implementation Plan

## Overview

This plan addresses five critical issues in the Union Music Player app related to file scanning, metadata extraction, persistence, and UI navigation.

---

## Issue Analysis & Root Causes

### Issue 1: Previous Index Not Cleared Before New Scan

**Problem:** When re-scanning a folder, old songs remain in the library alongside new ones.

**Root Cause:** In [`ScannedFilesManager.addScannedFolder()`](app/src/main/java/org/bibichan/union/player/data/ScannedFilesManager.kt:142), when a folder is re-scanned:
- The folder metadata is updated (line 157-158)
- But `_songsByFolder` value is **appended** not replaced: `_songsByFolder.value = _songsByFolder.value + (folderUri to songs)` (line 165)
- This causes duplicate songs when the same folder is scanned again

**Fix Location:** [`ScannedFilesManager.kt`](app/src/main/java/org/bibichan/union/player/data/ScannedFilesManager.kt)

---

### Issue 2: Album Art Not Displaying

**Problem:** Library screen shows artist and album but no album arts.

**Root Cause:** In [`MusicScanner.processDocumentFile()`](app/src/main/java/org/bibichan/union/player/data/MusicScanner.kt:695):
- Album art extraction is **explicitly skipped** (lines 726-727):
  ```kotlin
  // 跳過專輯封面提取以減少內存使用和處理時間
  // 專輯封面將在播放時按需加載
  // val albumArt = extractAlbumArt(retriever)
  ```
- `albumArt` is set to `null` (line 740)
- There's **no on-demand loading mechanism** implemented

**Fix Location:** [`MusicScanner.kt`](app/src/main/java/org/bibichan/union/player/data/MusicScanner.kt), [`Album.kt`](app/src/main/java/org/bibichan/union/player/ui/library/data/Album.kt)

---

### Issue 3: Library Empty After App Restart

**Problem:** After scanning, closing and reopening the app shows an empty library.

**Root Cause:** In [`ScannedFilesManager.loadFromPreferences()`](app/src/main/java/org/bibichan/union/player/data/ScannedFilesManager.kt:235):
- Only folder metadata (`ScannedFolder`) is loaded from SharedPreferences
- Songs are **NOT persisted** because `MusicMetadata` contains `Bitmap` which cannot be serialized to JSON
- Comment on line 249 explicitly states: `// 注意：歌曲資料需要在實際掃描時重新載入`

**Fix Location:** [`ScannedFilesManager.kt`](app/src/main/java/org/bibichan/union/player/data/ScannedFilesManager.kt), [`MainActivity.kt`](app/src/main/java/org/bibichan/union/player/ui/MainActivity.kt)

---

### Issue 4: File Explorer Not Hierarchical

**Problem:** Files screen shows all files in a flat list, not a directory tree structure.

**Root Cause:** In [`FilesScreen.kt`](app/src/main/java/org/bibichan/union/player/ui/screens/FilesScreen.kt):
- When entering a folder, `SongListView` displays all songs in a flat `LazyColumn` (lines 279-303)
- [`FilesViewModel`](app/src/main/java/org/bibichan/union/player/ui/screens/FilesViewModel.kt) doesn't build or navigate a directory tree
- The `FileTreeNode` data class exists in [`ScannedFilesManager.kt`](app/src/main/java/org/bibichan/union/player/data/ScannedFilesManager.kt:65) but is not utilized

**Fix Location:** [`FilesScreen.kt`](app/src/main/java/org/bibichan/union/player/ui/screens/FilesScreen.kt), [`FilesViewModel.kt`](app/src/main/java/org/bibichan/union/player/ui/screens/FilesViewModel.kt)

---

### Issue 5: Song Player Not Showing on File Tap

**Problem:** Tapping an audio file doesn't show the floating player widget.

**Root Cause:** In [`FloatingPlayer.kt`](app/src/main/java/org/bibichan/union/player/ui/components/FloatingPlayer.kt):
- The `currentSong` state is not reactive: `val currentSong by remember { mutableStateOf(musicPlayer.getCurrentSong()) }` (line 34)
- `remember` without `key` captures the initial value and never updates
- The `AnimatedVisibility` in [`UnionMusicApp.kt`](app/src/main/java/org/bibichan/union/player/ui/UnionMusicApp.kt:210) depends on `musicPlayer.getCurrentSong() != null` but the Compose runtime isn't notified of state changes

**Fix Location:** [`FloatingPlayer.kt`](app/src/main/java/org/bibichan/union/player/ui/components/FloatingPlayer.kt), [`MusicPlayer.kt`](app/src/main/java/org/bibichan/union/player/MusicPlayer.kt)

---

## Implementation Plan

### Phase 1: Fix Clear Previous Index

**File:** [`ScannedFilesManager.kt`](app/src/main/java/org/bibichan/union/player/data/ScannedFilesManager.kt)

**Changes:**
1. Modify `addScannedFolder()` method to **replace** songs instead of appending:
   ```kotlin
   fun addScannedFolder(folderUri: Uri, name: String, path: String, songs: List<MusicMetadata>) {
       // ... existing folder creation code ...
       
       // Clear existing songs for this folder first (if any)
       val existingFolder = _scannedFolders.value.find { it.uri == folderUri }
       
       // Update songsByFolder - REPLACE instead of APPEND
       _songsByFolder.value = _songsByFolder.value + (folderUri to songs)
       
       // ... rest of the code ...
   }
   ```

2. Add a `clearFolderSongs()` helper method:
   ```kotlin
   fun clearFolderSongs(folderUri: Uri) {
       _songsByFolder.value = _songsByFolder.value - folderUri
       updateScannedSongs()
   }
   ```

---

### Phase 2: Fix Album Art Extraction

**File:** [`MusicScanner.kt`](app/src/main/java/org/bibichan/union/player/data/MusicScanner.kt)

**Changes:**
1. Re-enable album art extraction in `processDocumentFile()`:
   ```kotlin
   private fun processDocumentFile(
       documentFile: androidx.documentfile.provider.DocumentFile,
       context: Context
   ): MusicMetadata? {
       // ... existing code ...
       
       val retriever = MediaMetadataRetriever()
       
       return try {
           retriever.setDataSource(context, documentFile.uri)
           
           // Extract metadata including album art
           val albumArt = extractAlbumArt(retriever)
           
           MusicMetadata(
               // ... other fields ...
               albumArt = albumArt,  // Now includes album art
               // ... rest of fields ...
           )
       } catch (e: Exception) {
           // ... error handling ...
       } finally {
           retriever.release()
       }
   }
   ```

2. Optimize `extractAlbumArt()` to use downscaled bitmap for memory efficiency:
   ```kotlin
   private fun extractAlbumArt(retriever: MediaMetadataRetriever): Bitmap? {
       return try {
           val imageData = retriever.embeddedPicture
           if (imageData != null) {
               // Decode with sample size for memory efficiency
               val options = BitmapFactory.Options().apply {
                   inSampleSize = 2  // Scale down to 1/2 size
               }
               BitmapFactory.decodeByteArray(imageData, 0, imageData.size, options)
           } else {
               null
           }
       } catch (e: Exception) {
           Log.w(TAG, "Error extracting album art", e)
           null
       }
   }
   ```

---

### Phase 3: Fix Library Persistence

**File:** [`ScannedFilesManager.kt`](app/src/main/java/org/bibichan/union/player/data/ScannedFilesManager.kt)

**Changes:**
1. Add song serialization (without Bitmap) to SharedPreferences:
   ```kotlin
   private const val KEY_SONGS_JSON = "songs_json"
   
   private fun saveSongsToPreferences(songs: List<MusicMetadata>) {
       val songsJson = JSONArray()
       songs.forEach { song ->
           songsJson.put(song.toJson())
       }
       prefs.edit()
           .putString(KEY_SONGS_JSON, songsJson.toString())
           .apply()
   }
   
   private fun loadSongsFromPreferences(): List<MusicMetadata> {
       val songsJson = prefs.getString(KEY_SONGS_JSON, null) ?: return emptyList()
       val jsonArray = JSONArray(songsJson)
       val songs = mutableListOf<MusicMetadata>()
       for (i in 0 until jsonArray.length()) {
           songs.add(MusicMetadata.fromJson(jsonArray.getJSONObject(i)))
       }
       return songs
   }
   ```

2. Modify `saveToPreferences()` to also save songs:
   ```kotlin
   private fun saveToPreferences() {
       // ... existing folder saving code ...
       
       // Save all songs (without Bitmap data)
       saveSongsToPreferences(_scannedSongs.value)
   }
   ```

3. Modify `loadFromPreferences()` to restore songs:
   ```kotlin
   private fun loadFromPreferences() {
       // ... existing folder loading code ...
       
       // Load songs
       val savedSongs = loadSongsFromPreferences()
       if (savedSongs.isNotEmpty()) {
           _scannedSongs.value = savedSongs
           // Rebuild songsByFolder from saved data
           rebuildSongsByFolder()
       }
   }
   ```

**File:** [`MainActivity.kt`](app/src/main/java/org/bibichan/union/player/ui/MainActivity.kt)

**Changes:**
1. Add auto-rescan on app start for persisted folders:
   ```kotlin
   override fun onCreate(savedInstanceState: Bundle?) {
       // ... existing initialization ...
       
       // Auto-rescan persisted folders on app start
       lifecycleScope.launch {
           val folders = scannedFilesManager.scannedFolders.value
           if (folders.isNotEmpty()) {
               scannedFilesManager.refreshAllFolders(musicScanner, this@MainActivity)
           }
       }
   }
   ```

---

### Phase 4: Implement Hierarchical File Explorer

**File:** [`FilesViewModel.kt`](app/src/main/java/org/bibichan/union/player/ui/screens/FilesViewModel.kt)

**Changes:**
1. Add directory tree state:
   ```kotlin
   // Current directory path stack for navigation
   private val _currentPath = MutableStateFlow<List<String>>(emptyList())
   val currentPath: StateFlow<List<String>> = _currentPath.asStateFlow()
   
   // Current directory contents (subdirectories and songs)
   private val _currentDirectoryContents = MutableStateFlow<DirectoryContents>(DirectoryContents())
   val currentDirectoryContents: StateFlow<DirectoryContents> = _currentDirectoryContents.asStateFlow()
   
   data class DirectoryContents(
       val directories: List<DirectoryItem> = emptyList(),
       val songs: List<MusicMetadata> = emptyList()
   )
   
   data class DirectoryItem(
       val name: String,
       val path: String,
       val songCount: Int
   )
   ```

2. Implement directory navigation:
   ```kotlin
   fun navigateToDirectory(directoryName: String) {
       _currentPath.value = _currentPath.value + directoryName
       updateDirectoryContents()
   }
   
   fun navigateUp(): Boolean {
       if (_currentPath.value.isEmpty()) return false
       _currentPath.value = _currentPath.value.dropLast(1)
       updateDirectoryContents()
       return true
   }
   
   private fun updateDirectoryContents() {
       val currentFolder = _currentFolderUri.value ?: return
       val songs = scannedFilesManager.getSongsInFolder(currentFolder)
       
       // Build directory tree from song paths
       val currentPathString = _currentPath.value.joinToString("/")
       
       val directories = mutableSetOf<DirectoryItem>()
       val songsInCurrentDir = mutableListOf<MusicMetadata>()
       
       for (song in songs) {
           val relativePath = song.filePath.removePrefix(currentPathString).trim('/')
           val pathParts = relativePath.split("/")
           
           if (pathParts.size == 1) {
               // Song is in current directory
               songsInCurrentDir.add(song)
           } else {
               // Song is in a subdirectory
               val dirName = pathParts.first()
               directories.add(DirectoryItem(
                   name = dirName,
                   path = "$currentPathString/$dirName",
                   songCount = countSongsInDirectory(songs, "$currentPathString/$dirName")
               ))
           }
       }
       
       _currentDirectoryContents.value = DirectoryContents(
           directories = directories.toList().sortedBy { it.name },
           songs = songsInCurrentDir
       )
   }
   ```

**File:** [`FilesScreen.kt`](app/src/main/java/org/bibichan/union/player/ui/screens/FilesScreen.kt)

**Changes:**
1. Update UI to show directories and songs:
   ```kotlin
   @Composable
   fun DirectoryListView(
       directories: List<DirectoryItem>,
       onDirectoryClick: (String) -> Unit
   ) {
       LazyColumn {
           // Directories section
           items(directories) { directory ->
               DirectoryItem(
                   directory = directory,
                   onClick = { onDirectoryClick(directory.name) }
               )
           }
           
           // Songs section
           items(songs) { song ->
               SongItem(song = song, onClick = { /* play song */ })
           }
       }
   }
   ```

---

### Phase 5: Fix Floating Player Visibility

**File:** [`MusicPlayer.kt`](app/src/main/java/org/bibichan/union/player/MusicPlayer.kt)

**Changes:**
1. Add StateFlow for reactive state:
   ```kotlin
   // Add these state flows for Compose observation
   private val _currentSongFlow = MutableStateFlow<MusicMetadata?>(null)
   val currentSongFlow: StateFlow<MusicMetadata?> = _currentSongFlow.asStateFlow()
   
   private val _isPlayingFlow = MutableStateFlow(false)
   val isPlayingFlow: StateFlow<Boolean> = _isPlayingFlow.asStateFlow()
   ```

2. Update flows when state changes:
   ```kotlin
   fun play(songIndex: Int) {
       // ... existing code ...
       
       // Update state flow
       _currentSongFlow.value = song
       _isPlayingFlow.value = true
       
       // ... rest of code ...
   }
   
   fun pause() {
       exoPlayer?.pause()
       _isPlayingFlow.value = false
   }
   
   fun resume() {
       exoPlayer?.play()
       _isPlayingFlow.value = true
   }
   ```

**File:** [`FloatingPlayer.kt`](app/src/main/java/org/bibichan/union/player/ui/components/FloatingPlayer.kt)

**Changes:**
1. Use StateFlow instead of remember:
   ```kotlin
   @Composable
   fun FloatingPlayer(
       musicPlayer: MusicPlayer,
       isVisible: Boolean,
       onExpand: () -> Unit,
       onCollapse: () -> Unit
   ) {
       // Observe StateFlow for reactive updates
       val currentSong by musicPlayer.currentSongFlow.collectAsState()
       val isPlaying by musicPlayer.isPlayingFlow.collectAsState()
       
       // ... rest of the code ...
   }
   ```

**File:** [`UnionMusicApp.kt`](app/src/main/java/org/bibichan/union/player/ui/UnionMusicApp.kt)

**Changes:**
1. Use StateFlow for AnimatedVisibility:
   ```kotlin
   // Observe current song state reactively
   val currentSong by musicPlayer.currentSongFlow.collectAsState()
   
   // Floating player - shows when there's a song
   AnimatedVisibility(
       visible = currentSong != null,
       enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
       exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
   ) {
       FloatingPlayer(
           musicPlayer = musicPlayer,
           isVisible = isPlayerExpanded,
           onExpand = { isPlayerExpanded = true },
           onCollapse = { isPlayerExpanded = false }
       )
   }
   ```

---

## Architecture Diagram

```mermaid
flowchart TB
    subgraph Scanning Flow
        A[User taps Scan Files] --> B[Folder Picker opens]
        B --> C[User selects folder]
        C --> D[MusicScanner.scanDocumentFolder]
        D --> E[Extract metadata + album art]
        E --> F[Return ScanResult]
        F --> G[ScannedFilesManager.addScannedFolder]
        G --> H[Clear old songs for folder]
        H --> I[Store new songs]
        I --> J[Save to SharedPreferences]
    end
    
    subgraph Persistence Flow
        K[App Start] --> L[ScannedFilesManager.init]
        L --> M[loadFromPreferences]
        M --> N[Restore folder list]
        N --> O[Restore songs JSON]
        O --> P[Rebuild songsByFolder]
        P --> Q[LibraryViewModel observes update]
        Q --> R[Library UI shows albums]
    end
    
    subgraph Playback Flow
        S[User taps song in Files] --> T[FilesScreen.playSong]
        T --> U[MusicPlayer.setSongs + play]
        U --> V[_currentSongFlow emits]
        V --> W[UnionMusicApp observes]
        W --> X[FloatingPlayer becomes visible]
    end
    
    subgraph File Explorer Flow
        Y[User opens Files tab] --> Z[FilesViewModel loads folders]
        Z --> AA[User taps folder]
        AA --> AB[Build directory tree]
        AB --> AC[Show subdirectories + songs]
        AC --> AD[User navigates into subdirectory]
        AD --> AB
    end
```

---

## Files to Modify

| File | Changes |
|------|---------|
| [`ScannedFilesManager.kt`](app/src/main/java/org/bibichan/union/player/data/ScannedFilesManager.kt) | Clear old songs, persist songs JSON, load on start |
| [`MusicScanner.kt`](app/src/main/java/org/bibichan/union/player/data/MusicScanner.kt) | Re-enable album art extraction with memory optimization |
| [`MusicPlayer.kt`](app/src/main/java/org/bibichan/union/player/MusicPlayer.kt) | Add StateFlow for reactive state observation |
| [`FloatingPlayer.kt`](app/src/main/java/org/bibichan/union/player/ui/components/FloatingPlayer.kt) | Use collectAsState instead of remember |
| [`UnionMusicApp.kt`](app/src/main/java/org/bibichan/union/player/ui/UnionMusicApp.kt) | Observe StateFlow for FloatingPlayer visibility |
| [`FilesViewModel.kt`](app/src/main/java/org/bibichan/union/player/ui/screens/FilesViewModel.kt) | Add directory tree navigation |
| [`FilesScreen.kt`](app/src/main/java/org/bibichan/union/player/ui/screens/FilesScreen.kt) | Show directories and songs in tree structure |
| [`MainActivity.kt`](app/src/main/java/org/bibichan/union/player/ui/MainActivity.kt) | Auto-rescan persisted folders on start |

---

## Testing Checklist

- [ ] Scan a folder, verify songs appear in Library
- [ ] Scan same folder again, verify no duplicates
- [ ] Verify album art displays in Library
- [ ] Close and reopen app, verify Library persists
- [ ] Navigate Files tab, verify directory tree structure
- [ ] Tap a song in Files, verify FloatingPlayer appears
- [ ] Play a song, verify playback controls work
