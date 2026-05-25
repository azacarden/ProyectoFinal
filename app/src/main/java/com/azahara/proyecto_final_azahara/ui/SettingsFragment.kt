package com.azahara.proyecto_final_azahara.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.azahara.proyecto_final_azahara.R
import com.azahara.proyecto_final_azahara.utils.PreferencesManager
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var preferencesManager: PreferencesManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())

        val switchTexto = view.findViewById<MaterialSwitch>(R.id.switchTextoGrande)
        val switchSonidos = view.findViewById<MaterialSwitch>(R.id.switchSonidos)
        val switchContraste = view.findViewById<MaterialSwitch>(R.id.switchAltoContraste)

        // Carga el estado actual guardado en la memoria del teléfono
        switchTexto.isChecked = preferencesManager.textoGrande
        switchSonidos.isChecked = preferencesManager.sonidosActivados
        switchContraste.isChecked = preferencesManager.altoContraste

        // Escucha los cambios en tiempo real
        switchTexto.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.textoGrande = isChecked
            mostrarAviso(view, "Tamaño de texto actualizado")
        }

        switchSonidos.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.sonidosActivados = isChecked
            mostrarAviso(view, "Configuración de sonidos actualizada")
        }

        switchContraste.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.altoContraste = isChecked
            mostrarAviso(view, "Modo contraste actualizado")
        }
    }

    private fun mostrarAviso(view: View, mensaje: String) {
        Snackbar.make(view, mensaje, Snackbar.LENGTH_SHORT).show()
    }
}