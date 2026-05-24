package com.azahara.proyecto_final_azahara.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.azahara.proyecto_final_azahara.R
import com.google.android.material.card.MaterialCardView

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Accedemos a la memoria local donde guardamos al usuario al hacer login/registro
        val prefs = requireContext().getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE)
        val nombreUsuario = prefs.getString("usuario_identificado", "Usuario") ?: "Usuario"

        // 2. Localizamos el TextView y aplicamos el nuevo formato de saludo
        val tvSaludo = view.findViewById<TextView>(R.id.tvSaludoDashboard)
        tvSaludo.text = "¡Hola, $nombreUsuario!\n¿Qué quieres hacer hoy?"

        // 3. Configuración de navegación de los 5 botones
        view.findViewById<MaterialCardView>(R.id.cardMedicacion).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_medicationList)
        }

        view.findViewById<MaterialCardView>(R.id.cardAdd).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_addMedication)
        }

        view.findViewById<MaterialCardView>(R.id.cardHistorial).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_appointmentHistory)
        }

        view.findViewById<MaterialCardView>(R.id.cardNuevaCita).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_addAppointment)
        }

        view.findViewById<MaterialCardView>(R.id.cardCuidadores).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_profile)
        }

        view.findViewById<MaterialCardView>(R.id.cardCitas).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_appointmentList)
        }
    }
}