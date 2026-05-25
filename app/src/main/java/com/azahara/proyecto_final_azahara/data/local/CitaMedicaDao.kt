package com.azahara.proyecto_final_azahara.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.azahara.proyecto_final_azahara.model.CitaMedica
import kotlinx.coroutines.flow.Flow

@Dao
interface CitaMedicaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCita(cita: CitaMedica): Long

    @Update
    suspend fun updateCita(cita: CitaMedica)

    @Delete
    suspend fun deleteCita(cita: CitaMedica)

    // Obtenemos todas las citas ordenadas por fecha (las más próximas primero)
    @Query("SELECT * FROM citas_medicas ORDER BY fechaHora ASC")
    fun getAllCitas(): Flow<List<CitaMedica>>

    @Query("SELECT * FROM citas_medicas WHERE fechaHora >= (strftime('%s', 'now') * 1000) ORDER BY fechaHora ASC")
    fun getActiveCitas(): Flow<List<CitaMedica>>

    @Query("SELECT * FROM citas_medicas WHERE fechaHora < (strftime('%s', 'now') * 1000) ORDER BY fechaHora DESC")
    fun getPastCitas(): Flow<List<CitaMedica>>

    @Query("SELECT * FROM citas_medicas WHERE id = :id")
    suspend fun getCitaById(id: String): CitaMedica? // <- CORREGIDO: Ahora es String
}