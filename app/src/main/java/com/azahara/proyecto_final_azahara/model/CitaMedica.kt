package com.azahara.proyecto_final_azahara.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "citas_medicas")
data class CitaMedica(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,          // Ej: "Revisión Anual"
    val especialista: String,    // Ej: "Cardiólogo - Dr. García"
    val fechaHora: Long,         // Guardamos la fecha y hora exacta en formato Timestamp (milisegundos)
    val notas: String,           // Ej: "Llevar analítica en ayunas"
    val recordatorioPrevio: Int  // Minutos de antelación para la alarma (Ej: 60 para avisar 1h antes)
)