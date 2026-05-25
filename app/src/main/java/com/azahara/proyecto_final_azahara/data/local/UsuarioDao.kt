package com.azahara.proyecto_final_azahara.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
// ¡AQUÍ ESTÁ LA MAGIA! Le decimos dónde encontrar la clase Usuario
import com.azahara.proyecto_final_azahara.model.Usuario

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: Usuario)

    @Update
    suspend fun updateUsuario(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE nombreUsuario = :nombre LIMIT 1")
    suspend fun getUsuarioPorNombreSync(nombre: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    fun getUsuarioPorId(id: String): Flow<Usuario?>

    @Query("DELETE FROM usuarios WHERE id = :id")
    suspend fun deleteUsuarioPorId(id: String)
}