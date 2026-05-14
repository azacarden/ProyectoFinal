package com.azahara.proyecto_final_azahara.data.remote

/**
 * Objeto de Transferencia de Datos para sincronizar los Medicamentos con Firebase.
 */
data class MedicamentoDTO(
    // Guardamos el ID que tenía en la base de datos local para mantener la referencia
    val idLocal: Int = 0,
    val nombre: String = "",
    val horaToma: String = "",
    val mensajePersonalizado: String = ""
)