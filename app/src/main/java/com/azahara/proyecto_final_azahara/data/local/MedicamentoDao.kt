package com.azahara.proyecto_final_azahara.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicamento(medicamento: Medicamento)

    @Query("SELECT * FROM medicamentos")
    fun getAllMedicamentos(): Flow<List<Medicamento>>

    @Query("SELECT * FROM medicamentos WHERE id = :id LIMIT 1")
    fun getMedicamentoPorId(id: String): Flow<Medicamento?>

    @Query("DELETE FROM medicamentos WHERE id = :id")
    suspend fun deleteMedicamentoPorId(id: String)

    @Transaction
    suspend fun reemplazarTodosLosMedicamentos(nuevos: List<Medicamento>) {
        vaciarTabla()
        nuevos.forEach { insertMedicamento(it) }
    }

    @Query("DELETE FROM medicamentos")
    suspend fun vaciarTabla()
}