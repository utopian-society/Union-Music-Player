package org.bibichan.union.player

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var musicPlayer: MusicPlayer
    private lateinit var songTitleTextView: TextView
    private lateinit var songArtistTextView: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var playPauseButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        musicPlayer = MusicPlayer(this)

        songTitleTextView = findViewById(R.id.song_title)
        songArtistTextView = findViewById(R.id.song_artist)
        seekBar = findViewById(R.id.seek_bar)
        playPauseButton = findViewById(R.id.play_pause_button)
        previousButton = findViewById(R.id.previous_button)
        nextButton = findViewById(R.id.next_button)

        val songs = listOf(
            Song("Sample Music 1", "Artist 1", R.raw.sample_music_1),
            Song("Sample Music 2", "Artist 2", R.raw.sample_music_2)
        )

        musicPlayer.setSongs(songs)

        playPauseButton.setOnClickListener {
            if (musicPlayer.isPlaying()) {
                musicPlayer.pause()
                playPauseButton.setImageResource(android.R.drawable.ic_media_play)
            } else {
                musicPlayer.resume()
                playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
            }
        }

        previousButton.setOnClickListener {
            musicPlayer.previous()
            updateSongUI()
        }

        nextButton.setOnClickListener {
            musicPlayer.next()
            updateSongUI()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        updateSongUI()
        initializeSeekBar()
    }

    private fun updateSongUI() {
        val currentSong = musicPlayer.getCurrentSong()
        if (currentSong != null) {
            songTitleTextView.text = currentSong.title
            songArtistTextView.text = currentSong.artist
        }
    }

    private fun initializeSeekBar() {
        seekBar.max = musicPlayer.getDuration()

        handler.postDelayed(object : Runnable {
            override fun run() {
                seekBar.progress = musicPlayer.getCurrentPosition()
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }
}
