package com.azahara.proyecto_final_azahara.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.azahara.proyecto_final_azahara.model.AlarmaGeneral
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmaGeneralDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarma(alarma: AlarmaGeneral): Long

    @Update
    suspend fun updateAlarma(alarma: AlarmaGeneral)

    @Delete
    suspend fun deleteAlarma(alarma: AlarmaGeneral)

    // Obtenemos todas las alarmas ordenadas cronológicamente
    @Query("SELECT * FROM alarmas_generales ORDER BY fechaHora ASC")
    fun getAllAlarmas(): Flow<List<AlarmaGeneral>>

    @Query("SELECT * FROM alarmas_generales WHERE id = :id")
    suspend fun getAlarmaById(id: Int): AlarmaGeneral?
}