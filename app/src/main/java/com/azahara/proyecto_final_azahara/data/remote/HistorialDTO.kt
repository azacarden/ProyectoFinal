package com.azahara.proyecto_final_azahara.data.remote

data class HistorialDTO(
    val usuarioIdLocal: String = "", // Guarda el UID textual del usuario en Firebase
    val medicamentoIdLocal: String = "", // Almacena el UUID del medicamento afectado
    val fechaHoraReal: Long = 0L,
    val estado: String = ""
)