/**
 * AlbumArtCache.kt - Album art cache helper
 *
 * Stores embedded album art as a downscaled JPEG in app-private storage so it can be
 * displayed by Coil and persisted across app restarts.
 */
package org.bibichan.union.player.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

object AlbumArtCache {
    private const val DIRECTORY_NAME = "album_art"
    private const val MAX_SIZE_PX = 512
    private const val JPEG_QUALITY = 85

    data class SavedAlbumArt(
        val bitmap: Bitmap,
        val uriString: String
    )

    fun keyFrom(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    fun saveEmbeddedPicture(
        context: Context,
        key: String,
        imageData: ByteArray
    ): SavedAlbumArt? {
        val bitmap = decodeDownsampledBitmap(imageData, MAX_SIZE_PX) ?: return null
        val uriString = saveBitmap(context, key, bitmap) ?: return null
        return SavedAlbumArt(bitmap = bitmap, uriString = uriString)
    }

    private fun saveBitmap(context: Context, key: String, bitmap: Bitmap): String? {
        return try {
            val directory = File(context.filesDir, DIRECTORY_NAME)
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, "$key.jpg")

            if (!file.exists()) {
                FileOutputStream(file).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
                    output.flush()
                }
            }

            Uri.fromFile(file).toString()
        } catch (_: Exception) {
            null
        }
    }

    private fun decodeDownsampledBitmap(bytes: ByteArray, maxSizePx: Int): Bitmap? {
        return try {
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)

            val outWidth = boundsOptions.outWidth
            val outHeight = boundsOptions.outHeight
            if (outWidth <= 0 || outHeight <= 0) {
                return null
            }

            val inSampleSize = calculateInSampleSize(outWidth, outHeight, maxSizePx)

            val decodeOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                this.inSampleSize = inSampleSize
            }

            val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return null

            val finalWidth = decoded.width
            val finalHeight = decoded.height
            val maxDim = maxOf(finalWidth, finalHeight)

            if (maxDim <= maxSizePx) {
                decoded
            } else {
                val scale = maxSizePx.toFloat() / maxDim.toFloat()
                val targetWidth = (finalWidth * scale).toInt().coerceAtLeast(1)
                val targetHeight = (finalHeight * scale).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(decoded, targetWidth, targetHeight, true)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxSizePx: Int): Int {
        var sampleSize = 1
        var halfWidth = width / 2
        var halfHeight = height / 2

        while (halfWidth / sampleSize >= maxSizePx && halfHeight / sampleSize >= maxSizePx) {
            sampleSize *= 2
        }

        return sampleSize.coerceAtLeast(1)
    }
}
