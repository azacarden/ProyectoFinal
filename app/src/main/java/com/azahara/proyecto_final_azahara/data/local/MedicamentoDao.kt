package com.azahara.proyecto_final_azahara.data.local

import androidx.room.*
import com.azahara.proyecto_final_azahara.model.HorarioMedicamento
import com.azahara.proyecto_final_azahara.model.MedicamentoConHorarios
import com.azahara.proyecto_final_azahara.model.Medicamento
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicamento(medicamento: Medicamento)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHorarios(horarios: List<HorarioMedicamento>)

    @Transaction
    suspend fun insertMedicamentoConHorarios(medicamento: Medicamento, horarios: List<HorarioMedicamento>) {
        insertMedicamento(medicamento)
        insertHorarios(horarios)
    }

    @Transaction
    @Query("SELECT * FROM medicamentos WHERE marcadoParaEliminar = 0")
    fun getAllMedicamentosConHorariosActivos(): Flow<List<MedicamentoConHorarios>>

    @Transaction
    @Query("SELECT * FROM medicamentos WHERE id = :id LIMIT 1")
    fun getMedicamentoConHorariosPorId(id: String): Flow<MedicamentoConHorarios?>

    @Query("DELETE FROM medicamentos WHERE id = :id")
    suspend fun deleteMedicamentoPorId(id: String)

    @Query("UPDATE medicamentos SET pendienteSincronizacion = :estado WHERE id = :id")
    suspend fun updateEstadoSincronizacion(id: String, estado: Boolean)

    @Query("UPDATE medicamentos SET marcadoParaEliminar = 1 WHERE id = :id")
    suspend fun softDeleteMedicamento(id: String)

    @Transaction
    @Query("SELECT * FROM medicamentos WHERE pendienteSincronizacion = 1 OR marcadoParaEliminar = 1")
    suspend fun obtenerPendientesDeSincronizar(): List<MedicamentoConHorarios>

    // ¡CORREGIDO! Activamos la limpieza previa para eliminar lo que ya no existe en la nube
    @Transaction
    suspend fun reemplazarTodosLosMedicamentos(
        nuevosMedicamentos: List<Medicamento>,
        nuevosHorarios: List<HorarioMedicamento>
    ) {
        vaciarTabla() // <--- Al limpiar la tabla, las eliminaciones del paciente se reflejan al instante
        nuevosMedicamentos.forEach { insertMedicamento(it) }
        insertHorarios(nuevosHorarios)
    }

    @Query("DELETE FROM horarios_medicamento WHERE medicamentoId = :medId")
    suspend fun deleteHorariosPorMedicamento(medId: String)

    @Query("DELETE FROM medicamentos")
    suspend fun vaciarTabla()

    @Transaction
    @Query("SELECT * FROM medicamentos WHERE marcadoParaEliminar = 0")
    suspend fun obtenerTodosLosMedicamentosConHorariosSync(): List<MedicamentoConHorarios>
}