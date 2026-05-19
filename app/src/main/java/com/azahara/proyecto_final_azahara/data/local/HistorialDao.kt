package com.azahara.proyecto_final_azahara.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.azahara.proyecto_final_azahara.model.Historial
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistorial(historial: Historial): Long

    @Query("SELECT * FROM historial_tomas WHERE usuarioId = :usuarioId ORDER BY fechaHoraReal DESC")
    fun getHistorialPorUsuario(usuarioId: Int): Flow<List<Historial>>
}