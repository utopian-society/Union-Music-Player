# Union Music Player - ProGuard Rules
# Optimized for APK size reduction

# ═══════════════════════════════════════════════════════════════
# ANDROIDX & COMPOSE - Keep essential framework classes
# ═══════════════════════════════════════════════════════════════

# Keep Compose classes
-keep class androidx.compose.** { *; }
-keep class androidx.navigation.** { *; }

# Keep AndroidX core components
-keepattributes *Annotation*
-keepclassmembers class * extends android.app.Activity { *; }
-keepclassmembers class * extends android.app.Service { *; }
-keepclassmembers class * extends android.view.View { *; }

# Keep Kotlin metadata for reflection
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ═══════════════════════════════════════════════════════════════
# AUDIO LIBRARIES - Only keep if actually used
# ═══════════════════════════════════════════════════════════════

# JAudioTagger - Keep only if metadata extraction is used
# Using -dontwarn to allow missing classes without keeping entire library
-dontwarn javax.swing.**
-dontwarn java.awt.**
-dontwarn org.jaudiotagger.**

# Mp3agic - Keep only if actually used
-dontwarn com.mpatric.mp3agic.**

# If audio metadata extraction is implemented, uncomment these:
# -keep class org.jaudiotagger.** { *; }
# -keep class com.mpatric.mp3agic.** { *; }

# ═══════════════════════════════════════════════════════════════
# COIL IMAGE LOADING - Optimize for size
# ═══════════════════════════════════════════════════════════════

# Keep Coil classes
-keep class coil.** { *; }
-keep class coil3.** { *; }

# ═══════════════════════════════════════════════════════════════
# SIZE OPTIMIZATIONS
# ═══════════════════════════════════════════════════════════════

# Remove debug attributes in release builds
-optimizations !code/simplification/various,!code/allocation/*
-allowaccessmodification

# Remove logging calls (optional - uncomment to enable)
# -assumenosideeffects class android.util.Log {
#     public static *** d(...);
#     public static *** v(...);
# }

# ═══════════════════════════════════════════════════════════════
# R8 SPECIFIC OPTIMIZATIONS
# ═══════════════════════════════════════════════════════════════

# Enable R8 full mode for better optimization
# (Set in gradle.properties: android.enableR8.fullMode=true)

# Keep generic signature information
-keepattributes Signature

# Keep line number table for crash reporting (can be removed for smaller size)
# -keepattributes SourceFile,LineNumberTable
