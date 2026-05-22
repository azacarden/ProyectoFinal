package com.azahara.proyecto_final_azahara.data.remote

data class MedicamentoDTO(
    val idLocal: Int = 0,
    val nombre: String = "",
    val mensajePersonalizado: String = "",
    val horarios: List<String> = emptyList(),
    val frecuencia: String = "Diaria",
    val urlProspecto: String? = null,
    val contraindicaciones: String? = null
)