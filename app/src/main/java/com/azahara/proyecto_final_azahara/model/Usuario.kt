package com.azahara.proyecto_final_azahara.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nombreUsuario: String,
    val correo: String,
    val contrasenaHash: String,
    val rol: String,
    val firebaseUid: String? = null
)