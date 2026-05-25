package com.azahara.proyecto_final_azahara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azahara.proyecto_final_azahara.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val nombreUsuario: String, val rol: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

// CORREGIDO: Inyectamos el repositorio por constructor
class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun registrarUsuario(usuario: String, correo: String, contrasena: String, rol: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // Delegamos toda la lógica al repositorio
            val resultado = userRepository.registrarUsuarioLocalYSincronizar(usuario, correo, contrasena, rol)

            resultado.fold(
                onSuccess = { usuarioGuardado ->
                    _authState.value = AuthState.Success(
                        nombreUsuario = usuarioGuardado.nombreUsuario,
                        rol = usuarioGuardado.rol
                    )
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Error desconocido en el registro")
                }
            )
        }
    }

    fun loginConUsuario(usuario: String, contrasena: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // Delegamos toda la lógica al repositorio
            val resultado = userRepository.iniciarSesionLocal(usuario, contrasena)

            resultado.fold(
                onSuccess = { usuarioLogueado ->
                    _authState.value = AuthState.Success(
                        nombreUsuario = usuarioLogueado.nombreUsuario,
                        rol = usuarioLogueado.rol
                    )
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Usuario o contraseña incorrectos")
                }
            )
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}