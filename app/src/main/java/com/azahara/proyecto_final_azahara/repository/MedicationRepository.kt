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
        return medicamentoDao.getAllMedicamentosConHorariosActivos()
    }

    suspend fun guardarMedicamento(
        medicamentoConHorarios: MedicamentoConHorarios,
        usuarioId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Guardamos en local forzando la bandera de PENDIENTE
            val medOffline = medicamentoConHorarios.medicamento.copy(
                pendienteSincronizacion = true,
                marcadoParaEliminar = false
            )
            medicamentoDao.insertMedicamentoConHorarios(medOffline, medicamentoConHorarios.horarios)

            // 2. Intentamos subir a la nube
            val dto = MedicamentoDTO(
                idLocal = medOffline.id,
                nombre = medOffline.nombre,
                mensajePersonalizado = medOffline.mensajePersonalizado,
                horarios = medicamentoConHorarios.horarios.map { it.horaToma },
                frecuencia = medOffline.frecuencia,
                diaEspecifico = medOffline.diaEspecifico,
                urlProspecto = medOffline.urlProspecto,
                contraindicaciones = medOffline.contraindicaciones
            )

            firestore.collection("usuarios")
                .document(usuarioId)
                .collection("medicamentos")
                .document(medOffline.id)
                .set(dto)
                .await()

            // 3. Si hay internet y triunfa, le quitamos la etiqueta de pendiente
            medicamentoDao.updateEstadoSincronizacion(medOffline.id, false)
            Result.success(Unit)
        } catch (e: Exception) {
            // FALLO SILENCIOSO: Se queda en Room. El usuario no sufre interrupciones.
            Result.success(Unit)
        }
    }

    suspend fun eliminarMedicamento(medicamentoId: String, usuarioId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Borrado Lógico en local (Desaparece de la vista al instante)
            medicamentoDao.softDeleteMedicamento(medicamentoId)

            // 2. Intentamos borrar en Firestore
            firestore.collection("usuarios")
                .document(usuarioId)
                .collection("medicamentos")
                .document(medicamentoId)
                .delete()
                .await()

            // 3. Si triunfa en la nube, destruimos el registro local para siempre
            medicamentoDao.deleteMedicamentoPorId(medicamentoId)
            Result.success(Unit)
        } catch (e: Exception) {
            // FALLO SILENCIOSO: Sigue oculto localmente y marcado para eliminarse luego
            Result.success(Unit)
        }
    }

    // Nuevo motor de auto-reparación
    suspend fun forzarSincronizacionPendientes(usuarioId: String) = withContext(Dispatchers.IO) {
        val pendientes = medicamentoDao.obtenerPendientesDeSincronizar()
        for (wrapper in pendientes) {
            if (wrapper.medicamento.marcadoParaEliminar) {
                eliminarMedicamento(wrapper.medicamento.id, usuarioId)
            } else if (wrapper.medicamento.pendienteSincronizacion) {
                guardarMedicamento(wrapper, usuarioId)
            }
        }
    }

    fun escucharMedicacionPaciente(pacienteUid: String): Flow<Boolean> = callbackFlow {
        val coleccionRef = firestore.collection("usuarios").document(pacienteUid).collection("medicamentos")

        val listener = coleccionRef.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }

            if (snapshot != null) {
                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                    val medicamentosNube = snapshot.documents.mapNotNull { it.toObject(MedicamentoDTO::class.java) }
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
                            contraindicaciones = dto.contraindicaciones,
                            // Lo que viene de la nube está garantizado que no está pendiente
                            pendienteSincronizacion = false,
                            marcadoParaEliminar = false
                        )
                        nuevosMedicamentos.add(med)

                        val horarios = dto.horarios.map {
                            HorarioMedicamento(medicamentoId = dto.idLocal, horaToma = it)
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