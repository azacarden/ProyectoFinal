package com.azahara.proyecto_final_azahara.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.azahara.proyecto_final_azahara.R
import com.google.android.material.card.MaterialCardView

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                // Pasamos el nombre del usuario logueado
                putString("NOMBRE_USUARIO_LOGUEADO", "Azahara")
            }
            findNavController().navigate(R.id.action_dashboard_to_profile, bundle)
        }
    }
}