/**
 * LogPanel.kt - 日志面板组件
 *
 * 显示应用日志信息，用于调试
 */
package org.bibichan.union.player.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日志条目数据类
 */
data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel = LogLevel.INFO,
    val tag: String = "",
    val message: String
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
            return sdf.format(Date(timestamp))
        }
}

/**
 * 日志级别
 */
enum class LogLevel(val color: Color, val prefix: String) {
    VERBOSE(Color(0xFF616161), "V"),
    DEBUG(Color(0xFF2196F3), "D"),
    INFO(Color(0xFF4CAF50), "I"),
    WARNING(Color(0xFFFF9800), "W"),
    ERROR(Color(0xFFF44336), "E")
}

/**
 * 日志管理器 - 单例模式
 */
object LogManager {
    private val _logs = mutableStateListOf<LogEntry>()
    val logs: List<LogEntry> = _logs.toList()
    
    private const val MAX_LOGS = 500
    
    fun v(tag: String, message: String) {
        addLog(LogLevel.VERBOSE, tag, message)
        Log.v(tag, message)
    }
    
    fun d(tag: String, message: String) {
        addLog(LogLevel.DEBUG, tag, message)
        Log.d(tag, message)
    }
    
    fun i(tag: String, message: String) {
        addLog(LogLevel.INFO, tag, message)
        Log.i(tag, message)
    }
    
    fun w(tag: String, message: String) {
        addLog(LogLevel.WARNING, tag, message)
        Log.w(tag, message)
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val fullMessage = if (throwable != null) {
            "$message\n${throwable.message}\n${throwable.stackTraceToString()}"
        } else {
            message
        }
        addLog(LogLevel.ERROR, tag, fullMessage)
        Log.e(tag, message, throwable)
    }
    
    private fun addLog(level: LogLevel, tag: String, message: String) {
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message
        )
        _logs.add(0, entry) // 添加到开头
        
        // 限制日志数量
        if (_logs.size > MAX_LOGS) {
            _logs.removeRange(MAX_LOGS, _logs.size)
        }
    }
    
    fun clear() {
        _logs.clear()
    }
    
    fun getLogsByLevel(level: LogLevel): List<LogEntry> {
        return _logs.filter { it.level == level }
    }
    
    fun getLogsByTag(tag: String): List<LogEntry> {
        return _logs.filter { it.tag.contains(tag, ignoreCase = true) }
    }
}

/**
 * 日志面板组件
 */
@Composable
fun LogPanel(
    modifier: Modifier = Modifier
) {
    var filterLevel by remember { mutableStateOf<LogLevel?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    
    val logs = LogManager.logs
    val filteredLogs = remember(logs, filterLevel, searchQuery) {
        logs.filter { entry ->
            val matchesLevel = filterLevel == null || entry.level == filterLevel
            val matchesSearch = searchQuery.isBlank() || 
                entry.message.contains(searchQuery, ignoreCase = true) ||
                entry.tag.contains(searchQuery, ignoreCase = true)
            matchesLevel && matchesSearch
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 头部
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Logs",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Debug Logs",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${filteredLogs.size} entries",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (isExpanded) {
                // 过滤器
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 搜索框
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search logs...", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    
                    // 清除按钮
                    IconButton(
                        onClick = { 
                            LogManager.clear()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Clear logs",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                // 日志级别过滤
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LogLevel.values().forEach { level ->
                        FilterChip(
                            selected = filterLevel == level,
                            onClick = { 
                                filterLevel = if (filterLevel == level) null else level 
                            },
                            label = { 
                                Text(level.prefix, fontSize = 10.sp) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = level.color.copy(alpha = 0.2f),
                                selectedContainerColor = level.color.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
                
                // 日志列表
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filteredLogs) { entry ->
                        LogEntryItem(entry = entry)
                    }
                }
            }
        }
    }
}

/**
 * 日志条目组件
 */
@Composable
private fun LogEntryItem(
    entry: LogEntry,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = entry.level.color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 时间戳
        Text(
            text = entry.formattedTime,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(70.dp)
        )
        
        // 级别
        Text(
            text = entry.level.prefix,
            style = MaterialTheme.typography.labelSmall,
            color = entry.level.color,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(12.dp)
        )
        
        // 标签
        Text(
            text = entry.tag,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(60.dp)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // 消息
        Text(
            text = entry.message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
