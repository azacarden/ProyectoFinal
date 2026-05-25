package com.azahara.proyecto_final_azahara.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.azahara.proyecto_final_azahara.model.Usuario
import java.util.UUID

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
        Index(value = ["horarioId"])
    ]
)
data class Historial(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val usuarioId: String,
    val medicamentoId: String,
    val horarioId: String,
    val fechaHora: Long,
    val estado: String
)