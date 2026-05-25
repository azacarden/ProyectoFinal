package com.azahara.proyecto_final_azahara.repository

import com.azahara.proyecto_final_azahara.data.local.CitaMedicaDao
import com.azahara.proyecto_final_azahara.data.remote.CitaMedicaDTO
import com.azahara.proyecto_final_azahara.model.CitaMedica
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AppointmentRepository(
    private val citaDao: CitaMedicaDao,
    private val firestore: FirebaseFirestore
) {
    suspend fun guardarCita(cita: CitaMedica, usuarioUid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            citaDao.insertCita(cita)

            val dto = CitaMedicaDTO(
                idLocal = cita.id,
                motivo = cita.motivo,
                medico = cita.medico,
                especialidad = cita.especialidad,
                fechaHora = cita.fechaHora,
                notas = cita.notas,
                recordatorioPrevio = cita.recordatorioPrevio,
                creadoPorNombre = cita.creadoPorNombre // <--- NUEVO
            )

            firestore.collection("usuarios")
                .document(usuarioUid)
                .collection("citas")
                .document(cita.id)
                .set(dto)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    suspend fun eliminarCita(cita: CitaMedica, usuarioUid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            citaDao.deleteCita(cita)
            firestore.collection("usuarios").document(usuarioUid).collection("citas").document(cita.id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    fun escucharCitasPaciente(pacienteUid: String): Flow<Boolean> = callbackFlow {
        val coleccionRef = firestore.collection("usuarios").document(pacienteUid).collection("citas")

        val listener = coleccionRef.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }

            if (snapshot != null) {
                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                    val citasNube = snapshot.documents.mapNotNull { it.toObject(CitaMedicaDTO::class.java) }
                    val nuevasCitasLocal = citasNube.map { dto ->
                        CitaMedica(
                            id = dto.idLocal,
                            motivo = dto.motivo,
                            medico = dto.medico,
                            especialidad = dto.especialidad,
                            fechaHora = dto.fechaHora,
                            notas = dto.notas,
                            recordatorioPrevio = dto.recordatorioPrevio,
                            creadoPorNombre = dto.creadoPorNombre
                        )
                    }
                    citaDao.reemplazarTodasLasCitas(nuevasCitasLocal)
                    trySend(true)
                }
            }
        }
        awaitClose { listener.remove() }
    }
}