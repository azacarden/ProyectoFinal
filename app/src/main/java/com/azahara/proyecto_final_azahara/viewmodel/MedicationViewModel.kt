package com.azahara.proyecto_final_azahara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azahara.proyecto_final_azahara.model.MedicamentoConHorarios
import com.azahara.proyecto_final_azahara.repository.MedicationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MedicationViewModel(private val medicationRepository: MedicationRepository) : ViewModel() {

    val medicamentos: StateFlow<List<MedicamentoConHorarios>> = medicationRepository.obtenerMedicamentosActivos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}