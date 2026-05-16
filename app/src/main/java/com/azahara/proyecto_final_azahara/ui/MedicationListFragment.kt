package com.azahara.proyecto_final_azahara.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.azahara.proyecto_final_azahara.R
import com.azahara.proyecto_final_azahara.data.local.AppDatabase
import com.azahara.proyecto_final_azahara.repository.MedicationRepository
import com.azahara.proyecto_final_azahara.viewmodel.MedicationViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MedicationListFragment : Fragment(R.layout.fragment_medication_list) {

    private lateinit var viewModel: MedicationViewModel
    private lateinit var adapter: MedicationAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inyección de dependencias manual idéntica a tus componentes
        val database = AppDatabase.getDatabase(requireContext())
        val repository = MedicationRepository(database.medicamentoDao(), FirebaseFirestore.getInstance())
        viewModel = MedicationViewModel(repository)

        // 2. Configurar el RecyclerView
        val rvMedicamentos = view.findViewById<RecyclerView>(R.id.rvMedicamentos)
        adapter = MedicationAdapter()
        rvMedicamentos.adapter = adapter

        // 3. Recogida reactiva del StateFlow (Requisito técnico)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.medicamentos.collect { listaPastillas ->
                    // Cada vez que Room sufra un cambio, este bloque se ejecuta solo
                    adapter.actualizarDatos(listaPastillas)
                }
            }
        }
    }
}