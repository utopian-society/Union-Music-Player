/**
 * MoreScreen.kt - 更多選項界面
 *
 * 包含設置、文件掃描、日誌面板等功能。
 */
package org.bibichan.union.player.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import org.bibichan.union.player.ui.components.LogPanel
import org.bibichan.union.player.ui.components.LogManager
import org.bibichan.union.player.ui.library.LibraryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onRequestPermission: () -> Unit,
    onPermissionResult: () -> Unit = {},
    onFolderPickerRequest: () -> Unit = {}
) {
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 使用 LibraryViewModel 來獲取權限狀態
    val libraryViewModel: LibraryViewModel = viewModel()
    val hasPermission by libraryViewModel.hasPermission.collectAsState()

    // 監聽權限狀態變化
    var previousHasPermission by remember { mutableStateOf(hasPermission) }
    LaunchedEffect(hasPermission) {
        if (!previousHasPermission && hasPermission) {
            // 權限從 false 變為 true，調用回調
            onPermissionResult()
        }
        previousHasPermission = hasPermission
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "More",
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
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 日誌面板（放在頂部以便調試）
            LogPanel(
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 文件掃描選項 - 使用 SAF 資料夾選擇器
            SettingsItem(
                icon = Icons.Default.FolderOpen,
                title = "Scan Local Files",
                subtitle = "Select a folder to scan for music files",
                onClick = {
                    LogManager.i("MoreScreen", "Opening folder picker...")
                    onFolderPickerRequest()
                }
            )

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            // 設置
            SettingsItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                subtitle = "App preferences and configuration",
                onClick = {
                    LogManager.d("MoreScreen", "Opening settings dialog")
                    showSettingsDialog = true
                }
            )

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            // 主題設置
            SettingsItem(
                icon = Icons.Default.Palette,
                title = "Theme",
                subtitle = "Light / Dark mode",
                onClick = {
                    LogManager.d("MoreScreen", "Theme setting clicked")
                    /* TODO: Theme settings */
                }
            )

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            // 音質設置
            SettingsItem(
                icon = Icons.Default.GraphicEq,
                title = "Audio Quality",
                subtitle = "Playback quality settings",
                onClick = {
                    LogManager.d("MoreScreen", "Audio quality setting clicked")
                    /* TODO: Audio quality settings */
                }
            )

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            // 關於
            SettingsItem(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "App version and credits",
                onClick = {
                    LogManager.d("MoreScreen", "Opening about dialog")
                    showAboutDialog = true
                }
            )

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(16.dp))

            // 應用信息卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "App Icon",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Union Music Player",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }

    // 掃描進度對話框
    if (showScanningDialog) {
        AlertDialog(
            onDismissRequest = { showScanningDialog = false },
            title = { Text("Scanning Local Files") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { scanProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = scanStatus)
                }
            },
            confirmButton = {
                TextButton(onClick = { showScanningDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = MaterialTheme.shapes.large
        )
    }

    // 設置對話框
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Settings") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Settings panel will be available in a future update.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Current features:")
                    Text("• Auto-scan on app start")
                    Text("• Background playback")
                    Text("• Material 3 design")
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("OK")
                }
            },
            shape = MaterialTheme.shapes.large
        )
    }

    // 關於對話框
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Union Music Player") },
            text = {
                Column {
                    Text("Version: 1.0.0")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("A beautiful music player built with Material 3 design.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Features:")
                    Text("• Local music playback")
                    Text("• Material 3 design")
                    Text("• Green, Yellow, White color theme")
                    Text("• Apple Music inspired UI")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            },
            shape = MaterialTheme.shapes.large
        )
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
