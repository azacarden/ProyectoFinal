package com.azahara.proyecto_final_azahara.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
            entity = HorarioMedicamento::class,
            parentColumns = ["id"],
            childColumns = ["horarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuarioId"]),
        Index(value = ["horarioId"])  // <--- OBLIGATORIO: Indexa las búsquedas relacionales
    ]
)
data class Historial(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val horarioId: Int, // <--- KEY COMPLIANCE: Conecta con la toma exacta del día
    val fechaHoraReal: Long, // Timestamp en milisegundos
    val estado: String // "Tomada", "Olvidada", "Omitida"
)