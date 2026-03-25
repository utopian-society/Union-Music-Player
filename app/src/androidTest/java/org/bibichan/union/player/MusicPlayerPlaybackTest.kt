package org.bibichan.union.player

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.bibichan.union.player.data.AudioFormat
import org.bibichan.union.player.data.MusicMetadata
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MusicPlayerPlaybackTest {
    @Test
    fun playRawResourceUpdatesState() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val song = MusicMetadata(
            id = 1L,
            title = "Sample Music 1",
            artist = "Union",
            album = "Samples",
            duration = 0L,
            filePath = "",
            uri = Uri.parse("android.resource://${context.packageName}/${R.raw.sample_music_1}"),
            format = AudioFormat.MP3
        )

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        lateinit var player: MusicPlayer

        instrumentation.runOnMainSync {
            player = MusicPlayer(context)
            player.setSongs(listOf(song))
            player.play(0)
        }

        val currentSong = player.currentSongFlow.value
        assertEquals("Sample Music 1", currentSong?.title)
        assertTrue(player.isPlayingFlow.value)

        instrumentation.runOnMainSync {
            player.release()
        }
    }
}
