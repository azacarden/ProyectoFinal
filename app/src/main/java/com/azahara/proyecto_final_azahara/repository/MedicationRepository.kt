package com.azahara.proyecto_final_azahara.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Repositorio de Medicación.
 * Coordinará las pastillas y alarmas entre la base de datos y la nube.
 */
class MedicationRepository {

    // Cumpliendo el requisito técnico: exponemos flujos de datos (Flow)
    fun obtenerMedicamentosActivos(): Flow<List<String>> {
        // TODO: Devolver la lista real de Room cuando esté implementada
        return emptyFlow()
    }
}