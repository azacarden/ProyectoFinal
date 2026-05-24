// app/src/main/java/com/azahara/proyecto_final_azahara/model/CitaMedica.kt
package com.azahara.proyecto_final_azahara.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "citas_medicas")
data class CitaMedica(
    @PrimaryKey val id: String = UUID.randomUUID().toString(), // Garantiza un ID seguro antes de sincronizar con Firebase
    val motivo: String,
    val medico: String,
    val especialidad: String,  // Nuevo
    val fechaHora: Long,
    val notas: String,
    val recordatorioPrevio: Int
)