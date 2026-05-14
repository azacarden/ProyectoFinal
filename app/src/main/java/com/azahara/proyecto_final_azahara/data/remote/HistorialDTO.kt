package com.azahara.proyecto_final_azahara.data.remote

/**
 * Objeto de Transferencia de Datos para sincronizar las tomas realizadas con Firebase.
 */
data class HistorialDTO(
    val usuarioUid: String = "",
    val medicamentoIdLocal: Int = 0,
    val fechaHoraReal: Long = 0L,
    val estado: String = ""
)