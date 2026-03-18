/**
 * PlaylistScreen.kt - 播放列表界面
 *
 * 显示当前播放队列和播放控制界面。
 */
package org.bibichan.union.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.bibichan.union.player.MusicPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    musicPlayer: MusicPlayer
) {
    val currentSong by remember { mutableStateOf(musicPlayer.getCurrentSong()) }
    val isPlaying by remember { mutableStateOf(musicPlayer.isPlaying()) }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Now Playing",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 专辑封面占位符
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .aspectRatio(1f),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 8.dp
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Album Cover",
                    modifier = Modifier.padding(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 歌曲信息
            currentSong?.let { song ->
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } ?: run {
                Text(
                    text = "No song playing",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 播放控制按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 上一首
                FilledIconButton(
                    onClick = {
                        musicPlayer.previous()
                    },
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // 播放/暂停
                FilledIconButton(
                    onClick = {
                        if (musicPlayer.isPlaying()) {
                            musicPlayer.pause()
                        } else {
                            musicPlayer.resume()
                        }
                    },
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (musicPlayer.isPlaying()) 
                            Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (musicPlayer.isPlaying()) "Pause" else "Play",
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                // 下一首
                FilledIconButton(
                    onClick = {
                        musicPlayer.next()
                    },
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
