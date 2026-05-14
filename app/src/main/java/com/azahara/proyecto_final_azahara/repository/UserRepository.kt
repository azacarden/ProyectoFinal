package com.azahara.proyecto_final_azahara.repository

import com.azahara.proyecto_final_azahara.data.local.UsuarioDao
import com.azahara.proyecto_final_azahara.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repositorio de Usuarios.
 * Actuará como la "Single Source of Truth" coordinando Room y Firebase.
 */
class UserRepository(
    private val usuarioDao: UsuarioDao,
    // Ahora el repositorio también tiene acceso a la nube
    private val firebaseAuth: FirebaseAuth
) {

    /**
     * Registra al usuario localmente y vuelca los datos a la nube.
     */
    suspend fun registrarUsuarioLocalYSincronizar(nombre: String, contrasena: String, rol: String): Result<Usuario> {

        // Usamos Dispatchers.IO para operar en hilos de ejecución
        // secundarios sin bloquear la interfaz de usuario (UI).
        return withContext(Dispatchers.IO) {
            try {
                // Guardar en la base de datos local (Room - Offline)
                val nuevoUsuario = Usuario(nombreUsuario = nombre, contrasena = contrasena, rol = rol)
                val idLocal = usuarioDao.insertUsuario(nuevoUsuario)
                val usuarioGuardado = nuevoUsuario.copy(id = idLocal.toInt())

                // Sincronización asíncrona hacia Firebase Auth
                // Firebase Auth exige obligatoriamente un formato de correo electrónico (ej: a@a.com).
                // Si el usuario se llama "Juan", internamente le creamos un correo ficticio para la nube.
                val emailParaFirebase = if (nombre.contains("@")) nombre else "$nombre@pastillero.app"

                // Ejecutamos la petición a la red. El comando .await() pausa esta corrutina
                // hasta que Firebase responde, pero al estar en Dispatchers.IO, el móvil no se congela.
                firebaseAuth.createUserWithEmailAndPassword(emailParaFirebase, contrasena).await()

                // Si tanto Room como Firebase han ido bien, devolvemos el éxito
                Result.success(usuarioGuardado)

            } catch (e: Exception) {
                // Si falla la red o la base de datos, capturamos el error
                Result.failure(Exception("Error en registro/sincronización: ${e.localizedMessage}"))
            }
        }
    }

    /**
     * Validar el login contra la base de datos local (Offline).
     */
    suspend fun iniciarSesionLocal(nombre: String, contrasena: String): Result<Usuario> {
        return try {
            val usuarioEncontrado = usuarioDao.login(nombre, contrasena)
            if (usuarioEncontrado != null) {
                Result.success(usuarioEncontrado)
            } else {
                Result.failure(Exception("Credenciales incorrectas o usuario no registrado."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de base de datos: ${e.localizedMessage}"))
        }
    }

    fun obtenerUsuarioActual(): Flow<String> {
        return emptyFlow()
    }
}