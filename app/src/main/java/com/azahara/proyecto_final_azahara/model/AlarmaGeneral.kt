package com.azahara.proyecto_final_azahara.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarmas_generales")
data class AlarmaGeneral(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val fechaHora: Long,
    val descripcion: String,
    val activa: Boolean = true
)