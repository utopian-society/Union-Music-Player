package org.bibichan.union.player

import android.content.Context
import android.media.MediaPlayer

class MusicPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var currentSongIndex = -1
    private var songs: List<Song> = emptyList()

    fun setSongs(songs: List<Song>) {
        this.songs = songs
    }

    fun play(songIndex: Int) {
        if (songIndex < 0 || songIndex >= songs.size) return

        currentSongIndex = songIndex
        val song = songs[currentSongIndex]

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, song.resourceId)
        mediaPlayer?.start()
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun resume() {
        mediaPlayer?.start()
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    fun next() {
        if (songs.isEmpty()) return
        val nextSongIndex = (currentSongIndex + 1) % songs.size
        play(nextSongIndex)
    }

    fun previous() {
        if (songs.isEmpty()) return
        val previousSongIndex = if (currentSongIndex - 1 < 0) songs.size - 1 else currentSongIndex - 1
        play(previousSongIndex)
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun seekTo(progress: Int) {
        mediaPlayer?.seekTo(progress)
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getCurrentSong(): Song? {
        return if (currentSongIndex != -1) {
            songs[currentSongIndex]
        } else {
            null
        }
    }
}
