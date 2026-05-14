package com.azahara.proyecto_final_azahara.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa la tabla de medicamentos en la base de datos
 */
@Entity(tableName = "medicamentos")
data class Medicamento(
    // Identificador único para cada medicamento, autogenerado por Room
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Nombre del medicamento
    val nombre: String,

    // String para la hora de la toma planificada
    val horaToma: String,

    // Mensaje personalizado para ayudar a identificar la pastilla
    // Ejemplo: 'La pastilla azul del colesterol'
    val mensajePersonalizado: String
)