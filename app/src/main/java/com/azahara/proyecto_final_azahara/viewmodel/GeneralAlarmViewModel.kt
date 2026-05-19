package com.azahara.proyecto_final_azahara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azahara.proyecto_final_azahara.data.local.AlarmaGeneralDao
import com.azahara.proyecto_final_azahara.model.AlarmaGeneral
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GeneralAlarmViewModel(private val dao: AlarmaGeneralDao) : ViewModel() {

    val alarmasActivas: StateFlow<List<AlarmaGeneral>> = dao.obtenerTodasLasAlarmas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _guardadoExitoso = MutableStateFlow<Boolean?>(null)
    val guardadoExitoso: StateFlow<Boolean?> = _guardadoExitoso.asStateFlow()

    var ultimaAlarmaGuardada: AlarmaGeneral? = null

    fun guardarAlarma(titulo: String, fechaHoraMilis: Long, descripcion: String) {
        viewModelScope.launch {
            try {
                val nuevaAlarma = AlarmaGeneral(
                    titulo = titulo,
                    fechaHora = fechaHoraMilis,
                    descripcion = descripcion
                )
                val idGenerado = dao.insertAlarmaGeneral(nuevaAlarma)
                ultimaAlarmaGuardada = nuevaAlarma.copy(id = idGenerado.toInt())
                _guardadoExitoso.value = true
            } catch (e: Exception) {
                _guardadoExitoso.value = false
            }
        }
    }

    fun actualizarAlarma(id: Int, titulo: String, fechaHoraMilis: Long, descripcion: String, activa: Boolean) {
        viewModelScope.launch {
            try {
                val alarmaModificada = AlarmaGeneral(
                    id = id,
                    titulo = titulo,
                    fechaHora = fechaHoraMilis,
                    descripcion = descripcion,
                    activa = activa
                )
                dao.updateAlarmaGeneral(alarmaModificada)
                ultimaAlarmaGuardada = alarmaModificada
                _guardadoExitoso.value = true
            } catch (e: Exception) {
                _guardadoExitoso.value = false
            }
        }
    }

    fun borrarAlarma(alarma: AlarmaGeneral) {
        viewModelScope.launch {
            try {
                dao.eliminarAlarmaGeneral(alarma.id)
            } catch (e: Exception) {
                android.util.Log.e("GeneralAlarmViewModel", "Error al eliminar alarma local", e)
            }
        }
    }

    fun resetearEstado() {
        _guardadoExitoso.value = null
    }
}