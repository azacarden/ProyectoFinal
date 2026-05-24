package com.azahara.proyecto_final_azahara.data.local

import androidx.room.*
import com.azahara.proyecto_final_azahara.model.HorarioMedicamento
import com.azahara.proyecto_final_azahara.model.MedicamentoConHorarios
import com.azahara.proyecto_final_azahara.model.Medicamento
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {

    // 1. Operaciones básicas de inserción
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicamento(medicamento: Medicamento)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHorarios(horarios: List<HorarioMedicamento>)

    // 2. Transacción atómica: Guarda el medicamento y sus horarios juntos
    @Transaction
    suspend fun insertMedicamentoConHorarios(medicamento: Medicamento, horarios: List<HorarioMedicamento>) {
        insertMedicamento(medicamento)
        insertHorarios(horarios)
    }

    // 3. Consultas de lectura combinadas
    @Transaction
    @Query("SELECT * FROM medicamentos")
    fun getAllMedicamentosConHorarios(): Flow<List<MedicamentoConHorarios>>

    @Transaction
    @Query("SELECT * FROM medicamentos WHERE id = :id LIMIT 1")
    fun getMedicamentoConHorariosPorId(id: String): Flow<MedicamentoConHorarios?>

    // 4. Operaciones de borrado
    @Query("DELETE FROM medicamentos WHERE id = :id")
    suspend fun deleteMedicamentoPorId(id: String)

    // 5. Operaciones masivas (útil para la sincronización desde la nube)
    @Transaction
    suspend fun reemplazarTodosLosMedicamentos(
        nuevosMedicamentos: List<Medicamento>,
        nuevosHorarios: List<HorarioMedicamento>
    ) {
        vaciarTabla()
        nuevosMedicamentos.forEach { insertMedicamento(it) }
        insertHorarios(nuevosHorarios)
    }

    @Query("DELETE FROM medicamentos")
    suspend fun vaciarTabla()

    // Síncrono para operaciones en segundo plano (Alarmas y Validaciones)
    @Transaction
    @Query("SELECT * FROM medicamentos")
    suspend fun obtenerTodosLosMedicamentosConHorariosSync(): List<MedicamentoConHorarios>
}