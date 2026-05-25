package com.azahara.proyecto_final_azahara.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.azahara.proyecto_final_azahara.model.AlarmaGeneral

@Dao
interface AlarmaGeneralDao {

    @Insert
    suspend fun insertAlarmaGeneral(alarma: AlarmaGeneral): Long

    @Query("SELECT * FROM alarmas_generales")
    fun obtenerTodasLasAlarmas(): kotlinx.coroutines.flow.Flow<List<AlarmaGeneral>>

    @Query("SELECT * FROM alarmas_generales WHERE id = :id")
    suspend fun getAlarmaById(id: Int): AlarmaGeneral?

    @Update
    suspend fun updateAlarmaGeneral(alarma: AlarmaGeneral)

    @Query("DELETE FROM alarmas_generales WHERE id = :id")
    suspend fun eliminarAlarmaGeneral(id: Int)
}