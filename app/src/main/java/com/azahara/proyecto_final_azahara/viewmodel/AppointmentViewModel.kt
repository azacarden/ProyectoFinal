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

    val citasActivas: StateFlow<List<CitaMedica>> = citaDao.getActiveCitas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val citasPasadas: StateFlow<List<CitaMedica>> = citaDao.getPastCitas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var ultimaCitaGuardada: CitaMedica? = null

    fun validarCita(motivo: String, medico: String, especialidad: String, fechaHoraMilis: Long): String? {
        if (motivo.isBlank()) return "El motivo de la cita es obligatorio."
        if (medico.isBlank()) return "El nombre del médico es obligatorio."
        if (especialidad.isBlank()) return "La especialidad es obligatoria."
        if (fechaHoraMilis < System.currentTimeMillis()) return "No puedes programar una cita en el pasado."
        return null
    }

    fun guardarCita(motivo: String, medico: String, especialidad: String, fechaHoraMilis: Long, notas: String) {
        viewModelScope.launch {
            try {
                val nuevaCita = CitaMedica(
                    motivo = motivo,
                    medico = medico,
                    especialidad = especialidad,
                    fechaHora = fechaHoraMilis,
                    notas = notas,
                    recordatorioPrevio = 60
                )
                // CORREGIDO: Insertamos directamente. El UUID de la cita ya está generado.
                citaDao.insertCita(nuevaCita)

                // Asignamos la cita intacta
                ultimaCitaGuardada = nuevaCita

                _guardadoExitoso.value = true
            } catch (e: Exception) {
                _guardadoExitoso.value = false
            }
        }
    }

    fun actualizarCita(id: String, motivo: String, medico: String, especialidad: String, fechaHoraMilis: Long, notas: String) {
        viewModelScope.launch {
            try {
                val citaModificada = CitaMedica(
                    id = id,
                    motivo = motivo,
                    medico = medico,
                    especialidad = especialidad,
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

    fun borrarCita(cita: CitaMedica) {
        viewModelScope.launch { citaDao.deleteCita(cita) }
    }

    fun resetearEstado() { _guardadoExitoso.value = null }
}