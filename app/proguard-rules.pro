# Kotlin serialization keeps serializers reflectively discoverable
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.rejwane.reelslocal.**$$serializer { *; }
-keepclassmembers class com.rejwane.reelslocal.** {
    *** Companion;
}
-keepclasseswithmembers class com.rejwane.reelslocal.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Media3
-dontnote androidx.media3.**
