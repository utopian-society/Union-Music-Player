package org.bibichan.union.player.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import org.bibichan.union.player.ui.theme.MusicTheme

/**
 * MainActivity - Entry Point of the App
 * 
 * This is the first Android component that runs when the app launches.
 * 
 * KEY CONCEPTS:
 * 
 * 1. ComponentActivity:
 *    - Modern Android Activity class
 *    - Designed for Compose apps
 *    - Provides setContent() method
 * 
 * 2. onCreate():
 *    - Called when the activity is first created
 *    - This is where we set up the UI
 *    - Similar to "main()" function but for Android
 * 
 * 3. setContent():
 *    - Compose function that creates the UI
 *    - Everything inside is a composable function
 *    - Automatically updates when data changes
 * 
 * 4. enableEdgeToEdge():
 *    - Makes app draw behind status bar and navigation bar
 *    - Creates immersive, modern look
 *    - Required for full-screen experience
 */
class MainActivity : ComponentActivity() {
    
    /**
     * onCreate() - Called when activity starts
     * 
     * Bundle?: Contains saved state (null on first launch)
     * 
     * Lifecycle:
     * 1. App is launched
     * 2. Android creates MainActivity
     * 3. onCreate() is called
     * 4. We set up the UI with setContent()
     * 5. User sees the app!
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Call parent class's onCreate (required!)
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        // This makes the app draw behind system bars
        enableEdgeToEdge()
        
        // ─────────────────────────────────────────────────────
        // SET UP THE UI WITH COMPOSE
        // ─────────────────────────────────────────────────────
        
        /**
         * setContent() defines the UI using Compose.
         * 
         * Structure (from outside to inside):
         * 
         * 1. MusicTheme: Applies colors and typography
         *    └── 2. Surface: Material Design background container
         *        └── 3. UnionMusicApp: The actual app UI
         */
        setContent {
            // MusicTheme: Wraps app with our custom theme
            // - Provides GreenPrimary, YellowAccent colors
            // - Provides Typography styles
            MusicTheme {
                
                // Surface: Material Design container
                // - Provides proper background color
                // - Fills entire screen
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    
                    // UnionMusicApp: The main app composable
                    // This is where all our UI components live!
                    UnionMusicApp()
                    
                }
            }
        }
    }
}

/**
 * ANDROID ACTIVITY LIFECYCLE (Simplified):
 * 
 * ┌─────────────────┐
 * │   App Launched  │
 * └────────┬────────┘
 *          │
 *          ▼
 * ┌─────────────────┐
 * │   onCreate()    │ ← We are here!
 * │   (Create UI)   │
 * └────────┬────────┘
 *          │
 *          ▼
 * ┌─────────────────┐
 * │   onStart()     │ ← Activity becomes visible
 * └────────┬────────┘
 *          │
 *          ▼
 * ┌─────────────────┐
 * │   onResume()    │ ← User can interact
 * └────────┬────────┘
 *          │
 *          ▼
 * ┌─────────────────┐
 * │   Running...    │
 * └────────┬────────┘
 *          │
 *          ▼
 * ┌─────────────────┐
 * │   onPause()     │ ← User switches apps
 * └────────┬────────┘
 *          │
 *          ▼
 * ┌─────────────────┐
 * │   onStop()      │ ← Activity no longer visible
 * └────────┬────────┘
 *          │
 *          ▼
 * ┌─────────────────┐
 * │   onDestroy()   │ ← Activity is destroyed
 * └─────────────────┘
 */

/**
 * WHAT HAPPENS WHEN APP STARTS:
 * 
 * 1. Android reads AndroidManifest.xml
 *    - Finds MainActivity is the "launcher" activity
 * 
 * 2. Android creates MainActivity instance
 *    - Calls onCreate()
 * 
 * 3. onCreate() calls setContent { }
 *    - Compose starts building UI
 * 
 * 4. Compose executes UnionMusicApp()
 *    - Creates Scaffold
 *    - Creates TopAppBar (green header)
 *    - Creates MiniPlayer (song info + controls)
 *    - Creates BottomNavigation (tabs)
 *    - Creates LibraryScreen (album grid)
 * 
 * 5. UI is displayed on screen!
 * 
 * 6. User interacts (taps buttons, scrolls)
 *    - State changes
 *    - Compose automatically recomposes affected UI
 *    - User sees updates instantly!
 */

/**
 * WHY COMPOSE IS DIFFERENT FROM XML:
 * 
 * OLD WAY (XML layouts):
 * ```
 * // In onCreate():
 * setContentView(R.layout.activity_main)  // Load XML
 * 
 * val button = findViewById<Button>(R.id.myButton)  // Find view
 * button.setOnClickListener {  // Set listener
 *     // Do something
 * }
 * ```
 * 
 * NEW WAY (Jetpack Compose):
 * ```
 * // In onCreate():
 * setContent {
 *     Button(onClick = {  // Direct callback
 *         // Do something
 *     }) {
 *         Text("Click me")
 *     }
 * }
 * ```
 * 
 * COMPOSE ADVANTAGES:
 * - Less boilerplate code
 * - No findViewById() needed
 * - State automatically updates UI
 * - Easier to understand and maintain
 */
