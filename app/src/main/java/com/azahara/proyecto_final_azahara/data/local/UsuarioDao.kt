package com.azahara.proyecto_final_azahara.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUsuario(usuario: Usuario): String

    @Update
    suspend fun updateUsuario(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun getUsuarioPorId(id: String): Flow<Usuario?>

    @Query("DELETE FROM usuarios WHERE id = :id")
    suspend fun deleteUsuarioPorId(id: String)
}