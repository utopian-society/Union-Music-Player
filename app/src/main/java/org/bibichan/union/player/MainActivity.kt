/**
 * MainActivity.kt - 主界面Activity
 *
 * 这是Union Music Player的主界面入口点。
 * Activity是Android四大组件之一，代表一个屏幕，用户可以与之交互。
 *
 * 学习要点：
 * 1. AppCompatActivity：提供向后兼容的Activity基类
 * 2. Bundle：保存和恢复Activity状态
 * 3. 视图绑定：findViewById用于查找布局中的UI元素
 * 4. MediaPlayer：Android内置的音频播放器类
 */

// 包名：用于组织代码，避免类名冲突
package org.bibichan.union.player

// 导入语句：引入其他类和包
import androidx.appcompat.app.AppCompatActivity // 兼容性Activity基类
import android.os.Bundle // 用于保存Activity状态
import android.os.Handler // 用于在主线程上执行代码
import android.os.Looper // 代表消息循环
import android.widget.ImageButton // 图片按钮控件
import android.widget.SeekBar // 进度条控件
import android.widget.TextView // 文本显示控件
import android.util.Log // 用于调试日志输出
import android.widget.Toast // 用于显示简短提示消息
import org.bibichan.union.player.data.MusicMetadata
import org.bibichan.union.player.data.AudioFormat

/**
 * MainActivity类 - 应用的主界面
 *
 * 继承自AppCompatActivity以获得更好的兼容性。
 * AppCompatActivity允许在新设备上使用新特性，同时兼容旧设备。
 */
class MainActivity : AppCompatActivity() {

    // ==================== 私有属性 ====================

    /**
     * 音乐播放器实例
     *
     * lateinit关键字：表示这个属性会在稍后初始化（在onCreate中）
     * 这样我们就不需要将其声明为可空类型（?）
     */
    private lateinit var musicPlayer: MusicPlayer

    /**
     * UI控件引用
     * 这些控件在onCreate方法中通过findViewById初始化
     */
    private lateinit var songTitleTextView: TextView // 显示歌曲标题
    private lateinit var songArtistTextView: TextView // 显示艺术家名称
    private lateinit var seekBar: SeekBar // 播放进度条
    private lateinit var playPauseButton: ImageButton // 播放/暂停按钮
    private lateinit var previousButton: ImageButton // 上一首按钮
    private lateinit var nextButton: ImageButton // 下一首按钮

    /**
     * Handler和Runnable用于更新进度条
     *
     * Handler：用于在主线程（UI线程）上执行代码
     * Looper.getMainLooper()：获取主线程的消息循环
     *
     * 注意：在Android中，所有UI操作必须在主线程执行，
     * 否则会抛出CalledFromWrongThreadException
     */
    private val handler = Handler(Looper.getMainLooper())

    /**
     * 用于调试的日志标签
     * 每个类通常定义一个TAG用于Log.d等日志方法
     */
    private val TAG = "MainActivity"

