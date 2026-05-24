// app/src/main/java/com/azahara/proyecto_final_azahara/model/CitaMedica.kt
package com.azahara.proyecto_final_azahara.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "citas_medicas")
data class CitaMedica(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val motivo: String,
    val medico: String,
    val especialidad: String,  // Nuevo
    val fechaHora: Long,
    val notas: String,
    val recordatorioPrevio: Int
)