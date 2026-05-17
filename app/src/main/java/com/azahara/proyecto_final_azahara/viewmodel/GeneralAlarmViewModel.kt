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

    // Leemos la lista completa de alarmas de forma reactiva
    val alarmasActivas: StateFlow<List<AlarmaGeneral>> = dao.getAllAlarmas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _guardadoExitoso = MutableStateFlow<Boolean?>(null)
    val guardadoExitoso: StateFlow<Boolean?> = _guardadoExitoso.asStateFlow()

    // Variable temporal para programar la alarma en el sistema operativo después de guardarla en Room
    var ultimaAlarmaGuardada: AlarmaGeneral? = null

    // MODO CREAR
    fun guardarAlarma(titulo: String, fechaHoraMilis: Long, descripcion: String) {
        viewModelScope.launch {
            try {
                val nuevaAlarma = AlarmaGeneral(
                    titulo = titulo,
                    fechaHora = fechaHoraMilis,
                    descripcion = descripcion
                )
                val idGenerado = dao.insertAlarma(nuevaAlarma)
                ultimaAlarmaGuardada = nuevaAlarma.copy(id = idGenerado.toInt())
                _guardadoExitoso.value = true
            } catch (e: Exception) {
                _guardadoExitoso.value = false
            }
        }
    }

    // MODO EDITAR
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
                dao.updateAlarma(alarmaModificada)
                ultimaAlarmaGuardada = alarmaModificada
                _guardadoExitoso.value = true
            } catch (e: Exception) {
                _guardadoExitoso.value = false
            }
        }
    }

    // MODO BORRAR
    fun borrarAlarma(alarma: AlarmaGeneral) {
        viewModelScope.launch {
            dao.deleteAlarma(alarma)
        }
    }

    fun resetearEstado() {
        _guardadoExitoso.value = null
    }
}