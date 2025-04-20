
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Keep Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# kotlinx-serialization-json specific
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep serializers for serializable classes
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep our model classes
-keep class com.magic.habbittracker.models.** { *; }
-keep class com.magic.habbittracker.data.HabitData { *; }
-keep class com.magic.habbittracker.data.CategoryData { *; }
-keep class com.magic.habbittracker.viewmodels.ExportData { *; }

# Keep only the material icons we're actually using
-keep class androidx.compose.material.icons.Icons$Filled { *; }
-keep class androidx.compose.material.icons.Icons$Outlined { *; }
-keep class androidx.compose.material.icons.Icons$Rounded { *; }

# WorkManager
-keepclassmembers class androidx.work.impl.** { *; }

# Remove debug logs in release build
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}