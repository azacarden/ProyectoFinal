package com.azahara.proyecto_final_azahara.repository

import com.azahara.proyecto_final_azahara.data.local.MedicamentoDao
import com.azahara.proyecto_final_azahara.data.remote.MedicamentoDTO
import com.azahara.proyecto_final_azahara.model.Medicamento
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedicationRepository(
    private val medicamentoDao: MedicamentoDao,
    private val firestore: FirebaseFirestore
) {

    /**
     * OPERACIÓN DEL PACIENTE: Guarda localmente y sincroniza con Firestore en segundo plano.
     */
    suspend fun agregarMedicamento(medicamento: Medicamento, pacienteUid: String) {
        withContext(Dispatchers.IO) {
            val idLocal = medicamentoDao.insertMedicamento(medicamento)
            val listaHorarios = medicamento.horaToma.split(",").map { it.trim() }

            val dto = MedicamentoDTO(
                idLocal = idLocal.toInt(),
                nombre = medicamento.nombre,
                horarios = listaHorarios,
                frecuencia = medicamento.frecuencia, // ¡Añadido!
                mensajePersonalizado = medicamento.mensajePersonalizado
            )

            firestore.collection("usuarios").document(pacienteUid)
                .collection("medicamentos").document(idLocal.toString())
                .set(dto)
        }
    }

    /**
     * OPERACIÓN DEL CUIDADOR: Listener en tiempo real usando el SDK de Firebase.
     * Descarga la nube, la traduce a modo local, y sobrescribe la base de datos (Room).
     */
    fun escucharMedicacionPaciente(pacienteUid: String): Flow<Boolean> = callbackFlow {
        val coleccionRef = firestore.collection("usuarios").document(pacienteUid).collection("medicamentos")

        val listener = coleccionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                // Como Room exige Corrutinas, lanzamos un hilo secundario dentro del Listener
                CoroutineScope(Dispatchers.IO).launch {
                    val medicamentosNube = snapshot.documents.mapNotNull {
                        it.toObject(MedicamentoDTO::class.java)
                    }

                    // 1. Mapeo Inverso: DTO (Nube) -> Entidad (Room)
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

                    // 2. Transacción Local: Hacemos que Room sea un espejo exacto de Firestore
                    medicamentoDao.reemplazarTodosLosMedicamentos(medicamentosLocales)

                    // 3. Emitimos un "true" para avisar a la UI de que hubo una actualización exitosa
                    trySend(true)
                }
            }
        }

        awaitClose { listener.remove() }
    }

    fun obtenerMedicamentosActivos(): Flow<List<Medicamento>> {
        return medicamentoDao.getAllMedicamentos()
    }
}