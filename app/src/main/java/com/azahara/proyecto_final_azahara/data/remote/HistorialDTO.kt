package com.azahara.proyecto_final_azahara.data.remote

data class HistorialDTO(
    val usuarioIdLocal: String = "",
    val medicamentoIdLocal: String = "",
    val fechaHoraReal: Long = 0L,
    val estado: String = ""
)