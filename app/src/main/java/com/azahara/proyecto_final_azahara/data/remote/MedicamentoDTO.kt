package com.azahara.proyecto_final_azahara.data.remote

data class MedicamentoDTO(
    val idLocal: String = "", // Cambiado a String para alojar el UUID del dispositivo
    val nombre: String = "",
    val mensajePersonalizado: String = "",
    val horarios: List<String> = emptyList(),
    val frecuencia: String = "Diaria",
    val urlProspecto: String? = null,
    val contraindicaciones: String? = null
)