package com.azahara.proyecto_final_azahara.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Repositorio de Usuarios.
 * Actuará como la "Single Source of Truth" coordinando Room y Firebase.
 */
class UserRepository {

    // Más adelante, pasaremos la base de datos local (Room) por aquí

    // Cumpliendo el requisito técnico: exponemos datos de forma reactiva con Flow
    fun obtenerUsuarioActual(): Flow<String> {
        // TODO: Leer de la base de datos local cuando esté implementada en la Fase 2
        return emptyFlow()
    }
}