package com.azahara.proyecto_final_azahara.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val id: String, // Vinculado directamente al UID devuelto por Firebase Auth
    val nombre: String,
    val email: String,
    val rol: String, // "Paciente" o "Cuidador"
    val codigoVinculacion: String? = null
)