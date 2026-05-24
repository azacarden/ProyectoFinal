package com.azahara.proyecto_final_azahara.repository

import com.azahara.proyecto_final_azahara.data.local.Medicamento
import com.azahara.proyecto_final_azahara.data.local.MedicamentoDao
import com.azahara.proyecto_final_azahara.data.remote.MedicamentoDTO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedicationRepository(
    private val medicamentoDao: MedicamentoDao,
    private val firestore: FirebaseFirestore
) {
    fun obtenerMedicamentosActivos(): Flow<List<Medicamento>> {
        return medicamentoDao.getAllMedicamentos()
    }

    suspend fun guardarMedicamento(medicamento: Medicamento, usuarioId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            medicamentoDao.insertMedicamento(medicamento)

            val dto = MedicamentoDTO(
                idLocal = medicamento.id,
                nombre = medicamento.nombre,
                mensajePersonalizado = medicamento.mensajePersonalizado,
                horarios = medicamento.horaToma,
                frecuencia = medicamento.frecuencia,
                urlProspecto = medicamento.urlProspecto,
                contraindicaciones = medicamento.contraindicaciones
            )

            firestore.collection("usuarios")
                .document(usuarioId)
                .collection("medicamentos")
                .document(medicamento.id)
                .set(dto)

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

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al eliminar el medicamento: ${e.localizedMessage}"))
        }
    }

    /**
     * OPERACIÓN DEL CUIDADOR: Listener en tiempo real usando el SDK de Firebase.
     * Descarga la nube, la traduce a modo local, y sobrescribe la base de datos (Room).
     */
    fun escucharMedicacionPaciente(pacienteUid: String): Flow<Boolean> = kotlinx.coroutines.flow.callbackFlow {
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

                    // Mapeo Inverso: DTO (Nube con String) -> Entidad (Room con String)
                    val medicamentosLocales = medicamentosNube.map { dto ->
                        Medicamento(
                            id = dto.idLocal,
                            nombre = dto.nombre,
                            horaToma = dto.horarios.joinToString(", "),
                            frecuencia = dto.frecuencia,
                            mensajePersonalizado = dto.mensajePersonalizado,
                            urlProspecto = dto.urlProspecto,
                            contraindicaciones = dto.contraindicaciones
                        )
                    }

                    medicamentoDao.reemplazarTodosLosMedicamentos(medicamentosLocales)
                    trySend(true)
                }
            }
        }

        kotlinx.coroutines.channels.awaitClose { listener.remove() }
    }
}