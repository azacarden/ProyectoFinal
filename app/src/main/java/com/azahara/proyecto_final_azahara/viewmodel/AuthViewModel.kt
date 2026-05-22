package com.azahara.proyecto_final_azahara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val nombreUsuario: String, val rol: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun registrarUsuario(usuario: String, correo: String, contrasena: String, rol: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val userCheck = db.collection("usuarios").whereEqualTo("nombreUsuario", usuario).get().await()
                if (!userCheck.isEmpty) {
                    _authState.value = AuthState.Error("Ese nombre de usuario ya está cogido")
                    return@launch
                }

                val result = auth.createUserWithEmailAndPassword(correo, contrasena).await()
                val uid = result.user?.uid ?: throw Exception("Error al crear usuario")

                val perfil = hashMapOf(
                    "nombreUsuario" to usuario,
                    "correo" to correo,
                    "rol" to rol,
                    "fechaRegistro" to System.currentTimeMillis()
                )
                db.collection("usuarios").document(uid).set(perfil).await()

                _authState.value = AuthState.Success(nombreUsuario = usuario, rol = rol)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Error en el registro")
            }
        }
    }

    fun loginConUsuario(usuario: String, contrasena: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val querySnapshot = db.collection("usuarios")
                    .whereEqualTo("nombreUsuario", usuario)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    _authState.value = AuthState.Error("El usuario no existe")
                    return@launch
                }

                val documento = querySnapshot.documents.first()
                val correoReal = documento.getString("correo") ?: ""

                val rolReal = documento.getString("rol") ?: "Paciente"

                auth.signInWithEmailAndPassword(correoReal, contrasena).await()

                _authState.value = AuthState.Success(nombreUsuario = usuario, rol = rolReal)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Usuario o contraseña incorrectos")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}