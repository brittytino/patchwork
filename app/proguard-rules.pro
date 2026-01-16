# Add project specific ProGuard rules here.
# Guardian Launcher ProGuard Configuration

# Keep Guardian Launcher classes
-keep class com.guardian.launcher.** { *; }

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Security
-keep class androidx.security.crypto.** { *; }

# Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
