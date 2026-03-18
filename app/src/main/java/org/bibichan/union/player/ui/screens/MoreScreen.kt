/**
 * MoreScreen.kt - 更多选项界面
 *
 * 包含设置、文件扫描等功能。
 */
package org.bibichan.union.player.ui.screens

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
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onRequestPermission: () -> Unit
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    
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
            // 文件扫描选项
            SettingsItem(
                icon = Icons.Default.FolderOpen,
                title = "Scan Local Files",
                subtitle = "Scan your device for music files",
                onClick = onRequestPermission
            )
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // 设置
            SettingsItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                subtitle = "App preferences and configuration",
                onClick = { /* TODO: Open settings */ }
            )
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // 主题设置
            SettingsItem(
                icon = Icons.Default.Palette,
                title = "Theme",
                subtitle = "Light / Dark mode",
                onClick = { /* TODO: Theme settings */ }
            )
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // 音质设置
            SettingsItem(
                icon = Icons.Default.GraphicEq,
                title = "Audio Quality",
                subtitle = "Playback quality settings",
                onClick = { /* TODO: Audio quality settings */ }
            )
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // 关于
            SettingsItem(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "App version and credits",
                onClick = { showAboutDialog = true }
            )
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 应用信息卡片
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
    
    // 关于对话框
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text("About Union Music Player")
            },
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
