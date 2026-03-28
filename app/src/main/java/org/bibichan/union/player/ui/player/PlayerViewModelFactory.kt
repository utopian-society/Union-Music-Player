package org.bibichan.union.player.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.bibichan.union.player.MusicPlayer

class PlayerViewModelFactory(
    private val musicPlayer: MusicPlayer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlayerViewModel(musicPlayer) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
