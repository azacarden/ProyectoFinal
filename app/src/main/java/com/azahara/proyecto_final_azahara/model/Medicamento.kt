package com.azahara.proyecto_final_azahara.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "medicamentos")
data class Medicamento(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val mensajePersonalizado: String,
    val frecuencia: String = "Diaria",
    val diaEspecifico: String? = null,
    val urlProspecto: String? = null,
    val contraindicaciones: String? = null,
    val pendienteSincronizacion: Boolean = false,
    val marcadoParaEliminar: Boolean = false
)