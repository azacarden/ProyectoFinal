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

    fun cargarDetalle(nregistro: String, nombre: String) {
        viewModelScope.launch {
            _uiState.value = CimaUiState.Loading
            try {
                val detalle = repository.obtenerDetalleCompleto(nregistro, nombre)
                _uiState.value = CimaUiState.SuccessDetail(detalle)
            } catch (e: Exception) {
                // ¡AQUÍ ESTÁ EL CAMBIO! Ahora nos mostrará el motivo real del fallo
                _uiState.value = CimaUiState.Error("Fallo CIMA: ${e.message}")
            }
        }
    }

    fun guardarMedicamentoLocal(nombre: String, hora: String, mensaje: String) {
        viewModelScope.launch {
            try {
                // Usamos la clase Medicamento exacta de tu model/Medicamento.kt
                val nuevoMedicamento = Medicamento(
                    nombre = nombre,
                    horaToma = hora,
                    mensajePersonalizado = mensaje
                )

                medicamentoDao.insertMedicamento(nuevoMedicamento)
                _uiState.value = CimaUiState.SaveSuccess
            } catch (e: Exception) {
                _uiState.value = CimaUiState.Error("Error al guardar en base de datos")
            }
        }
    }
}