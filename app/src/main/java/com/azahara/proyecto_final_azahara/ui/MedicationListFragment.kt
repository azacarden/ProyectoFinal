package com.azahara.proyecto_final_azahara.ui

import android.content.Intent
import android.net.Uri
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
import com.azahara.proyecto_final_azahara.repository.MedicationRepository
import com.azahara.proyecto_final_azahara.viewmodel.MedicationViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedicationListFragment : Fragment(R.layout.fragment_medication_list) {

    private lateinit var viewModel: MedicationViewModel
    private lateinit var adapter: MedicationAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val dao = database.medicamentoDao()
        val repository = MedicationRepository(dao, FirebaseFirestore.getInstance())
        viewModel = MedicationViewModel(repository)

        // ¡NUEVO! Lógica de Cuidador: Si venimos de pulsar un paciente, descargamos SUS datos a nuestra base local
        val pacienteUid = arguments?.getString("PACIENTE_UID")
        if (pacienteUid != null) {
            Toast.makeText(requireContext(), "Sincronizando pastillas de $pacienteUid...", Toast.LENGTH_SHORT).show()
            viewLifecycleOwner.lifecycleScope.launch {
                repository.escucharMedicacionPaciente(pacienteUid).collect {
                    // Room se actualiza solo en background, y el Flow normal de abajo pintará las pastillas solas
                }
            }
        }

        val rvMedicamentos = view.findViewById<RecyclerView>(R.id.rvMedicamentos)

        adapter = MedicationAdapter(
            lista = emptyList(),
            onBorrarClick = { medicamento ->
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) { dao.deleteMedicamento(medicamento) }
                    Toast.makeText(requireContext(), "${medicamento.nombre} eliminado", Toast.LENGTH_SHORT).show()
                }
            },
            onItemClick = { medicamento ->
                val bundle = Bundle().apply {
                    putInt("MEDICAMENTO_ID_EDITAR", medicamento.id)
                }
                findNavController().navigate(R.id.action_medicationList_to_addMedication, bundle)
            },
            onProspectoClick = { url ->
                // ¡LANZAMOS EL NAVEGADOR DEL MÓVIL CON EL PROSPECTO GUARDADO EN ROOM!
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