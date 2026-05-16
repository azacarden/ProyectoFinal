package com.azahara.proyecto_final_azahara.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.azahara.proyecto_final_azahara.R
import com.google.android.material.card.MaterialCardView

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón 1: Mis Pastillas
        view.findViewById<MaterialCardView>(R.id.cardMedicacion).setOnClickListener {
            Toast.makeText(requireContext(), "Navegando a Mis Pastillas", Toast.LENGTH_SHORT).show()
            // findNavController().navigate(R.id.action_dashboard_to_medicationList)
        }

        // Botón 2: Nueva Toma
        view.findViewById<MaterialCardView>(R.id.cardAdd).setOnClickListener {
            Toast.makeText(requireContext(), "Navegando a Nueva Toma", Toast.LENGTH_SHORT).show()
            // findNavController().navigate(R.id.action_dashboard_to_addMedication)
        }

        // Botón 3: Historial
        view.findViewById<MaterialCardView>(R.id.cardHistorial).setOnClickListener {
            Toast.makeText(requireContext(), "Navegando a Historial", Toast.LENGTH_SHORT).show()
        }

        // Botón 4: Alarmas
        view.findViewById<MaterialCardView>(R.id.cardAlarmas).setOnClickListener {
            Toast.makeText(requireContext(), "Navegando a Alarmas", Toast.LENGTH_SHORT).show()
        }

        // Botón 5: Cuidadores
        view.findViewById<MaterialCardView>(R.id.cardCuidadores).setOnClickListener {
            Toast.makeText(requireContext(), "Navegando a Cuidadores", Toast.LENGTH_SHORT).show()
        }

        // Botón 6: Mi Perfil
        view.findViewById<MaterialCardView>(R.id.cardPerfil).setOnClickListener {
            Toast.makeText(requireContext(), "Navegando a Mi Perfil", Toast.LENGTH_SHORT).show()
        }
    }
}