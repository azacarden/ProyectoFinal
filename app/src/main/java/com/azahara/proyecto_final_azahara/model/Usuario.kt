package com.azahara.proyecto_final_azahara.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["nombreUsuario", "correo"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firebaseUid: String? = null,
    val nombreUsuario: String,
    val correo: String,
    val contrasenaHash: String,
    val rol: String
)