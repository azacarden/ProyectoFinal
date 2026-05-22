package com.azahara.proyecto_final_azahara.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicamentos")
data class Medicamento(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val mensajePersonalizado: String,
    val horaToma: String,
    val frecuencia: String,
    val diaEspecifico: String? = null,
    val urlProspecto: String? = null,
    val contraindicaciones: String? = null
)