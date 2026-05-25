package com.azahara.proyecto_final_azahara.repository

import com.azahara.proyecto_final_azahara.data.local.CitaMedicaDao
import com.azahara.proyecto_final_azahara.data.remote.CitaMedicaDTO
import com.azahara.proyecto_final_azahara.model.CitaMedica
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AppointmentRepository(
    private val citaDao: CitaMedicaDao,
    private val firestore: FirebaseFirestore
) {
    suspend fun guardarCita(cita: CitaMedica, usuarioUid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Guardamos localmente en Room
            citaDao.insertCita(cita)

            // 2. Transformamos al DTO de la nube
            val dto = CitaMedicaDTO(
                idLocal = cita.id,
                motivo = cita.motivo,
                medico = cita.medico,
                especialidad = cita.especialidad,
                fechaHora = cita.fechaHora,
                notas = cita.notas,
                recordatorioPrevio = cita.recordatorioPrevio
            )

            // 3. Subimos a Firestore colgándolo del UID seguro del usuario
            firestore.collection("usuarios")
                .document(usuarioUid)
                .collection("citas")
                .document(cita.id)
                .set(dto)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            // Si no hay red, devolvemos éxito para tolerar el modo offline
            Result.success(Unit)
        }
    }

    suspend fun eliminarCita(cita: CitaMedica, usuarioUid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            citaDao.deleteCita(cita)

            firestore.collection("usuarios")
                .document(usuarioUid)
                .collection("citas")
                .document(cita.id)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }
}