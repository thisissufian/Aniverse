# Proguard rules for Aniverse

# Data models
-keep class com.aniverse.app.data.model.** { *; }
-keepclassmembers class com.aniverse.app.data.model.** { *; }

# Media3 ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Gson & Reflection
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

# OkHttp & Jsoup
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.jsoup.**
-keep class org.jsoup.** { *; }

# Kotlin Coroutines & Coil
-dontwarn kotlinx.coroutines.**
-keep class coil.** { *; }
