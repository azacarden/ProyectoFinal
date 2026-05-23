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

    var ultimoMedicamentoGuardado: Medicamento? = null

    fun buscarMedicamento(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = CimaUiState.Loading
            try {
                val resultados = repository.buscarPorNombre(query)
                val queryLower = query.lowercase()

                // BÚSQUEDA INTELIGENTE:
                // 1. Priorizamos los que empiezan exactamente por lo que escribe el usuario.
                // 2. Incluimos los que contienen la palabra como parte de un nombre compuesto.
                val resultadosFiltrados = resultados.filter {
                    val nombre = it.nombre.lowercase()
                    nombre.startsWith(queryLower) || nombre.contains(" $queryLower")
                }

                if (resultadosFiltrados.isEmpty()) {
                    _uiState.value = CimaUiState.Error("No se encontraron coincidencias para '$query'.")
                } else {
                    _uiState.value = CimaUiState.SuccessList(resultadosFiltrados)
                }
            } catch (e: Exception) {
                _uiState.value = CimaUiState.Error("Error de conexión: ${e.localizedMessage}")
            }
        }
    }

    suspend fun verificarContraindicaciones(nombreNuevo: String): String? {
        val misMedicamentos = medicamentoDao.obtenerTodosLosMedicamentosSync()
        for (med in misMedicamentos) {
            val contra = med.contraindicaciones ?: ""
            if (contra.contains(nombreNuevo, ignoreCase = true)) {
                return "Cuidado: ${med.nombre} tiene contraindicaciones con $nombreNuevo."
            }
        }
        return null
    }

    fun validarYGuardar(nombre: String, hora: String, mensaje: String, frecuencia: String, diaEspecifico: String?, url: String?, contra: String?, id: Int) {
        viewModelScope.launch {
            try {
                // Validación duplicados (solo modo creación)
                if (id == -1 && medicamentoDao.getMedicamentoByNombre(nombre) != null) {
                    _uiState.value = CimaUiState.Error("Este medicamento ya está registrado.")
                    return@launch
                }

                val nuevoMedicamento = Medicamento(
                    id = if (id == -1) 0 else id,
                    nombre = nombre,
                    horaToma = hora,
                    mensajePersonalizado = mensaje,
                    frecuencia = frecuencia,
                    diaEspecifico = diaEspecifico,
                    urlProspecto = url,
                    contraindicaciones = contra
                )

                if (id == -1) medicamentoDao.insertMedicamento(nuevoMedicamento)
                else medicamentoDao.updateMedicamento(nuevoMedicamento)

                ultimoMedicamentoGuardado = nuevoMedicamento
                _uiState.value = CimaUiState.SaveSuccess
            } catch (e: Exception) {
                _uiState.value = CimaUiState.Error("Error al guardar: ${e.message}")
            }
        }
    }

    fun cargarDetalle(medicamento: MedicamentoBasicoDto) {
        viewModelScope.launch {
            _uiState.value = CimaUiState.Loading
            try {
                val detalle = repository.obtenerDetalleCompleto(medicamento)
                _uiState.value = CimaUiState.SuccessDetail(detalle)
            } catch (e: Exception) {
                _uiState.value = CimaUiState.Error("Error al cargar detalles")
            }
        }
    }
}