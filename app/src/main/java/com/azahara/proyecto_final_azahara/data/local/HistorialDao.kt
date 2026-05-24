package com.azahara.proyecto_final_azahara.data.local

import androidx.room.*
import com.azahara.proyecto_final_azahara.model.Historial
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistorial(historial: Historial)

    @Query("SELECT * FROM historial_tomas WHERE usuarioId = :usuarioId ORDER BY fechaHora DESC")
    fun getHistorialPorUsuario(usuarioId: String): Flow<List<Historial>>
}