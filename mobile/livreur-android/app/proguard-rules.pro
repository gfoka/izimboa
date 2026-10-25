# kotlinx.serialization : garder les sérialiseurs générés des classes @Serializable.
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class com.izimboa.livreur.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.izimboa.livreur.**$$serializer { *; }

# SQLCipher : classes appelées depuis le code natif.
-keep class net.zetetic.database.** { *; }
-keep class net.zetetic.database.sqlcipher.** { *; }
