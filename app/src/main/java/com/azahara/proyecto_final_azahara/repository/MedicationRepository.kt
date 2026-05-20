package com.azahara.proyecto_final_azahara.repository

import com.azahara.proyecto_final_azahara.data.local.MedicamentoDao
import com.azahara.proyecto_final_azahara.data.remote.MedicamentoDTO
import com.azahara.proyecto_final_azahara.model.Medicamento
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext

/**
 * Repositorio de Medicación.
 * Coordinará las pastillas y alarmas entre la base de datos local y la nube
 */
class MedicationRepository(
    private val medicamentoDao: MedicamentoDao,
    private val firestore: FirebaseFirestore
) {

    /**
     * OPERACIÓN DEL PACIENTE: Guarda localmente y sincroniza con Firestore en segundo plano.
     */
    suspend fun agregarMedicamento(medicamento: Medicamento, pacienteUid: String) {
        // Opera en segundo plano sin bloquear la UI
        withContext(Dispatchers.IO) {
            // Guardar en la fuente de verdad local (Room)
            val idLocal = medicamentoDao.insertMedicamento(medicamento)

            // Cortamos el texto por las comas y quitamos los espacios en blanco
            val listaHorarios = medicamento.horaToma.split(",").map { it.trim() }

            // Crear el DTO para transferirlo a la red
            val dto = MedicamentoDTO(
                idLocal = idLocal.toInt(),
                nombre = medicamento.nombre,
                horarios = listaHorarios,
                mensajePersonalizado = medicamento.mensajePersonalizado
            )

            // Volcar los datos a Firestore
            // Estructura documental: usuarios -> [uid] -> medicamentos -> [idLocal]
            firestore.collection("usuarios").document(pacienteUid)
                .collection("medicamentos").document(idLocal.toString())
                .set(dto)
        }
    }

    /**
     * OPERACIÓN DEL CUIDADOR: Listener en tiempo real usando el SDK de Firebase.
     */

    fun escucharMedicacionPaciente(pacienteUid: String): Flow<List<MedicamentoDTO>> = callbackFlow {
        // Apunta a la carpeta (colección) de pastillas de ese paciente específico
        val coleccionRef = firestore.collection("usuarios").document(pacienteUid).collection("medicamentos")

        // Añade el Listener oficial de Firebase SDK
        val listener = coleccionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // Si hay error de red, cerramos el flujo
                return@addSnapshotListener
            }

            if (snapshot != null) {
                // Convierte el "JSON" de Firestore directamente a la clase DTO
                val medicamentosNube = snapshot.documents.mapNotNull {
                    it.toObject(MedicamentoDTO::class.java)
                }
                // Envia la lista actualizada al ViewModel del cuidador
                trySend(medicamentosNube)
            }
        }

        // Cuando el cuidador cierra la app, cancelamos el listener para no gastar datos/batería
        awaitClose { listener.remove() }
    }

    // Modificamos el tipo de retorno para que devuelva la lista de objetos de tu tabla 'Medicamento'
    fun obtenerMedicamentosActivos(): Flow<List<Medicamento>> {
        // Retorna el flujo reactivo directo de Room
        return medicamentoDao.getAllMedicamentos()
    }
}