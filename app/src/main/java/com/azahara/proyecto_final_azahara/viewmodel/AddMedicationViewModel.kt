package com.azahara.proyecto_final_azahara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azahara.proyecto_final_azahara.data.local.MedicamentoDao
import com.azahara.proyecto_final_azahara.data.network.MedicamentoBasicoDto
import com.azahara.proyecto_final_azahara.model.Medicamento
import com.azahara.proyecto_final_azahara.repository.CimaRepository
import com.azahara.proyecto_final_azahara.repository.MedicamentoDetalle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CimaUiState {
    object Idle : CimaUiState
    object Loading : CimaUiState
    data class SuccessList(val medicamentos: List<MedicamentoBasicoDto>) : CimaUiState
    data class SuccessDetail(val detalle: MedicamentoDetalle) : CimaUiState
    data class Error(val message: String) : CimaUiState
    object SaveSuccess : CimaUiState
}

class AddMedicationViewModel(
    private val repository: CimaRepository,
    private val medicamentoDao: MedicamentoDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<CimaUiState>(CimaUiState.Idle)
    val uiState: StateFlow<CimaUiState> = _uiState.asStateFlow()

    // Variable temporal para programar la alarma en el Fragment
    var ultimoMedicamentoGuardado: Medicamento? = null

    fun buscarMedicamento(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.value = CimaUiState.Loading
            try {
                val resultados = repository.buscarPorNombre(query)
                _uiState.value = CimaUiState.SuccessList(resultados)
            } catch (e: Exception) {
                // Ahora enviaremos a la pantalla el motivo EXACTO por el que ha fallado
                android.util.Log.e("CIMA_DEBUG", "Fallo real: ", e)
                _uiState.value = CimaUiState.Error("Fallo real: ${e.message}")
            }
        }
    }

    // Carga los detalles clínicos desde la API de CIMA
    fun cargarDetalle(medicamento: MedicamentoBasicoDto) {
        viewModelScope.launch {
            _uiState.value = CimaUiState.Loading
            try {
                val detalle = repository.obtenerDetalleCompleto(medicamento)
                _uiState.value = CimaUiState.SuccessDetail(detalle)
            } catch (e: Exception) {
                android.util.Log.e("CIMA_DEBUG", "Fallo real: ", e)
                _uiState.value = CimaUiState.Error("Fallo CIMA: ${e.message}")
            }
        }
    }

    // MODO CREAR: Guarda en Room y retiene el ID generado para la alarma
    fun guardarMedicamentoLocal(nombre: String, hora: String, mensaje: String, urlProspecto: String?, contraindicaciones: String?) {
        viewModelScope.launch {
            try {
                val nuevoMedicamento = Medicamento(
                    nombre = nombre,
                    horaToma = hora,
                    mensajePersonalizado = mensaje,
                    urlProspecto = urlProspecto,
                    contraindicaciones = contraindicaciones
                )
                // Room nos devuelve el ID único en formato Long al insertar
                val idGenerado = medicamentoDao.insertMedicamento(nuevoMedicamento)

                // Guardamos una copia exacta con su ID real para que el Fragment encienda la alarma
                ultimoMedicamentoGuardado = nuevoMedicamento.copy(id = idGenerado.toInt())

                _uiState.value = CimaUiState.SaveSuccess
            } catch (e: Exception) {
                _uiState.value = CimaUiState.Error("Error al guardar localmente")
            }
        }
    }

    // MODO EDITAR: Actualiza los datos de una pastilla existente
    fun actualizarMedicamentoLocal(id: Int, nombre: String, hora: String, mensaje: String, urlProspecto: String?, contraindicaciones: String?) {
        viewModelScope.launch {
            try {
                val medModificado = Medicamento(
                    id = id, // Al pasar el mismo ID, Room sabe que debe sobrescribir
                    nombre = nombre,
                    horaToma = hora,
                    mensajePersonalizado = mensaje,
                    urlProspecto = urlProspecto,
                    contraindicaciones = contraindicaciones
                )
                medicamentoDao.updateMedicamento(medModificado)

                // Guardamos el objeto modificado para actualizar su alarma en el sistema
                ultimoMedicamentoGuardado = medModificado

                _uiState.value = CimaUiState.SaveSuccess
            } catch (e: Exception) {
                _uiState.value = CimaUiState.Error("Error al actualizar medicamento")
            }
        }
    }
}