/**
 * PlaylistPathUtils.kt - 播放列表路径处理工具
 *
 * 提供播放列表条目规范化与相对路径解析支持。
 */
package org.bibichan.union.player.data

import java.net.URLDecoder

object PlaylistPathUtils {
    fun normalizePlaylistEntry(line: String): String? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null
        }

        var cleaned = trimmed
        if (cleaned.startsWith("\uFEFF")) {
            cleaned = cleaned.removePrefix("\uFEFF")
        }

        if (cleaned.startsWith("file://", ignoreCase = true)) {
            cleaned = cleaned.removePrefix("file://")
        }

        cleaned = try {
            URLDecoder.decode(cleaned, "UTF-8")
        } catch (e: Exception) {
            cleaned
        }

        cleaned = cleaned.replace('\\', '/').trim()

        return normalizeRelativePath(cleaned)
    }

    fun normalizeRelativePath(path: String): String {
        val trimmed = path.trim().trim('/')
        if (trimmed.isEmpty()) {
            return ""
        }

        val parts = trimmed.split('/').filter { it.isNotBlank() }
        val stack = mutableListOf<String>()
        for (part in parts) {
            when (part) {
                "." -> Unit
                ".." -> if (stack.isNotEmpty()) stack.removeAt(stack.size - 1)
                else -> stack.add(part)
            }
        }
        return stack.joinToString("/")
    }

    fun buildRelativePathIndex(musicLibrary: List<MusicMetadata>): Map<String, MusicMetadata> {
        val index = mutableMapOf<String, MusicMetadata>()
        for (song in musicLibrary) {
            val relativePath = song.relativePath?.takeIf { it.isNotBlank() } ?: continue
            val normalized = normalizeRelativePath(relativePath)
            if (normalized.isNotBlank()) {
                index[normalized] = song
            }
        }
        return index
    }

    fun resolveSongFromRelativeIndex(
        entry: String,
        playlistDirPath: String?,
        playlistDirPathWithoutRoot: String?,
        relativeIndex: Map<String, MusicMetadata>
    ): MusicMetadata? {
        val normalizedEntry = normalizeRelativePath(entry)
        if (normalizedEntry.isBlank() || relativeIndex.isEmpty()) {
            return null
        }

        val candidates = mutableListOf<String>()
        candidates.add(normalizedEntry)

        if (!playlistDirPath.isNullOrBlank()) {
            candidates.add(normalizeRelativePath("$playlistDirPath/$normalizedEntry"))
        }

        if (!playlistDirPathWithoutRoot.isNullOrBlank()) {
            candidates.add(normalizeRelativePath("$playlistDirPathWithoutRoot/$normalizedEntry"))
        }

        for (candidate in candidates) {
            relativeIndex[candidate]?.let { return it }
        }

        val suffixMatch = relativeIndex.entries.firstOrNull { (path, _) ->
            path.endsWith("/$normalizedEntry")
        }

        return suffixMatch?.value
    }
}
