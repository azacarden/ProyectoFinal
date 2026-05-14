package com.azahara.proyecto_final_azahara.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Aquí se le dice a Room que esta clase se va a convertir en una tabla de SQLite llamada "usuarios"
// @Entity transforma la clase en una tabla relacional en SQLite
@Entity(tableName = "usuarios")
data class Usuario(
    // Creamos la clave primaria (ID). Al poner autoGenerate = true, SQLite contará por nosotros
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Campos para la autenticación local segura (Offline-Ready)
    val nombreUsuario: String,
    val contrasena: String,

    // Aquí guardaremos el rol del usuario, si es "Paciente" o "Cuidador"
    val rol: String
)
