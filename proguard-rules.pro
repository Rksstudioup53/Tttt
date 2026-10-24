# Add project specific ProGuard rules here.
# Media3 ExoPlayer keep rules
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Jetpack Compose keep rules
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**