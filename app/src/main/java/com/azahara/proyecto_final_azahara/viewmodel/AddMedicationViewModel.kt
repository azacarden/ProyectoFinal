package com.azahara.proyecto_final_azahara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azahara.proyecto_final_azahara.model.Medicamento
import com.azahara.proyecto_final_azahara.data.local.MedicamentoDao
import com.azahara.proyecto_final_azahara.data.network.MedicamentoBasicoDto
import com.azahara.proyecto_final_azahara.model.HorarioMedicamento
import com.azahara.proyecto_final_azahara.model.MedicamentoConHorarios
import com.azahara.proyecto_final_azahara.repository.CimaRepository
import com.azahara.proyecto_final_azahara.repository.MedicamentoDetalle
import com.azahara.proyecto_final_azahara.repository.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

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
    private val medicationRepository: MedicationRepository,
    private val medicamentoDao: MedicamentoDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<CimaUiState>(CimaUiState.Idle)
    val uiState: StateFlow<CimaUiState> = _uiState.asStateFlow()

    var ultimoMedicamentoGuardado: MedicamentoConHorarios? = null

    fun buscarMedicamento(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = CimaUiState.Loading
            try {
                val resultados = repository.buscarPorNombre(query)
                val queryLower = query.lowercase()

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
                // Diferenciamos si es falta de internet o un error del servidor
                // El único problema es que se guarda con el nombre tal cual, no guarda la ficha del producto
                if (e is java.net.UnknownHostException || e is java.net.ConnectException) {
                    _uiState.value = CimaUiState.Error("Sin conexión a Internet. Escribe el nombre del medicamento a mano y guárdalo.")
                } else {
                    _uiState.value = CimaUiState.Error("Error del servidor CIMA: Vuelve a intentarlo más tarde.")
                }
            }
        }
    }

    suspend fun verificarContraindicaciones(nombreNuevo: String): String? {
        val misMedicamentos = medicamentoDao.obtenerTodosLosMedicamentosConHorariosSync()
        for (medConHorarios in misMedicamentos) {
            val contra = medConHorarios.medicamento.contraindicaciones ?: ""
            if (contra.contains(nombreNuevo, ignoreCase = true)) {
                return "Cuidado: ${medConHorarios.medicamento.nombre} tiene contraindicaciones con $nombreNuevo."
            }
        }
        return null
    }

    fun validarYGuardar(
        nombre: String, horasTexto: String, mensaje: String, frecuencia: String,
        diaEspecifico: String?, url: String?, contra: String?, id: String?,
        usuarioId: String, creadoPorNombre: String, pacienteNombre: String
    ) {
        viewModelScope.launch {
            try {
                val misMeds = medicamentoDao.obtenerTodosLosMedicamentosConHorariosSync()
                val existe = misMeds.any { it.medicamento.nombre.equals(nombre, ignoreCase = true) }

                if (id == null && existe) {
                    _uiState.value = CimaUiState.Error("Este medicamento ya está registrado.")
                    return@launch
                }

                val idFinal = id ?: UUID.randomUUID().toString()
                val nuevoMedicamento = Medicamento(
                    id = idFinal, nombre = nombre, mensajePersonalizado = mensaje,
                    frecuencia = frecuencia, diaEspecifico = diaEspecifico,
                    urlProspecto = url, contraindicaciones = contra,
                    creadoPorNombre = creadoPorNombre, pacienteNombre = pacienteNombre
                )

                val listaHorarios = horasTexto.split(",").mapNotNull { horaStr ->
                    val horaLimpia = horaStr.trim()
                    if (horaLimpia.isNotEmpty()) HorarioMedicamento(medicamentoId = idFinal, horaToma = horaLimpia) else null
                }

                val wrapper = MedicamentoConHorarios(nuevoMedicamento, listaHorarios)

                medicationRepository.guardarMedicamento(wrapper, usuarioId)

                ultimoMedicamentoGuardado = wrapper
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