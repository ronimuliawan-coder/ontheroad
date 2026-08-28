# OnTheRoad Production ProGuard / R8 Rules

# Room SQLite Database & Entities
-keep class androidx.room.RoomDatabase { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public void <init>();
}
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# Kotlin Coroutines & Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# Jetpack Compose Runtime & UI
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }

# Google Play Services Location
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.location.**

# Keep Domain Models
-keep class com.ontheroad.core.model.** { *; }
