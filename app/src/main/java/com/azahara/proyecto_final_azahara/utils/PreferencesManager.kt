package com.azahara.proyecto_final_azahara.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    // Crea un archivo XML oculto en el móvil llamado "ajustes_accesibilidad"
    private val prefs: SharedPreferences = context.getSharedPreferences("ajustes_accesibilidad", Context.MODE_PRIVATE)

    var textoGrande: Boolean
        get() = prefs.getBoolean("texto_grande", false)
        set(value) = prefs.edit().putBoolean("texto_grande", value).apply()

    var sonidosActivados: Boolean
        get() = prefs.getBoolean("sonidos_activados", true) // Por defecto encendidos
        set(value) = prefs.edit().putBoolean("sonidos_activados", value).apply()

    var altoContraste: Boolean
        get() = prefs.getBoolean("alto_contraste", false)
        set(value) = prefs.edit().putBoolean("alto_contraste", value).apply()
}