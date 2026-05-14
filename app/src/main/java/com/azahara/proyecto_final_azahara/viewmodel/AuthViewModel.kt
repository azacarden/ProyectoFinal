package com.azahara.proyecto_final_azahara.viewmodel

import androidx.lifecycle.ViewModel
import com.azahara.proyecto_final_azahara.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel para la Autenticación (Login y Registro).
 * Conecta la interfaz de usuario (UI) con el UserRepository.
 */
class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    // 1. Estado interno (privado) que nosotros podemos modificar aquí dentro
    private val _estadoLogin = MutableStateFlow("Esperando credenciales...")

    // 2. Estado público (solo lectura) que la pantalla (UI) va a observar
    val estadoLogin: StateFlow<String> = _estadoLogin.asStateFlow()

    /**
     * Función que llamará el botón de "Iniciar Sesión" desde la pantalla XML
     */
    fun iniciarSesion(nombreUsuario: String, contrasena: String) {
        // De momento, solo cambiamos el estado para ver que funciona.
        // Más adelante, aquí llamaremos al userRepository.
        if (nombreUsuario.isNotEmpty() && contrasena.isNotEmpty()) {
            _estadoLogin.value = "Iniciando sesión para: $nombreUsuario"
        } else {
            _estadoLogin.value = "Error: Faltan datos"
        }
    }
}