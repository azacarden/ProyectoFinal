package com.azahara.proyecto_final_azahara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azahara.proyecto_final_azahara.model.Medicamento
import com.azahara.proyecto_final_azahara.repository.MedicationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel para la Medicación.
 * Conecta las pantallas del pastillero con el MedicationRepository de forma reactiva.
 */
class MedicationViewModel(private val medicationRepository: MedicationRepository) : ViewModel() {

    // Convertimos el Flow en un StateFlow seguro para el ciclo de vida de la UI
    val medicamentos: StateFlow<List<Medicamento>> = medicationRepository.obtenerMedicamentosActivos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}