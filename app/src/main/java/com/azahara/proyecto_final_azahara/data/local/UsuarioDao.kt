package com.azahara.proyecto_final_azahara.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.azahara.proyecto_final_azahara.model.Usuario

@Dao
interface UsuarioDao {

    // Operación para registrar un nuevo usuario
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: Usuario): Long

    // Operación para el Login: Busca un usuario que coincida con nombre y contraseña
    @Query("SELECT * FROM usuarios WHERE nombreUsuario = :nombre AND contrasena = :pass")
    suspend fun login(nombre: String, pass: String): Usuario?
}