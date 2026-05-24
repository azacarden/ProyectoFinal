package com.azahara.proyecto_final_azahara.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.azahara.proyecto_final_azahara.data.local.Usuario
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
        Index(value = ["horarioId"])  // <--- OBLIGATORIO: Indexa las búsquedas relacionales
    ]
)
data class Historial(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val usuarioId: String, // Ahora apunta al UID de tipo String de la tabla usuarios
    val medicamentoId: String, // Ahora apunta al ID de tipo String del medicamento tomado
    val fechaHora: Long,
    val estado: String // "Tomada", "Olvidada", "Omitida"
)