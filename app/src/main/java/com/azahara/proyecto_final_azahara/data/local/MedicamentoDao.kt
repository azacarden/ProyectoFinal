package com.azahara.proyecto_final_azahara.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.azahara.proyecto_final_azahara.model.Medicamento
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicamento(medicamento: Medicamento): Long

    @Update
    suspend fun updateMedicamento(medicamento: Medicamento)

    @Delete
    suspend fun deleteMedicamento(medicamento: Medicamento)

    // Nota: Flow ya es asíncrono por naturaleza, por lo que NO necesita la palabra "suspend"
    @Query("SELECT * FROM medicamentos")
    fun getAllMedicamentos(): Flow<List<Medicamento>>

    @Query("SELECT * FROM medicamentos WHERE id = :id")
    suspend fun getMedicamentoPorId(id: Int): Medicamento?
}