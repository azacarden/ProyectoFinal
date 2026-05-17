plugins {
    alias(libs.plugins.android.application)
    // Forzamos la versión exacta 1.9.24 directamente aquí para que Room no falle
    id("org.jetbrains.kotlin.android") version "1.9.24"
    kotlin("kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.azahara.proyecto_final_azahara"
    // Corregido: Volvemos a poner 36 como exigen tus librerías del .toml
    compileSdk = 36

    defaultConfig {
        applicationId = "com.azahara.proyecto_final_azahara"
        minSdk = 26
        targetSdk = 36 // Volvemos a poner 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Dependencias de Room
    // Cambiamos el 2.6.1 por el 2.7.0 para que sea compatible con Android Studio 9
    val roomVersion = "2.7.0"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // Ciclo de vida y Corrutinas
    val lifecycleVersion = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Dependencias para Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.13.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Dependencias de Navegación y UI
    val navVersion = "2.7.7"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    implementation(libs.androidx.gridlayout)

    // --- DEPENDENCIAS PARA CONEXIÓN WEB (API CIMA) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Base de datos en la nube para vincular cuentas (¡Ahora con su versión correcta!)
    implementation("com.google.firebase:firebase-firestore-ktx:24.10.0")

    // Librería para DIBUJAR el código QR en pantalla
    implementation("com.google.zxing:core:3.5.3")

    // Librería para LEER códigos QR con la cámara fácilmente
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
}