package com.azahara.proyecto_final_azahara.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.azahara.proyecto_final_azahara.data.local.AppDatabase
import com.azahara.proyecto_final_azahara.repository.MedicationRepository
import com.azahara.proyecto_final_azahara.viewmodel.MedicationViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MedicationListFragment : Fragment(R.layout.fragment_medication_list) {

    private val viewModel: MedicationViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(requireContext())
                val dao = database.medicamentoDao()
                val repository = MedicationRepository(dao, FirebaseFirestore.getInstance())
                @Suppress("UNCHECKED_CAST")
                return MedicationViewModel(repository) as T
            }
        }
    }

    private lateinit var adapter: MedicationAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE)
        val miUsuarioId = prefs.getString("firebase_uid", "") ?: ""
        val pacienteUid = arguments?.getString("PACIENTE_UID")
        val pacienteNombre = arguments?.getString("PACIENTE_NOMBRE")

        if (pacienteUid != null) {
            val dao = AppDatabase.getDatabase(requireContext()).medicamentoDao()
            val repository = MedicationRepository(dao, FirebaseFirestore.getInstance())

            viewLifecycleOwner.lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                dao.vaciarTabla()
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repository.escucharMedicacionPaciente(pacienteUid).collect {}
            }
        }

        val rvMedicamentos = view.findViewById<RecyclerView>(R.id.rvMedicamentos)

        adapter = MedicationAdapter(
            onBorrarClick = { wrapper ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val dao = AppDatabase.getDatabase(requireContext()).medicamentoDao()
                    val repository = MedicationRepository(dao, FirebaseFirestore.getInstance())

                    val targetUid = pacienteUid ?: miUsuarioId
                    repository.eliminarMedicamento(wrapper.medicamento.id, targetUid)

                    Toast.makeText(requireContext(), "${wrapper.medicamento.nombre} eliminado", Toast.LENGTH_SHORT).show()
                }
            },
            onItemClick = { wrapper ->
                val bundle = Bundle().apply {
                    putString("MEDICAMENTO_ID_EDITAR", wrapper.medicamento.id)
                    if (pacienteUid != null) putString("PACIENTE_UID", pacienteUid)

                    // Le pasamos el nombre del paciente para que no se reescriba con el del Cuidador
                    if (pacienteNombre != null) putString("PACIENTE_NOMBRE", pacienteNombre)
                }
                findNavController().navigate(R.id.action_medicationList_to_addMedication, bundle)
            },
            onProspectoClick = { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        )

        rvMedicamentos.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.medicamentos.collect { listaPastillas ->
                    adapter.actualizarDatos(listaPastillas)
                }
            }
        }
    }
}