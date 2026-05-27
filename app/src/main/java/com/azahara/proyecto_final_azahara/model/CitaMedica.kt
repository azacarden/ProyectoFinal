package com.azahara.proyecto_final_azahara.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "citas_medicas")
data class CitaMedica(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val motivo: String,
    val medico: String,
    val especialidad: String,
    val centroHospital: String = "",
    val fechaHora: Long,
    val notas: String,
    val recordatorioPrevio: Int,
    val creadoPorNombre: String = "Paciente"
)