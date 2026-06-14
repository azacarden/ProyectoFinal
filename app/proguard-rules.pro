# Mantener las clases de modelo de base de datos (ROOM)
-keep class com.azahara.proyecto_final_azahara.model.** { *; }

# Mantener los DTOs de Firebase Firestore para que no falle la sincronización ni el login
-keep class com.azahara.proyecto_final_azahara.data.remote.** { *; }

# Mantener los DTOs de la API de CIMA (Retrofit/Gson) para que funcione el buscador
-keep class com.azahara.proyecto_final_azahara.data.network.** { *; }

# Reglas generales para Gson (el traductor de JSON a Kotlin)
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }