package com.azahara.proyecto_final_azahara.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "horarios_medicamento",
    foreignKeys = [
        ForeignKey(
            entity = Medicamento::class,
            parentColumns = ["id"],
            childColumns = ["medicamentoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicamentoId"])]
)
data class HorarioMedicamento(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val medicamentoId: String,
    val horaToma: String
)