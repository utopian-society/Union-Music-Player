/**
 * MainActivity.kt - 主界面Activity (Jetpack Compose版本)
 *
 * 这是Union Music Player的主界面入口点，使用Jetpack Compose构建UI。
 * Material 3设计系统，Apple Music风格的三按钮导航。
 *
 * 2026-03-22: 添加資料夾選擇器支援
 * 2026-03-23: 添加掃描進度對話框支援
 * 2026-03-24: 新掃描會取代舊索引
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bibichan.union.player.MusicPlayer
import org.bibichan.union.player.data.MusicScanner
import org.bibichan.union.player.data.ScannedFilesManager
import org.bibichan.union.player.ui.components.ScanningDialogState
import org.bibichan.union.player.ui.components.ScanningProgressDialog
import org.bibichan.union.player.ui.theme.UnionMusicPlayerTheme

private const val TAG = "MainActivity"

/**
 * MainActivity类 - 应用的主界面
 *
 * 使用Jetpack Compose构建Material 3风格的UI
 */
class MainActivity : ComponentActivity() {

    private lateinit var musicPlayer: MusicPlayer

    private lateinit var musicScanner: MusicScanner

    private lateinit var scannedFilesManager: ScannedFilesManager

    private val scope = CoroutineScope(Dispatchers.Main)

    private val folderPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            Log.i(TAG, "Selected folder URI: $uri")
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            scanSelectedFolder(it)
        } ?: run {
            Log.w(TAG, "No folder selected")
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
            onPermissionGranted()
        } else {
            Toast.makeText(
                this,
                "Storage permission denied. Some features may not work.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val manageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Toast.makeText(this, "All files access granted", Toast.LENGTH_SHORT).show()
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

        musicPlayer = MusicPlayer(this)
        musicScanner = MusicScanner(this)
        scannedFilesManager = ScannedFilesManager.getInstance(this)

        checkAndRequestPermissions()

        setContent {
            UnionMusicPlayerTheme {
                var scanningDialogState by remember { mutableStateOf(ScanningDialogState()) }

                val scanState by musicScanner.scanState.collectAsState()

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

                if (scanningDialogState.isVisible) {
                    ScanningProgressDialog(
                        scanState = scanState,
                        onDismiss = {
                            scanningDialogState = scanningDialogState.copy(isVisible = false)
                        },
                        onCancel = {
                            musicScanner.stopScan()
                            scanningDialogState = scanningDialogState.copy(isVisible = false)
                            Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                LaunchedEffect(scanState) {
                    when (scanState) {
                        is MusicScanner.ScanState.Scanning -> {
                            scanningDialogState = scanningDialogState.copy(
                                isVisible = true,
                                scanState = scanState
                            )
                        }
                        is MusicScanner.ScanState.Completed,
                        is MusicScanner.ScanState.Error -> {
                            scanningDialogState = scanningDialogState.copy(scanState = scanState)
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun openFolderPicker() {
        Log.i(TAG, "Opening folder picker...")
        folderPickerLauncher.launch(null)
    }

    private fun scanSelectedFolder(folderUri: Uri) {
        Log.i(TAG, "Starting scan for folder: $folderUri")

        scope.launch {
            try {
                val documentFile = DocumentFile.fromTreeUri(this@MainActivity, folderUri)
                val folderName = documentFile?.name ?: "Unknown Folder"
                val folderPath = folderUri.path ?: folderUri.toString()

                Log.i(TAG, "Folder name: $folderName")

                val result = musicScanner.scanDocumentFolder(folderUri, this@MainActivity)

                Log.i(TAG, "Scan completed: ${result.songs.size} songs found")

                if (result.songs.isNotEmpty()) {
                    scannedFilesManager.replaceAllWithSingleFolder(
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

    private fun onPermissionGranted() {
        PermissionManager.updatePermissionStatus(this)
        Toast.makeText(this, "Permissions updated, refreshing UI...", Toast.LENGTH_SHORT).show()
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    manageStorageLauncher.launch(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    manageStorageLauncher.launch(intent)
                }
            } else {
                onPermissionGranted()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_AUDIO))
            } else {
                onPermissionGranted()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            } else {
                onPermissionGranted()
            }
        }
    }

    private fun hasAllFilesAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayer.release()
        musicScanner.cleanup()
    }
}
