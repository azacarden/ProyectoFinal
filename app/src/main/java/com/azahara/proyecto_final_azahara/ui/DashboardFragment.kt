package com.azahara.proyecto_final_azahara.ui

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

        // 1. Recuperamos de forma segura el nombre del usuario identificado
        val prefs = requireContext().getSharedPreferences("SesionUsuario", android.content.Context.MODE_PRIVATE)
        val miUsuario = prefs.getString("usuario_identificado", "Usuario") ?: "Usuario"

        // 2. Buscamos el TextView y aplicamos tu nuevo formato personalizado con salto de línea
        val tvSaludo = view.findViewById<TextView>(R.id.tvSaludoDashboard)
        tvSaludo.text = "¡Hola, $miUsuario!\n¿Qué quieres hacer hoy?"

        // Botón 1: Mis Pastillas (Ahora es el Banner Principal)
        view.findViewById<MaterialCardView>(R.id.cardMedicacion).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_medicationList)
        }

        // Botón 2: Nueva Toma
        view.findViewById<MaterialCardView>(R.id.cardAdd).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_addMedication)
        }

        // Botón 3: Historial
        view.findViewById<MaterialCardView>(R.id.cardHistorial).setOnClickListener {
            Toast.makeText(requireContext(), "Navegando a Historial", Toast.LENGTH_SHORT).show()
        }

        // Botón 4: Citas Médicas y Alarmas
        view.findViewById<MaterialCardView>(R.id.cardAlarmas).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_appointmentList)
        }

        // Botón 5: Cuidadores / Perfil QR
        view.findViewById<MaterialCardView>(R.id.cardCuidadores).setOnClickListener {
            val bundle = Bundle().apply {
                putString("NOMBRE_USUARIO_LOGUEADO", miUsuario)
            }
            findNavController().navigate(R.id.action_dashboard_to_profile, bundle)
        }
    }
}