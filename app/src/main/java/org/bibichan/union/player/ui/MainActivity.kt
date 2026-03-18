/**
 * MainActivity.kt - 主界面Activity (Jetpack Compose版本)
 *
 * 这是Union Music Player的主界面入口点，使用Jetpack Compose构建UI。
 * Material 3设计系统，Apple Music风格的三按钮导航。
 */
package org.bibichan.union.player.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import org.bibichan.union.player.MusicPlayer
import org.bibichan.union.player.ui.theme.UnionMusicPlayerTheme

/**
 * MainActivity类 - 应用的主界面
 *
 * 使用Jetpack Compose构建Material 3风格的UI
 */
class MainActivity : ComponentActivity() {
    
    // 音乐播放器实例
    private lateinit var musicPlayer: MusicPlayer
    
    // 权限请求启动器
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
            // TODO: Scan local music files
        } else {
            Toast.makeText(
                this,
                "Storage permission denied. Cannot play local files.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化音乐播放器
        musicPlayer = MusicPlayer(this)
        
        // 检查并请求存储权限
        checkAndRequestPermissions()
        
        // 设置Compose内容
        setContent {
            UnionMusicPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UnionMusicApp(
                        musicPlayer = musicPlayer,
                        onRequestPermission = { checkAndRequestPermissions() }
                    )
                }
            }
        }
    }
    
    /**
     * 检查并请求存储权限
     */
    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11-12 (API 30-32)
            // READ_EXTERNAL_STORAGE在Android 11+已废弃，但仍需要请求以兼容旧应用
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            // Android 10及以下 (API 29-)
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放音乐播放器资源
        musicPlayer.release()
    }
}
