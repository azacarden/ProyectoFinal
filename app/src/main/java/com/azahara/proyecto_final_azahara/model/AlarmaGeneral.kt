package com.azahara.proyecto_final_azahara.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarmas_generales")
data class AlarmaGeneral(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,       // Ej: "Recoger analítica en el centro de salud"
    val fechaHora: Long,      // Guardamos la fecha y hora exacta en milisegundos (Timestamp)
    val descripcion: String,  // Detalles extra
    val activa: Boolean = true // Permite al usuario encender/apagar la alarma con un interruptor
)