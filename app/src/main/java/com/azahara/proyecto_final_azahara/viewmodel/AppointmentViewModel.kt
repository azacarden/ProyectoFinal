package com.azahara.proyecto_final_azahara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azahara.proyecto_final_azahara.data.local.CitaMedicaDao
import com.azahara.proyecto_final_azahara.model.CitaMedica
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppointmentViewModel(private val citaDao: CitaMedicaDao) : ViewModel() {

    private val _guardadoExitoso = MutableStateFlow<Boolean?>(null)
    val guardadoExitoso: StateFlow<Boolean?> = _guardadoExitoso.asStateFlow()

    val citasActivas: StateFlow<List<CitaMedica>> = citaDao.getAllCitas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Variable temporal para guardar el ID de la cita recién creada y pasársela a la alarma
    var ultimaCitaGuardada: CitaMedica? = null

    fun guardarCita(titulo: String, especialista: String, fechaHoraMilis: Long, notas: String) {
        viewModelScope.launch {
            try {
                val nuevaCita = CitaMedica(
                    titulo = titulo,
                    especialista = especialista,
                    fechaHora = fechaHoraMilis,
                    notas = notas,
                    recordatorioPrevio = 60 // Por defecto avisamos 60 minutos (1 hora) antes
                )

                // Guardamos en Room y obtenemos el ID generado
                val idGenerado = citaDao.insertCita(nuevaCita)

                // Guardamos el objeto completo para que el Fragment pueda programar la alarma
                ultimaCitaGuardada = nuevaCita.copy(id = idGenerado.toInt())

                _guardadoExitoso.value = true
            } catch (e: Exception) {
                _guardadoExitoso.value = false
            }
        }
    }
    fun borrarCita(cita: CitaMedica) {
        viewModelScope.launch {
            citaDao.deleteCita(cita)
        }
    }

    fun actualizarCita(id: Int, titulo: String, especialista: String, fechaHoraMilis: Long, notas: String) {
        viewModelScope.launch {
            try {
                val citaModificada = CitaMedica(
                    id = id, // Al pasarle el mismo ID, Room sabe que tiene que sobrescribir la antigua
                    titulo = titulo,
                    especialista = especialista,
                    fechaHora = fechaHoraMilis,
                    notas = notas,
                    recordatorioPrevio = 60
                )
                citaDao.updateCita(citaModificada)
                ultimaCitaGuardada = citaModificada
                _guardadoExitoso.value = true
            } catch (e: Exception) {
                _guardadoExitoso.value = false
            }
        }
    }

    fun resetearEstado() {
        _guardadoExitoso.value = null
    }
}