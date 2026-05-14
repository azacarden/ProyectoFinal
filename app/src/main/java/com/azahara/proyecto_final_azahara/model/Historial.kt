package com.azahara.proyecto_final_azahara.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

/**
 * Entidad que registra el historial de tomas de medicación
 */
@Entity(
    tableName = "historial_tomas",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Medicamento::class,
            parentColumns = ["id"],
            childColumns = ["medicamentoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Historial(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Relación con el usuario que debe tomar la medicina
    val usuarioId: Int,

    // Relación con el medicamento específico
    val medicamentoId: Int,

    // El momento exacto en el que el usuario marcó la toma
    val fechaHoraReal: Long,

    // Estado de la toma (tomada/no tomada)
    val estado: String
)