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

        val pacienteUid = arguments?.getString("PACIENTE_UID")
        val pacienteNombre = arguments?.getString("PACIENTE_NOMBRE")

        if (pacienteUid != null) {
            val firestore = FirebaseFirestore.getInstance()
            val repoSync = AppointmentRepository(AppDatabase.getDatabase(requireContext()).citaMedicaDao(), firestore)
            viewLifecycleOwner.lifecycleScope.launch {
                repoSync.escucharCitasPaciente(pacienteUid).collect {}
            }
        }

        val rvCitas = view.findViewById<RecyclerView>(R.id.rvCitas)
        val fabAddCita = view.findViewById<ExtendedFloatingActionButton>(R.id.fabAddCita)

        adapter = AppointmentAdapter(
            lista = emptyList(),
            onBorrarClick = { cita ->
                val prefs = requireContext().getSharedPreferences("SesionUsuario", android.content.Context.MODE_PRIVATE)
                val miUidLocal = prefs.getString("firebase_uid", "") ?: ""
                val targetUid = pacienteUid ?: miUidLocal

                AlarmHelper(requireContext()).cancelarAlarmaCita(cita)
                viewModel.borrarCita(cita, targetUid)
                Toast.makeText(requireContext(), "Cita eliminada", Toast.LENGTH_SHORT).show()
            },
            onItemClick = { cita ->
                val bundle = Bundle().apply {
                    putString("CITA_ID_EDITAR", cita.id)
                    if (pacienteUid != null) putString("PACIENTE_UID", pacienteUid)
                    if (pacienteNombre != null) putString("PACIENTE_NOMBRE", pacienteNombre)
                }
                findNavController().navigate(R.id.action_appointmentList_to_addAppointment, bundle)
            }
        )
        rvCitas.adapter = adapter

        fabAddCita.setOnClickListener {
            val bundle = Bundle().apply {
                if (pacienteUid != null) putString("PACIENTE_UID", pacienteUid)
                if (pacienteNombre != null) putString("PACIENTE_NOMBRE", pacienteNombre)
            }
            findNavController().navigate(R.id.action_appointmentList_to_addAppointment, bundle)
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