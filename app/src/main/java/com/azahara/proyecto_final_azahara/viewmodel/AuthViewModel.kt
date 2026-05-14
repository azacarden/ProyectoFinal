package com.azahara.proyecto_final_azahara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azahara.proyecto_final_azahara.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la Autenticación (Login y Registro).
 */
class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _estadoLogin = MutableStateFlow("Esperando credenciales...")
    val estadoLogin: StateFlow<String> = _estadoLogin.asStateFlow()

    fun iniciarSesion(nombreUsuario: String, contrasena: String) {
        // Validaciones en el cliente para evitar consultas innecesarias a la base de datos
        if (nombreUsuario.isBlank() || contrasena.isBlank()) {
            _estadoLogin.value = "Error: El usuario y la contraseña son obligatorios."
            return
        }

        _estadoLogin.value = "Comprobando datos de forma segura..."

        // Ejecución asíncrona sin bloquear la pantalla
        viewModelScope.launch {
            val resultado = userRepository.iniciarSesionLocal(nombreUsuario, contrasena)

            // 3. Evaluamos el resultado de la operación
            resultado.fold(
                onSuccess = { usuario ->
                    _estadoLogin.value = "¡Bienvenido, ${usuario.nombreUsuario}! (Rol: ${usuario.rol})"
                },
                onFailure = { excepcion ->
                    _estadoLogin.value = "Acceso denegado: ${excepcion.message}"
                }
            )
        }
    }
}