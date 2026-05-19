package com.azahara.proyecto_final_azahara.repository

import com.azahara.proyecto_final_azahara.data.local.UsuarioDao
import com.azahara.proyecto_final_azahara.model.Usuario
import com.azahara.proyecto_final_azahara.utils.CryptoUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository(
    private val usuarioDao: UsuarioDao,
    private val firebaseAuth: FirebaseAuth
) {

    /**
     * Valida el login de forma 100% offline aplicando criptografía SHA-256.
     */
    suspend fun iniciarSesionLocal(nombre: String, contrasenaPlana: String): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Transformamos la contraseña introducida al mismo formato hash guardado en Room
                val hashBuscado = CryptoUtils.sha256(contrasenaPlana)

                // 2. Consultamos la base de datos de forma segura
                val usuarioEncontrado = usuarioDao.obtenerUsuarioPorNombre(nombre)

                if (usuarioEncontrado != null && usuarioEncontrado.contrasenaHash == hashBuscado) {
                    Result.success(usuarioEncontrado)
                } else {
                    Result.failure(Exception("Usuario o contraseña incorrectos."))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Error en la base de datos local: ${e.localizedMessage}"))
            }
        }
    }

    suspend fun registrarUsuarioLocalYSincronizar(
        nombre: String,
        correo: String,
        contrasenaPlana: String,
        rol: String
    ): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                val contrasenaHash = CryptoUtils.sha256(contrasenaPlana)

                val nuevoUsuario = Usuario(
                    nombreUsuario = nombre,
                    correo = correo,
                    contrasenaHash = contrasenaHash,
                    rol = rol
                )

                val idGenerado = usuarioDao.insertUsuario(nuevoUsuario)
                var usuarioGuardado = nuevoUsuario.copy(id = idGenerado.toInt())

                try {
                    val authResult = firebaseAuth.createUserWithEmailAndPassword(correo, contrasenaPlana).await()
                    val firebaseUser = authResult.user

                    if (firebaseUser != null) {
                        usuarioGuardado = usuarioGuardado.copy(firebaseUid = firebaseUser.uid)
                        usuarioDao.updateUsuario(usuarioGuardado)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("UserRepository", "Sincronización diferida o fallida: ${e.message}")
                }

                Result.success(usuarioGuardado)
            } catch (e: Exception) {
                Result.failure(Exception("Error al registrar en base de datos local: ${e.localizedMessage}"))
            }
        }
    }
}
