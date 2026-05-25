package com.azahara.proyecto_final_azahara.data.local

import androidx.room.*
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

    @Query("SELECT * FROM citas_medicas ORDER BY fechaHora ASC")
    fun getAllCitas(): Flow<List<CitaMedica>>

    @Query("SELECT * FROM citas_medicas WHERE fechaHora >= (strftime('%s', 'now') * 1000) ORDER BY fechaHora ASC")
    fun getActiveCitas(): Flow<List<CitaMedica>>

    @Query("SELECT * FROM citas_medicas WHERE fechaHora < (strftime('%s', 'now') * 1000) ORDER BY fechaHora DESC")
    fun getPastCitas(): Flow<List<CitaMedica>>

    @Query("SELECT * FROM citas_medicas WHERE id = :id")
    suspend fun getCitaById(id: String): CitaMedica?

    // NUEVOS MÉTODOS PARA LA SINCRONIZACIÓN DEL CUIDADOR
    @Query("DELETE FROM citas_medicas")
    suspend fun vaciarTabla()

    @Transaction
    suspend fun reemplazarTodasLasCitas(nuevasCitas: List<CitaMedica>) {
        vaciarTabla()
        nuevasCitas.forEach { insertCita(it) }
    }
}