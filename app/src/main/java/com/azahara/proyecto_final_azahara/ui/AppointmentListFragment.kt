package com.azahara.proyecto_final_azahara.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.azahara.proyecto_final_azahara.R
import com.azahara.proyecto_final_azahara.data.local.AppDatabase
import com.azahara.proyecto_final_azahara.viewmodel.AppointmentViewModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.launch

class AppointmentListFragment : Fragment(R.layout.fragment_appointment_list) {

    private lateinit var viewModel: AppointmentViewModel
    private lateinit var adapter: AppointmentAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dao = AppDatabase.getDatabase(requireContext()).citaMedicaDao()
        viewModel = AppointmentViewModel(dao)

        val rvCitas = view.findViewById<RecyclerView>(R.id.rvCitas)
        val fabAddCita = view.findViewById<ExtendedFloatingActionButton>(R.id.fabAddCita)

// Actualizamos el constructor con las dos acciones (Borrar y Editar)
        adapter = AppointmentAdapter(
            lista = emptyList(),
            onBorrarClick = { cita ->
                com.azahara.proyecto_final_azahara.alarm.AlarmHelper(requireContext())
                    .cancelarAlarmaCita(cita)
                viewModel.borrarCita(cita)
                Toast.makeText(requireContext(), "Cita eliminada", Toast.LENGTH_SHORT).show()
            },
            onItemClick = { cita ->
                val bundle = Bundle().apply {
                    putString("CITA_ID_EDITAR", cita.id) // <- CORREGIDO
                }
                findNavController().navigate(R.id.action_appointmentList_to_addAppointment, bundle)
            }
        )
        rvCitas.adapter = adapter

        // Navegar al formulario de Nueva Cita al pulsar el botón flotante
        fabAddCita.setOnClickListener {
            findNavController().navigate(R.id.action_appointmentList_to_addAppointment)
        }

        // Observar la lista de citas en tiempo real desde Room
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.citasActivas.collect { lista ->
                    adapter.actualizarDatos(lista)
                }
            }
        }
    }
}