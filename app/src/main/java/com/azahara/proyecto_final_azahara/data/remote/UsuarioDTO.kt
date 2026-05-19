package com.azahara.proyecto_final_azahara.data.remote

/**
 * DTO para registrar los perfiles y roles en Cloud Firestore.
 */
data class UsuarioDTO(
    val uid: String = "",
    val nombreUsuario: String = "",
    val correo: String = "",
    val rol: String = "" // "Paciente" o "Cuidador"
)