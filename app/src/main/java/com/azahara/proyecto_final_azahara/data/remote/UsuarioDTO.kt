package com.azahara.proyecto_final_azahara.data.remote

/**
 * Objeto de Transferencia de Datos para sincronizar el Usuario con Firebase.
 */
data class UsuarioDTO(
    // En Firebase no usaremos el ID numérico de Room, sino el UID único de Firebase Auth
    val uid: String = "",
    val nombreUsuario: String = "",
    val rol: String = ""
)