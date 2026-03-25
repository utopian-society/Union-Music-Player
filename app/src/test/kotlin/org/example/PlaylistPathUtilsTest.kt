package org.example

import org.bibichan.union.player.data.MusicMetadata
import org.bibichan.union.player.data.PlaylistPathUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class PlaylistPathUtilsTest {
    @Test
    fun normalizePlaylistEntryStripsBOMAndComments() {
        assertNull(PlaylistPathUtils.normalizePlaylistEntry("#EXTM3U"))
        assertNull(PlaylistPathUtils.normalizePlaylistEntry("  "))
        assertEquals("Music/song.mp3", PlaylistPathUtils.normalizePlaylistEntry("\uFEFFMusic/./song.mp3"))
    }

    @Test
    fun normalizePlaylistEntryDecodesAndNormalizesSeparators() {
        assertEquals("Music/Album/Track 01.flac", PlaylistPathUtils.normalizePlaylistEntry("Music\\Album\\Track%2001.flac"))
    }

    @Test
    fun normalizeRelativePathResolvesDotSegments() {
        assertEquals("Music/Track.mp3", PlaylistPathUtils.normalizeRelativePath("Music/Album/../Track.mp3"))
    }

    @Test
    fun resolveSongFromRelativeIndexMatchesNestedPaths() {
        val song = MusicMetadata(relativePath = "Music/Album/Track01.mp3")
        val index = PlaylistPathUtils.buildRelativePathIndex(listOf(song))

        val resolved = PlaylistPathUtils.resolveSongFromRelativeIndex(
            entry = "Track01.mp3",
            playlistDirPath = "Music/Album",
            playlistDirPathWithoutRoot = "Album",
            relativeIndex = index
        )

        assertNotNull(resolved)
    }
}
