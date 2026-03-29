/**
 * AlbumColorExtractor.kt - 專輯封面顏色提取工具
 * 
 * 使用 Android Palette API 從專輯封面圖片提取主色調
 * 用於實現動態模糊背景效果
 * 
 * 2026-03-28: 初始實現
 */
package org.bibichan.union.player.ui.player.utils

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 從專輯封面提取主色調的結果
 */
data class AlbumColor(
    val dominant: Color,
    val vibrant: Color?,
    val darkVibrant: Color?,
    val lightVibrant: Color?,
    val muted: Color?,
    val darkMuted: Color?,
    val lightMuted: Color?
) {
    companion object {
        /**
         * 創建默認顏色（當無法提取時使用）
         */
        fun default(): AlbumColor = AlbumColor(
            dominant = Color(0xFF4CAF50), // 綠色主調
            vibrant = Color(0xFF81C784),
            darkVibrant = Color(0xFF388E3C),
            lightVibrant = Color(0xFFC8E6C9),
            muted = Color(0xFFA5D6A7),
            darkMuted = Color(0xFF2E7D32),
            lightMuted = Color(0xFFE8F5E9)
        )
    }
}

/**
 * 專輯顏色提取器
 * 
 * 使用示例:
 * ```kotlin
 * val extractor = AlbumColorExtractor(context)
 * val color = extractor.extractFromPath(albumArtPath)
 * ```
 */
class AlbumColorExtractor(
    private val imageLoader: ImageLoader
) {
    /**
     * 從圖片路徑提取顏色
     * 
     * @param imagePath 專輯封面圖片路徑
     * @return AlbumColor 包含各種顏色變體
     */
    suspend fun extractFromPath(imagePath: String?): AlbumColor = withContext(Dispatchers.IO) {
        if (imagePath.isNullOrBlank()) {
            return@withContext AlbumColor.default()
        }
        
        try {
            val request = ImageRequest.Builder(imageLoader)
                .data(imagePath)
                .allowHardware(false)
                .build()
            
            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                if (bitmap != null) {
                    return@withContext extractFromBitmap(bitmap)
                }
            }
        } catch (e: Exception) {
            // 忽略錯誤，返回默認顏色
        }
        
        AlbumColor.default()
    }

    /**
     * 從 Bitmap 提取顏色
     * 
     * @param bitmap 專輯封面 Bitmap
     * @return AlbumColor 包含各種顏色變體
     */
    fun extractFromBitmap(bitmap: Bitmap): AlbumColor {
        val palette = Palette.from(bitmap)
            .maximumColorCount(24)
            .generate()
        
        return AlbumColor(
            dominant = palette.dominantSwatch?.rgb?.let { Color(it) } ?: Color(0xFF4CAF50),
            vibrant = palette.vibrantSwatch?.rgb?.let { Color(it) },
            darkVibrant = palette.darkVibrantSwatch?.rgb?.let { Color(it) },
            lightVibrant = palette.lightVibrantSwatch?.rgb?.let { Color(it) },
            muted = palette.mutedSwatch?.rgb?.let { Color(it) },
            darkMuted = palette.darkMutedSwatch?.rgb?.let { Color(it) },
            lightMuted = palette.lightMutedSwatch?.rgb?.let { Color(it) }
        )
    }

    /**
     * 從 Android Bitmap 提取顏色（非 Compose 版本）
     * 
     * @param androidBitmap Android Bitmap
     * @return AlbumColor 包含各種顏色變體
     */
    fun extractFromAndroidBitmap(androidBitmap: Bitmap): AlbumColor {
        val palette = Palette.from(androidBitmap)
            .maximumColorCount(24)
            .generate()
        
        return AlbumColor(
            dominant = palette.dominantSwatch?.rgb?.let { Color(it) } ?: Color(0xFF4CAF50),
            vibrant = palette.vibrantSwatch?.rgb?.let { Color(it) },
            darkVibrant = palette.darkVibrantSwatch?.rgb?.let { Color(it) },
            lightVibrant = palette.lightVibrantSwatch?.rgb?.let { Color(it) },
            muted = palette.mutedSwatch?.rgb?.let { Color(it) },
            darkMuted = palette.darkMutedSwatch?.rgb?.let { Color(it) },
            lightMuted = palette.lightMutedSwatch?.rgb?.let { Color(it) }
        )
    }

    companion object {
        /**
         * 創建默認的顏色提取器
         */
        fun createDefault(imageLoader: ImageLoader): AlbumColorExtractor {
            return AlbumColorExtractor(imageLoader)
        }
    }
}