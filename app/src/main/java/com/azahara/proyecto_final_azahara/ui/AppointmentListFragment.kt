package com.azahara.proyecto_final_azahara.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.azahara.proyecto_final_azahara.R
import com.azahara.proyecto_final_azahara.alarm.AlarmHelper
import com.azahara.proyecto_final_azahara.data.local.AppDatabase
import com.azahara.proyecto_final_azahara.repository.AppointmentRepository
import com.azahara.proyecto_final_azahara.viewmodel.AppointmentViewModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class AppointmentListFragment : Fragment(R.layout.fragment_appointment_list) {

    // 1. Conexión del ciclo oficial de Android con la factoría de repositorios
    private val viewModel: AppointmentViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val dao = AppDatabase.getDatabase(requireContext()).citaMedicaDao()
                val firestore = FirebaseFirestore.getInstance()
                val repo = AppointmentRepository(dao, firestore)
                @Suppress("UNCHECKED_CAST")
                return AppointmentViewModel(dao, repo) as T
            }
        }
    }

    private lateinit var adapter: AppointmentAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvCitas = view.findViewById<RecyclerView>(R.id.rvCitas)
        val fabAddCita = view.findViewById<ExtendedFloatingActionButton>(R.id.fabAddCita)

        adapter = AppointmentAdapter(
            lista = emptyList(),
            onBorrarClick = { cita ->
                // 2. Obtenemos el ID unificado para borrar el elemento de Firestore de forma precisa
                val prefs = requireContext().getSharedPreferences("SesionUsuario", android.content.Context.MODE_PRIVATE)
                val miUid = prefs.getString("firebase_uid", "") ?: ""

                AlarmHelper(requireContext()).cancelarAlarmaCita(cita)
                viewModel.borrarCita(cita, miUid)
                Toast.makeText(requireContext(), "Cita eliminada", Toast.LENGTH_SHORT).show()
            },
            onItemClick = { cita ->
                val bundle = Bundle().apply {
                    putString("CITA_ID_EDITAR", cita.id)
                }
                findNavController().navigate(R.id.action_appointmentList_to_addAppointment, bundle)
            }
        )
        rvCitas.adapter = adapter

        fabAddCita.setOnClickListener {
            findNavController().navigate(R.id.action_appointmentList_to_addAppointment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.citasActivas.collect { lista ->
                    adapter.actualizarDatos(lista)
                }
            }
        }
    }
}