package com.azahara.proyecto_final_azahara.model

import androidx.room.Embedded
import androidx.room.Relation

data class MedicamentoConHorarios(
    @Embedded val medicamento: Medicamento,
    @Relation(
        parentColumn = "id",
        entityColumn = "medicamentoId"
    )
    val horarios: List<HorarioMedicamento>
)