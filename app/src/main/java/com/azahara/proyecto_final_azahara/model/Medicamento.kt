package com.azahara.proyecto_final_azahara.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "medicamentos")
data class Medicamento(
    @PrimaryKey val id: String = UUID.randomUUID().toString(), // Genera un identificador único universal por defecto
    val nombre: String,
    val mensajePersonalizado: String,
    val horaToma: List<String>,
    val frecuencia: String = "Diaria",
    val urlProspecto: String? = null,
    val contraindicaciones: String? = null
)