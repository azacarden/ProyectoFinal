package com.azahara.proyecto_final_azahara.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.azahara.proyecto_final_azahara.model.Usuario

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUsuario(usuario: Usuario): Long

    @Update
    suspend fun updateUsuario(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE nombreUsuario = :nombre LIMIT 1")
    suspend fun obtenerUsuarioPorNombre(nombre: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE firebaseUid = :uid LIMIT 1")
    suspend fun obtenerUsuarioPorUid(uid: String): Usuario?
}