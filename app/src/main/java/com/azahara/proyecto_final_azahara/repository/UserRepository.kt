package com.azahara.proyecto_final_azahara.repository

import com.azahara.proyecto_final_azahara.data.local.UsuarioDao
import com.azahara.proyecto_final_azahara.data.remote.UsuarioDTO
import com.azahara.proyecto_final_azahara.model.Usuario
import com.azahara.proyecto_final_azahara.utils.CryptoUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository(
    private val usuarioDao: UsuarioDao,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun iniciarSesionLocal(nombre: String, contrasenaPlana: String): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                val hashBuscado = CryptoUtils.sha256(contrasenaPlana)
                var usuarioEncontrado = usuarioDao.getUsuarioPorNombreSync(nombre)

                // Si el usuario no está en Room
                if (usuarioEncontrado == null) {
                    try {
                        // Busca el perfil en Firestore por el nombre de usuario para obtener su correo
                        val querySnapshot = firestore.collection("usuarios")
                            .whereEqualTo("nombreUsuario", nombre)
                            .get()
                            .await()

                        if (querySnapshot.isEmpty) {
                            return@withContext Result.failure(Exception("El usuario no existe en el sistema."))
                        }

                        val documento = querySnapshot.documents.first()
                        val correoReal = documento.getString("correo") ?: ""
                        val rolReal = documento.getString("rol") ?: "Paciente"

                        // Valida las credenciales contra Firebase Auth
                        val authResult = firebaseAuth.signInWithEmailAndPassword(correoReal, contrasenaPlana).await()
                        val firebaseUser = authResult.user

                        if (firebaseUser != null) {
                            // El usuario es real. Lo descarga de Firestore y reconstruye su Room local inmediatamente
                            val nuevoUsuarioLocal = Usuario(
                                nombreUsuario = nombre,
                                correo = correoReal,
                                contrasenaHash = hashBuscado, // Generamos su hash local
                                rol = rolReal,
                                firebaseUid = firebaseUser.uid
                            )
                            usuarioDao.insertUsuario(nuevoUsuarioLocal)
                            usuarioEncontrado = nuevoUsuarioLocal
                        }
                    } catch (e: Exception) {
                        return@withContext Result.failure(Exception("Usuario o contraseña incorrectos en la red."))
                    }
                }

                // Verificación final de seguridad (Local u Offline)
                if (usuarioEncontrado != null && usuarioEncontrado.contrasenaHash == hashBuscado) {

                    var usuarioActualizado = usuarioEncontrado

                    // Si el usuario existía en Room pero se creó sin internet (no tiene uid de Firebase)
                    if (usuarioActualizado.firebaseUid == null) {
                        try {
                            val authResult = firebaseAuth.createUserWithEmailAndPassword(
                                usuarioActualizado.correo,
                                contrasenaPlana
                            ).await()

                            val firebaseUser = authResult.user
                            if (firebaseUser != null) {
                                usuarioActualizado = usuarioActualizado.copy(firebaseUid = firebaseUser.uid)
                                usuarioDao.updateUsuario(usuarioActualizado)

                                val dto = UsuarioDTO(
                                    uid = firebaseUser.uid,
                                    nombreUsuario = usuarioActualizado.nombreUsuario,
                                    correo = usuarioActualizado.correo,
                                    rol = usuarioActualizado.rol
                                )
                                firestore.collection("usuarios").document(firebaseUser.uid).set(dto).await()
                            }
                        } catch (e: Exception) {
                            android.util.Log.w("UserRepository", "Sincronización diferida en login fallida.")
                        }
                    } else {
                        // Si ya está totalmente sincronizado, renueva sesión en la nube de fondo si hay red
                        try {
                            firebaseAuth.signInWithEmailAndPassword(usuarioActualizado.correo, contrasenaPlana).await()
                        } catch (e: Exception) {
                            android.util.Log.w("UserRepository", "Mantenemos sesión puramente local (Offline mode).")
                        }
                    }

                    Result.success(usuarioActualizado)
                } else {
                    Result.failure(Exception("Usuario o contraseña incorrectos."))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Error en el sistema de autenticación: ${e.localizedMessage}"))
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
                val existe = usuarioDao.getUsuarioPorNombreSync(nombre)
                if (existe != null) {
                    return@withContext Result.failure(Exception("El nombre de usuario ya está en uso."))
                }

                val contrasenaHash = CryptoUtils.sha256(contrasenaPlana)

                var nuevoUsuario = Usuario(
                    nombreUsuario = nombre,
                    correo = correo,
                    contrasenaHash = contrasenaHash,
                    rol = rol
                )

                usuarioDao.insertUsuario(nuevoUsuario)

                try {
                    val authResult = firebaseAuth.createUserWithEmailAndPassword(correo, contrasenaPlana).await()
                    val firebaseUser = authResult.user

                    if (firebaseUser != null) {
                        nuevoUsuario = nuevoUsuario.copy(firebaseUid = firebaseUser.uid)
                        usuarioDao.updateUsuario(nuevoUsuario)

                        val dto = UsuarioDTO(
                            uid = firebaseUser.uid,
                            nombreUsuario = nombre,
                            correo = correo,
                            rol = rol
                        )
                        firestore.collection("usuarios").document(firebaseUser.uid).set(dto).await()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("UserRepository", "Sincronización diferida o fallida: ${e.message}")
                }

                Result.success(nuevoUsuario)
            } catch (e: Exception) {
                Result.failure(Exception("Error al registrar en base de datos local: ${e.localizedMessage}"))
            }
        }
    }
}