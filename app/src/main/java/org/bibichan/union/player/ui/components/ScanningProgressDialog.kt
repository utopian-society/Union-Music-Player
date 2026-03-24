/**
 * ScanningProgressDialog.kt - 掃描進度對話框
 *
 * 顯示文件掃描進度，包含進度條、文件計數和取消按鈕。
 * 2026-03-23: 新增功能，解決掃描時無進度顯示的問題
 */
package org.bibichan.union.player.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.bibichan.union.player.data.MusicScanner

/**
 * 掃描進度對話框
 *
 * @param scanState 掃描狀態 Flow
 * @param onDismiss 關閉對話框的回調
 * @param onCancel 取消掃描的回調
 */
@Composable
fun ScanningProgressDialog(
    scanState: MusicScanner.ScanState,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 當掃描完成或出錯時自動關閉
    LaunchedEffect(scanState) {
        when (scanState) {
            is MusicScanner.ScanState.Completed -> {
                // 延遲一下讓用戶看到完成狀態
                kotlinx.coroutines.delay(500)
                onDismiss()
            }
            is MusicScanner.ScanState.Error -> {
                // 錯誤時保持對話框開啟，讓用戶看到錯誤信息
            }
            else -> {}
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (scanState !is MusicScanner.ScanState.Scanning) {
                onDismiss()
            }
        },
        title = {
            Text(
                text = when (scanState) {
                    is MusicScanner.ScanState.Scanning -> "Scanning Folder..."
                    is MusicScanner.ScanState.Completed -> "Scan Complete"
                    is MusicScanner.ScanState.Error -> "Scan Error"
                    is MusicScanner.ScanState.Idle -> "Preparing..."
                }
            )
        },
        text = {
            Column(
                modifier = modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (scanState) {
                    is MusicScanner.ScanState.Scanning -> {
                        // 進度條
                        LinearProgressIndicator(
                            progress = { scanState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                        )

                        // 當前文件信息
                        Text(
                            text = scanState.currentFile,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // 進度百分比
                        Text(
                            text = "${(scanState.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    is MusicScanner.ScanState.Completed -> {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                            contentDescription = "Complete",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Found ${scanState.result.songs.size} songs",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (scanState.result.songs.isEmpty()) {
                            Text(
                                text = "No audio files were found in the selected folder.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    is MusicScanner.ScanState.Error -> {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Error,
                            contentDescription = "Error",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )

                        Text(
                            text = scanState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    is MusicScanner.ScanState.Idle -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Preparing to scan...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            when (scanState) {
                is MusicScanner.ScanState.Scanning -> {
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                }
                is MusicScanner.ScanState.Completed,
                is MusicScanner.ScanState.Error,
                is MusicScanner.ScanState.Idle -> {
                    TextButton(onClick = onDismiss) {
                        Text("OK")
                    }
                }
            }
        }
    )
}

/**
 * 掃描進度對話框的狀態管理
 *
 * 用於在 Activity 中管理掃描對話框的顯示狀態
 */
data class ScanningDialogState(
    val isVisible: Boolean = false,
    val scanState: MusicScanner.ScanState = MusicScanner.ScanState.Idle
)
