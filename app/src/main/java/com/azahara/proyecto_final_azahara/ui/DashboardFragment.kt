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

        // Botón 1: Mis Pastillas (Consulta reactiva del paso [UI-05])
        view.findViewById<MaterialCardView>(R.id.cardMedicacion).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_medicationList)
        }

        // Botón 2: Nueva Toma (Buscador API CIMA del paso [UI-04])
        view.findViewById<MaterialCardView>(R.id.cardAdd).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_addMedication)
        }

        // Botón 3: Historial
        view.findViewById<MaterialCardView>(R.id.cardHistorial).setOnClickListener {
            Toast.makeText(requireContext(), "Navegando a Historial", Toast.LENGTH_SHORT).show()
        }

        // Botón 4: Citas Médicas y Alarmas (¡CONECTADO PARA EL PASO [UI-06]!)
        view.findViewById<MaterialCardView>(R.id.cardAlarmas).setOnClickListener {
            // Quitamos el Toast de mentira y ejecutamos la acción real del nav_graph
            findNavController().navigate(R.id.action_dashboard_to_appointmentList)
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