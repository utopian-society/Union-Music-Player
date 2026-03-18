/**
 * Song.kt - 歌曲数据类
 * 
 * 这是一个数据类（data class），专门用于存储数据。
 * Kotlin会自动生成equals()、hashCode()、toString()等方法。
 * 
 * 学习要点：
 * 1. 数据类的概念和用途
 * 2. 主构造函数和属性
 * 3. Kotlin的类型推断
 */

package org.bibichan.union.player

/**
 * Song数据类 - 表示一首歌曲
 * 
 * data class关键字：自动生成以下方法
 * - equals()：比较两个对象是否相等
 * - hashCode()：返回对象的哈希码
 * - toString()：返回对象的字符串表示
 * - copy()：创建对象的副本
 * - componentN()：解构声明支持
 * 
 * @param title 歌曲标题（字符串类型）
 * @param artist 艺术家名称（字符串类型）
 * @param resourceId 资源ID（Int类型），指向res/raw/目录下的音频文件
 */
data class Song(
    val title: String,      // val：只读属性（不可变）
    val artist: String,
    val resourceId: Int     // R.raw.xxx形式的资源ID
)

/**
 * 使用示例：
 * 
 * // 创建Song实例
 * val song = Song("My Song", "Artist", R.raw.my_song)
 * 
 * // 访问属性
 * println(song.title)   // 输出: My Song
 * println(song.artist)  // 输出: Artist
 * 
 * // 解构声明（data class特有功能）
 * val (title, artist, resourceId) = song
 * println(title)        // 输出: My Song
 * 
 * // 复制对象（可以修改部分属性）
 * val song2 = song.copy(title = "New Title")
 * println(song2.title)  // 输出: New Title
 */
