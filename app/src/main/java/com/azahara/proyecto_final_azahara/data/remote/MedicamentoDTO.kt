package com.azahara.proyecto_final_azahara.data.remote

data class MedicamentoDTO(
    val idLocal: String = "",
    val nombre: String = "",
    val mensajePersonalizado: String = "",
    val horarios: List<String> = emptyList(),
    val frecuencia: String = "Diaria",
    val diaEspecifico: String? = null,
    val urlProspecto: String? = null,
    val contraindicaciones: String? = null
)