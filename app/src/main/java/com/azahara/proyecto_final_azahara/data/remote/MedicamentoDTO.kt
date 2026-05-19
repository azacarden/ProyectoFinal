package com.azahara.proyecto_final_azahara.data.remote

/**
 * DTO para transferir y escuchar la medicación en la nube (Firestore).
 * Incluye los horarios embebidos de forma nativa como una lista NoSQL.
 */
data class MedicamentoDTO(
    val idLocal: Int = 0,
    val nombre: String = "",
    val mensajePersonalizado: String = "",
    val horarios: List<String> = emptyList(), // Ej: ["08:00", "20:00"]
    val urlProspecto: String? = null,
    val contraindicaciones: String? = null
)