    /**
     * 进度更新Runnable
     * 定义为成员变量以便在onDestroy中移除回调
     */
    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            try {
                // 只有在播放时才更新进度
                if (musicPlayer.isPlaying()) {
                    // 将Long转换为Int，因为SeekBar.progress需要Int类型
                    seekBar.progress = musicPlayer.getCurrentPosition().toInt()
                }
                // 每1000毫秒（1秒）更新一次
                handler.postDelayed(this, 1000)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating progress", e)
            }
        }
    }

    /**
     * onCreate方法 - Activity创建时调用
     *
     * 这是Activity生命周期中最重要的方法之一。
     * 在这里进行：
     * 1. 加载布局文件（setContentView）
     * 2. 初始化UI控件
     * 3. 设置点击监听器
     * 4. 初始化数据
     *
     * @param savedInstanceState 保存的实例状态，用于恢复Activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // 调用父类的onCreate方法，这是必须的
        // super关键字：引用父类的方法
        super.onCreate(savedInstanceState)

        // 设置内容视图：加载activity_main.xml布局文件
        // setContentView将XML布局转换为实际的View对象
        setContentView(R.layout.activity_main)

        // 使用try-catch包裹初始化代码，防止崩溃
        try {
            // 初始化音乐播放器
            // this：将当前Activity作为Context传递
            // Context：提供访问应用资源和系统服务的接口
            musicPlayer = MusicPlayer(this)

            // 初始化UI控件
            // findViewById：在布局中查找指定ID的视图
            // R.id.xxx：资源ID，在R类中自动生成
            songTitleTextView = findViewById(R.id.song_title)
            songArtistTextView = findViewById(R.id.song_artist)
            seekBar = findViewById(R.id.seek_bar)
            playPauseButton = findViewById(R.id.play_pause_button)
            previousButton = findViewById(R.id.previous_button)
            nextButton = findViewById(R.id.next_button)

            // 创建歌曲列表
            // 使用MusicMetadata代替Song
            // MusicMetadata：数据类，包含完整的音乐元数据信息
            val songs = listOf(
                MusicMetadata(
                    id = 1,
                    title = "Sample Music 1",
                    artist = "Artist 1",
                    album = "Sample Album",
                    duration = 180000, // 3分钟
                    bitDepth = null,
                    sampleRateHz = null,
                    filePath = "",
                    format = AudioFormat.MP3
                ),
                MusicMetadata(
                    id = 2,
                    title = "Sample Music 2",
                    artist = "Artist 2",
                    album = "Sample Album",
                    duration = 240000, // 4分钟
                    bitDepth = null,
                    sampleRateHz = null,
                    filePath = "",
                    format = AudioFormat.MP3
                )
            )

            // 将歌曲列表传递给播放器
            musicPlayer.setSongs(songs)

            // 设置按钮点击监听器
            setupClickListeners()

            // 设置SeekBar监听器
            setupSeekBarListener()

            // 更新UI显示当前歌曲信息
            updateSongUI()

            // 初始化进度条
            initializeSeekBar()

            // 播放第一首歌（修复原来的崩溃问题）
            // 原来的代码没有在启动时播放歌曲，导致SeekBar操作时崩溃
            if (songs.isNotEmpty()) {
                musicPlayer.play(0)
                playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
            }

            Log.d(TAG, "Activity created successfully")
        } catch (e: Exception) {
            // 捕获并记录所有异常，防止应用崩溃
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 设置按钮点击监听器
     *
     * 在Kotlin中，使用lambda表达式简化点击监听器的设置
     */
    private fun setupClickListeners() {
        // 播放/暂停按钮点击事件
        // setOnClickListener：设置点击监听器
        playPauseButton.setOnClickListener {
            try {
                if (musicPlayer.isPlaying()) {
                    // 当前正在播放，执行暂停
                    musicPlayer.pause()
                    // 更新按钮图标为播放图标
                    // setImageResource：设置ImageView的图片资源
                    playPauseButton.setImageResource(android.R.drawable.ic_media_play)
                } else {
                    // 当前暂停中，执行播放
                    musicPlayer.resume()
                    // 更新按钮图标为暂停图标
                    playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in play/pause", e)
            }
        }

        // 上一首按钮点击事件
        previousButton.setOnClickListener {
            try {
                musicPlayer.previous()
                updateSongUI()
                // 重置按钮图标
                playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
            } catch (e: Exception) {
                Log.e(TAG, "Error in previous", e)
            }
        }

        // 下一首按钮点击事件
        nextButton.setOnClickListener {
            try {
                musicPlayer.next()
                updateSongUI()
                // 重置按钮图标
                playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
            } catch (e: Exception) {
                Log.e(TAG, "Error in next", e)
            }
        }
    }

    /**
     * 设置SeekBar（进度条）监听器
     *
     * SeekBar.OnSeekBarChangeListener：进度条变化监听器接口
     * 包含三个必须实现的方法
     */
    private fun setupSeekBarListener() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            /**
             * 进度变化时调用
             *
             * @param seekBar 触发事件的SeekBar
             * @param progress 当前进度值
             * @param fromUser 是否由用户拖动触发（true）还是程序设置（false）
             */
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // 只有用户拖动时才调整播放位置
                // 避免与自动更新进度产生冲突
                if (fromUser) {
                    try {
                        musicPlayer.seekTo(progress.toLong())
                    } catch (e: Exception) {
                        Log.e(TAG, "Error seeking", e)
                    }
                }
            }

            /**
             * 用户开始触摸SeekBar时调用
             * 可以在这里暂停自动更新进度
             */
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // 空实现：暂不需要处理
            }

            /**
             * 用户停止触摸SeekBar时调用
             * 可以在这里恢复自动更新进度
             */
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // 空实现：暂不需要处理
            }
        })
    }

    /**
     * 更新歌曲信息显示
     *
     * 从MusicPlayer获取当前歌曲信息，并更新到TextView
     */
    private fun updateSongUI() {
        try {
            // 获取当前播放的歌曲
            val currentSong = musicPlayer.getCurrentSong()

            // ?. 安全调用操作符：如果对象不为null则调用方法，否则返回null
            // ?.let：如果不为null，执行lambda表达式
            currentSong?.let { song ->
                // 更新歌曲标题
                songTitleTextView.text = song.title

                // 更新艺术家名称
                songArtistTextView.text = song.artist

                // 设置SeekBar的最大值为歌曲时长
                seekBar.max = musicPlayer.getDuration().toInt()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI", e)
        }
    }

    /**
     * 初始化进度条
     *
     * 使用Handler定时更新进度条位置
     */
    private fun initializeSeekBar() {
        try {
            // 设置初始最大值（防止SeekBar.max为0导致的问题）
            val duration = musicPlayer.getDuration()
            if (duration > 0) {
                seekBar.max = duration.toInt()
            }

            // 启动进度更新循环
            // postDelayed：在指定延迟后执行Runnable
            handler.postDelayed(updateProgressRunnable, 1000)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing seekbar", e)
        }
    }

    /**
     * onDestroy方法 - Activity销毁时调用
     *
     * 这是Activity生命周期的最后一个回调方法。
     * 在这里必须释放所有资源，防止内存泄漏。
     */
    override fun onDestroy() {
        // 调用父类的onDestroy方法
        super.onDestroy()

        try {
            // 释放音乐播放器资源
            // 如果不释放，MediaPlayer会继续占用音频资源
            musicPlayer.release()

            // 移除所有Handler的回调和消息
            // 这很重要！否则Activity销毁后Handler仍会尝试更新UI
            handler.removeCallbacksAndMessages(null)

            Log.d(TAG, "Activity destroyed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }
}
