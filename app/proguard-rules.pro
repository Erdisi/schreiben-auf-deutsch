# --- Schreiben auf Deutsch Production Proguard Rules ---

# 1. Jetpack Compose & Material 3
-keepclassmembers class androidx.compose.ui.platform.InspectableValue {
    private java.lang.Object inspectionTables;
}

# 2. Kotlin Serialization (Crucial for AI Task Generation)
# Keep the serializable classes and their properties to avoid JSON parsing errors
-keepattributes *Annotation*, EnclosingMethod, InnerClasses, Signature
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName *;
}
# Keep the generated serializer classes
-keep class **.Companion { *; }
-keep class **.$serializer { *; }

# 3. Room Database
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep class * extends androidx.room.Dao

# 4. Retrofit & OkHttp (Groq API)
-keepattributes Signature, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# 5. Moshi (JSON Converters)
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
}

# 6. ML Kit (Translation)
-keep class com.google.mlkit.** { *; }
-keep interface com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# 7. App Specific Data Models
# Explicitly keep our core learning models to be 100% safe
-keep class com.example.schreibenaufdeutsch.ui.create.GeneratedTask { *; }
-keep class com.example.schreibenaufdeutsch.ui.create.Variation { *; }
-keep class com.example.schreibenaufdeutsch.ui.practice.PracticeSentence { *; }
-keep class com.example.schreibenaufdeutsch.data.local.entity.** { *; }

# 8. Navigation 3
-keep class androidx.navigation3.** { *; }
