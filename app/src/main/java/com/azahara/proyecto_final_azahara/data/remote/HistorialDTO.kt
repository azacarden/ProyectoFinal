package com.azahara.proyecto_final_azahara.data.remote

/**
 * DTO para volcar el cumplimiento de las tomas a la nube.
 */
data class HistorialDTO(
    val usuarioIdLocal: Int = 0,
    val horarioIdLocal: Int = 0, // Enlaza de forma precisa con el ID del horario atómico
    val fechaHoraReal: Long = 0L,
    val estado: String = "" // "Tomada", "Olvidada", "Omitida"
)