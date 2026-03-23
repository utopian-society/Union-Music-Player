package org.bibichan.union.player.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 權限管理器
 * 用於管理存儲權限狀態並通知 UI 更新
 */
object PermissionManager {
    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted: StateFlow<Boolean> = _permissionGranted.asStateFlow()
    
    /**
     * 檢查存儲權限
     */
    fun checkStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 更新權限狀態
     */
    fun updatePermissionStatus(context: Context) {
        val hasPermission = checkStoragePermission(context)
        _permissionGranted.value = hasPermission
    }
    
    /**
     * 手動設置權限狀態（用於測試或權限授予後）
     */
    fun setPermissionGranted(granted: Boolean) {
        _permissionGranted.value = granted
    }
}