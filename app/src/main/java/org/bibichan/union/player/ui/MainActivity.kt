/**
 * MainActivity.kt - 主界面Activity (Jetpack Compose版本)
 *
 * 这是Union Music Player的主界面入口点，使用Jetpack Compose构建UI。
 * Material 3设计系统，Apple Music风格的三按钮导航。
 * 
 * 2026-03-22: 添加資料夾選擇器支援
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
import android.util.Log
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
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bibichan.union.player.MusicPlayer
import org.bibichan.union.player.data.MusicScanner
import org.bibichan.union.player.data.ScannedFilesManager
import org.bibichan.union.player.ui.theme.UnionMusicPlayerTheme

private const val TAG = "MainActivity"

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

    // 已掃描檔案管理器
    private lateinit var scannedFilesManager: ScannedFilesManager

    // 協程作用域
    private val scope = CoroutineScope(Dispatchers.Main)

    // 資料夾選擇器啟動器 (SAF)
    private val folderPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            Log.i(TAG, "Selected folder URI: $uri")
            // 獲取持久化權限
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            // 掃描選擇的資料夾
            scanSelectedFolder(it)
        } ?: run {
            Log.w(TAG, "No folder selected")
        }
    }

    // 权限请求启动器 - 用于普通权限
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
            // 权限授予后，通知UI更新
            onPermissionGranted()
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
                // 权限授予后，通知UI更新
                onPermissionGranted()
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

        // 初始化已掃描檔案管理器
        scannedFilesManager = ScannedFilesManager.getInstance(this)

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
                        onRequestPermission = { checkAndRequestPermissions() },
                        onPermissionResult = { onPermissionGranted() },
                        onFolderPickerRequest = { openFolderPicker() }
                    )
                }
            }
        }
    }

    /**
     * 打開資料夾選擇器
     */
    private fun openFolderPicker() {
        Log.i(TAG, "Opening folder picker...")
        folderPickerLauncher.launch(null)
    }

    /**
     * 掃描選擇的資料夾
     */
    private fun scanSelectedFolder(folderUri: Uri) {
        Log.i(TAG, "Starting scan for folder: $folderUri")
        Toast.makeText(this, "Scanning folder...", Toast.LENGTH_SHORT).show()

        scope.launch {
            try {
                // 獲取資料夾名稱
                val documentFile = DocumentFile.fromTreeUri(this@MainActivity, folderUri)
                val folderName = documentFile?.name ?: "Unknown Folder"
                val folderPath = folderUri.path ?: folderUri.toString()

                Log.i(TAG, "Folder name: $folderName")

                // 使用 MusicScanner 掃描資料夾
                val result = musicScanner.scanDocumentFolder(folderUri, this@MainActivity)

                Log.i(TAG, "Scan completed: ${result.songs.size} songs found")

                if (result.songs.isNotEmpty()) {
                    // 添加到 ScannedFilesManager
                    scannedFilesManager.addScannedFolder(
                        folderUri = folderUri,
                        name = folderName,
                        path = folderPath,
                        songs = result.songs
                    )

                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Found ${result.songs.size} songs in $folderName",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "No music files found in $folderName",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scanning folder", e)
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Error scanning folder: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * 权限授予后的回调
     * 用于刷新UI状态
     */
    private fun onPermissionGranted() {
        // 更新 PermissionManager 的權限狀態
        PermissionManager.updatePermissionStatus(this)
        Toast.makeText(this, "Permissions updated, refreshing UI...", Toast.LENGTH_SHORT).show()
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
            } else {
                // 已经有权限，通知UI更新
                onPermissionGranted()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+): 请求READ_MEDIA_AUDIO
            if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_AUDIO))
            } else {
                // 已经有权限，通知UI更新
                onPermissionGranted()
            }
        } else {
            // Android 10及以下: 请求READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            } else {
                // 已经有权限，通知UI更新
                onPermissionGranted()
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
