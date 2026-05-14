package com.azahara.proyecto_final_azahara.viewmodel

import androidx.lifecycle.ViewModel
import com.azahara.proyecto_final_azahara.repository.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel para la Medicación.
 * Conecta las pantallas del pastillero con el MedicationRepository.
 */
class MedicationViewModel(private val medicationRepository: MedicationRepository) : ViewModel() {

    // 1. Estado privado: Almacena una lista de textos (los medicamentos)
    // Inicialmente empieza como una lista vacía: emptyList()
    private val _medicamentos = MutableStateFlow<List<String>>(emptyList())

    // 2. Estado público: La pantalla (XML) observará esta variable para dibujar la lista
    val medicamentos: StateFlow<List<String>> = _medicamentos.asStateFlow()

    /**
     * Función que llamaremos cuando la pantalla principal se abra
     */
    fun cargarMedicamentos() {
        // De momento, metemos datos "de mentira" (Mock data) para ver que funciona.
        // En la Fase 2, aquí nos conectaremos al MedicationRepository real.
        _medicamentos.value = listOf("Paracetamol 500mg", "Sintrom", "Pastilla Azul del Colesterol")
    }
}