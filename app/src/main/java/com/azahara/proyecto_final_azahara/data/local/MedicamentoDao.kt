package com.azahara.proyecto_final_azahara.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.azahara.proyecto_final_azahara.model.HorarioMedicamento
import com.azahara.proyecto_final_azahara.model.Medicamento
import com.azahara.proyecto_final_azahara.model.MedicamentoConHorarios
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicamento(medicamento: Medicamento): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHorarios(horarios: List<HorarioMedicamento>)

    @Transaction
    suspend fun guardarMedicamentoConHorarios(medicamento: Medicamento, horas: List<String>) {
        val idMed = insertMedicamento(medicamento).toInt()
        val listaHorarios = horas.map { horaTexto ->
            HorarioMedicamento(medicamentoId = idMed, horaToma = horaTexto)
        }
        insertHorarios(listaHorarios)
    }

    @Transaction
    @Query("SELECT * FROM medicamentos")
    fun obtenerMedicamentosActivos(): Flow<List<MedicamentoConHorarios>>

    @Query("SELECT * FROM medicamentos WHERE id = :id")
    suspend fun getMedicamentoById(id: Int): Medicamento?

    @Update
    suspend fun updateMedicamento(medicamento: Medicamento)

    @Delete
    suspend fun deleteMedicamento(medicamento: Medicamento)

    @Query("SELECT * FROM medicamentos")
    fun getAllMedicamentos(): Flow<List<Medicamento>>

    @androidx.room.Query("DELETE FROM medicamentos")
    suspend fun borrarTodosLosMedicamentos()

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertListaMedicamentos(medicamentos: List<Medicamento>)

    // Una transacción asegura que si falla el guardado, no se borran los datos anteriores
    @androidx.room.Transaction
    suspend fun reemplazarTodosLosMedicamentos(medicamentos: List<Medicamento>) {
        borrarTodosLosMedicamentos()
        insertListaMedicamentos(medicamentos)
    }

    @androidx.room.Query("SELECT * FROM medicamentos")
    suspend fun obtenerTodosLosMedicamentosSync(): List<Medicamento>
}