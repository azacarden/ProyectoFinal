package com.azahara.proyecto_final_azahara.data.remote

data class CitaMedicaDTO(
    val idLocal: String = "",
    val motivo: String = "",
    val medico: String = "",
    val especialidad: String = "",
    val centroHospital: String = "",
    val fechaHora: Long = 0L,
    val notas: String = "",
    val recordatorioPrevio: Int = 60,
    val creadoPorNombre: String = "Paciente"
)