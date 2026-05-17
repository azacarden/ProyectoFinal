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

    fun buscarMedicamento(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.value = CimaUiState.Loading
            try {
                val resultados = repository.buscarPorNombre(query)
                _uiState.value = CimaUiState.SuccessList(resultados)
            } catch (e: Exception) {
                _uiState.value = CimaUiState.Error("Error al conectar con CIMA")
            }
        }
    }

    // Ahora recibe el objeto MedicamentoBasicoDto
    fun cargarDetalle(medicamento: MedicamentoBasicoDto) {
        viewModelScope.launch {
            _uiState.value = CimaUiState.Loading
            try {
                val detalle = repository.obtenerDetalleCompleto(medicamento)
                _uiState.value = CimaUiState.SuccessDetail(detalle)
            } catch (e: Exception) {
                _uiState.value = CimaUiState.Error("Fallo CIMA: ${e.message}")
            }
        }
    }

    // Guardado normal (Modo Crear)
    fun guardarMedicamentoLocal(nombre: String, hora: String, mensaje: String) {
        viewModelScope.launch {
            try {
                val nuevoMedicamento = Medicamento(
                    nombre = nombre,
                    horaToma = hora,
                    mensajePersonalizado = mensaje
                )
                medicamentoDao.insertMedicamento(nuevoMedicamento)
                _uiState.value = CimaUiState.SaveSuccess
            } catch (e: Exception) {
                _uiState.value = CimaUiState.Error("Error al guardar localmente")
            }
        }
    }

    // Esta función es necesaria para que el botón "Guardar Cambios" no dé error
// Actualiza tu función de Guardar para recibir los nuevos datos
    fun guardarMedicamentoLocal(nombre: String, hora: String, mensaje: String, urlProspecto: String?, contraindicaciones: String?) {
        viewModelScope.launch {
            try {
                val nuevoMedicamento = com.azahara.proyecto_final_azahara.model.Medicamento(
                    nombre = nombre,
                    horaToma = hora,
                    mensajePersonalizado = mensaje,
                    urlProspecto = urlProspecto,
                    contraindicaciones = contraindicaciones
                )
                medicamentoDao.insertMedicamento(nuevoMedicamento)
                _uiState.value = CimaUiState.SaveSuccess
            } catch (e: Exception) {
                _uiState.value = CimaUiState.Error("Error al guardar localmente")
            }
        }
    }

    // Actualiza tu función de Editar para mantener o cambiar los nuevos datos
    fun actualizarMedicamentoLocal(id: Int, nombre: String, hora: String, mensaje: String, urlProspecto: String?, contraindicaciones: String?) {
        viewModelScope.launch {
            try {
                val medModificado = com.azahara.proyecto_final_azahara.model.Medicamento(
                    id = id,
                    nombre = nombre,
                    horaToma = hora,
                    mensajePersonalizado = mensaje,
                    urlProspecto = urlProspecto,
                    contraindicaciones = contraindicaciones
                )
                medicamentoDao.updateMedicamento(medModificado)
                _uiState.value = CimaUiState.SaveSuccess
            } catch (e: Exception) {
                _uiState.value = CimaUiState.Error("Error al actualizar medicamento")
            }
        }
    }
}