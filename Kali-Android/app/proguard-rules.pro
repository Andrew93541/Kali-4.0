# KALI Android Production ProGuard / R8 Obfuscation Rules

# Project specific keeps for API and SQLite models
-keep class com.kali.network.** { *; }
-keep class com.kali.model.** { *; }

# Serialization annotations protection
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Retrofit, OkHttp and standard warning suppressions
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**

# Room Database structural integrity
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
