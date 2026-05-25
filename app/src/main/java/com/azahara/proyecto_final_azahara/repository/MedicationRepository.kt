package com.azahara.proyecto_final_azahara.repository

import com.azahara.proyecto_final_azahara.model.Medicamento
import com.azahara.proyecto_final_azahara.data.local.MedicamentoDao
import com.azahara.proyecto_final_azahara.data.remote.MedicamentoDTO
import com.azahara.proyecto_final_azahara.model.HorarioMedicamento
import com.azahara.proyecto_final_azahara.model.MedicamentoConHorarios
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MedicationRepository(
    private val medicamentoDao: MedicamentoDao,
    private val firestore: FirebaseFirestore
) {
    fun obtenerMedicamentosActivos(): Flow<List<MedicamentoConHorarios>> {
        return medicamentoDao.getAllMedicamentosConHorarios()
    }

    suspend fun guardarMedicamento(
        medicamentoConHorarios: MedicamentoConHorarios,
        usuarioId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            medicamentoDao.insertMedicamentoConHorarios(
                medicamentoConHorarios.medicamento,
                medicamentoConHorarios.horarios
            )

            val dto = MedicamentoDTO(
                idLocal = medicamentoConHorarios.medicamento.id,
                nombre = medicamentoConHorarios.medicamento.nombre,
                mensajePersonalizado = medicamentoConHorarios.medicamento.mensajePersonalizado,
                horarios = medicamentoConHorarios.horarios.map { it.horaToma },
                frecuencia = medicamentoConHorarios.medicamento.frecuencia,
                diaEspecifico = medicamentoConHorarios.medicamento.diaEspecifico,
                urlProspecto = medicamentoConHorarios.medicamento.urlProspecto,
                contraindicaciones = medicamentoConHorarios.medicamento.contraindicaciones
            )

            firestore.collection("usuarios")
                .document(usuarioId)
                .collection("medicamentos")
                .document(medicamentoConHorarios.medicamento.id)
                .set(dto)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al guardar el medicamento: ${e.localizedMessage}"))
        }
    }

    suspend fun eliminarMedicamento(medicamentoId: String, usuarioId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            medicamentoDao.deleteMedicamentoPorId(medicamentoId)

            firestore.collection("usuarios")
                .document(usuarioId)
                .collection("medicamentos")
                .document(medicamentoId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al eliminar el medicamento: ${e.localizedMessage}"))
        }
    }

    fun escucharMedicacionPaciente(pacienteUid: String): Flow<Boolean> = callbackFlow {
        val coleccionRef = firestore.collection("usuarios").document(pacienteUid).collection("medicamentos")

        val listener = coleccionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                    val medicamentosNube = snapshot.documents.mapNotNull {
                        it.toObject(MedicamentoDTO::class.java)
                    }

                    val nuevosMedicamentos = mutableListOf<Medicamento>()
                    val nuevosHorarios = mutableListOf<HorarioMedicamento>()

                    for (dto in medicamentosNube) {
                        val med = Medicamento(
                            id = dto.idLocal,
                            nombre = dto.nombre,
                            mensajePersonalizado = dto.mensajePersonalizado,
                            frecuencia = dto.frecuencia,
                            diaEspecifico = dto.diaEspecifico,
                            urlProspecto = dto.urlProspecto,
                            contraindicaciones = dto.contraindicaciones
                        )
                        nuevosMedicamentos.add(med)

                        val horarios = dto.horarios.map { horaString ->
                            HorarioMedicamento(
                                medicamentoId = dto.idLocal,
                                horaToma = horaString
                            )
                        }
                        nuevosHorarios.addAll(horarios)
                    }

                    medicamentoDao.reemplazarTodosLosMedicamentos(nuevosMedicamentos, nuevosHorarios)
                    trySend(true)
                }
            }
        }

        awaitClose { listener.remove() }
    }
}