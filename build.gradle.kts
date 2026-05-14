// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // Añadimos el plugin de Google Services para que Android Studio entienda el JSON
    id("com.google.gms.google-services") version "4.4.4" apply false
}