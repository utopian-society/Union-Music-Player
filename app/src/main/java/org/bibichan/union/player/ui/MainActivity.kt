/**
 * MainActivity.kt - 主界面Activity (Jetpack Compose版本)
 *
 * 这是Union Music Player的主界面入口点，使用Jetpack Compose构建UI。
 * Material 3设计系统，Apple Music风格的三按钮导航。
 */
package org.bibichan.union.player.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
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
import org.bibichan.union.player.data.MusicScanner
import org.bibichan.union.player.ui.theme.UnionMusicPlayerTheme

/**
 * MainActivity类 - 应用的主界面
 *
 * 使用Jetpack Compose构建Material 3风格的UI
 */
class MainActivity : ComponentActivity() {

    // 音乐播放器实例
    private lateinit var musicPlayer: MusicPlayer

    // 音乐扫描器
    private lateinit var musicScanner: MusicScanner

    // 权限请求启动器 - 用于普通权限
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                "Storage permission denied. Some features may not work.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // MANAGE_EXTERNAL_STORAGE 权限启动器 (Android 11+)
    private val manageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Toast.makeText(this, "All files access granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "All files access denied. Playlist features may not work properly.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化音乐播放器
        musicPlayer = MusicPlayer(this)

        // 初始化音乐扫描器
        musicScanner = MusicScanner(this)

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
     *
     * 对于Android 11+ (API 30+)，需要MANAGE_EXTERNAL_STORAGE权限来访问所有文件
     * 这样才能正确处理m3u8播放列表中的相对路径
     */
    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+): 检查MANAGE_EXTERNAL_STORAGE权限
            if (!Environment.isExternalStorageManager()) {
                // 请求MANAGE_EXTERNAL_STORAGE权限
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    manageStorageLauncher.launch(intent)
                } catch (e: Exception) {
                    // 如果特定应用的设置页面不可用，打开通用设置页面
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    manageStorageLauncher.launch(intent)
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+): 请求READ_MEDIA_AUDIO
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_AUDIO))
            }
        } else {
            // Android 10及以下: 请求READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            }
        }
    }

    /**
     * 检查是否有所有文件访问权限
     */
    private fun hasAllFilesAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true // Android 10及以下不需要此权限
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放音乐播放器资源
        musicPlayer.release()
        // 清理扫描器资源
        musicScanner.cleanup()
    }
}
