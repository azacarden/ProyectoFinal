package com.azahara.proyecto_final_azahara.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.azahara.proyecto_final_azahara.R
import com.azahara.proyecto_final_azahara.data.local.AppDatabase
import com.azahara.proyecto_final_azahara.viewmodel.GeneralAlarmViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddGeneralAlarmFragment : Fragment(R.layout.fragment_add_general_alarm) {

    private lateinit var viewModel: GeneralAlarmViewModel
    private val calendarioAlarma = Calendar.getInstance()
    private var fechaSeleccionada = false
    private var horaSeleccionada = false

    // Si viene un ID, estamos en Modo Edición
    private var idAlarmaEditar: Int = -1
    private var alarmaActivaOriginal: Boolean = true // Por defecto, si es nueva, está encendida

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dao = AppDatabase.getDatabase(requireContext()).alarmaGeneralDao()
        viewModel = GeneralAlarmViewModel(dao)

        idAlarmaEditar = arguments?.getInt("ALARMA_ID_EDITAR", -1) ?: -1

        val tvTituloPantalla = view.findViewById<TextView>(R.id.tvTituloAddGeneral)
        val etTitulo = view.findViewById<EditText>(R.id.etTituloAlarma)
        val etNotas = view.findViewById<EditText>(R.id.etNotasAlarma)
        val btnFecha = view.findViewById<Button>(R.id.btnElegirFechaAlarma)
        val btnHora = view.findViewById<Button>(R.id.btnElegirHoraAlarma)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarAlarmaGeneral)

        // MODO EDICIÓN
        if (idAlarmaEditar != -1) {
            tvTituloPantalla.text = "Modificar Alarma"
            btnGuardar.text = "Guardar Cambios"

            viewLifecycleOwner.lifecycleScope.launch {
                // Corregido: getAlarmaById con su tipografía correcta
                val alarma: com.azahara.proyecto_final_azahara.model.AlarmaGeneral? = withContext(Dispatchers.IO) {
                    dao.getAlarmaById(idAlarmaEditar)
                }
                alarma?.let {
                    etTitulo.setText(it.titulo)
                    etNotas.setText(it.descripcion)
                    alarmaActivaOriginal = it.activa

                    calendarioAlarma.timeInMillis = it.fechaHora
                    fechaSeleccionada = true
                    horaSeleccionada = true

                    val sdfFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val sdfHora = SimpleDateFormat("HH:mm", Locale.getDefault())
                    btnFecha.text = sdfFecha.format(Date(it.fechaHora))
                    btnHora.text = sdfHora.format(Date(it.fechaHora))
                }
            }
        }

        // Calendario
        btnFecha.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Día de la alarma")
                .setSelection(calendarioAlarma.timeInMillis) // Corregido: calendarioAlarma
                .build()

            datePicker.addOnPositiveButtonClickListener { milisegundos: Long ->
                calendarioAlarma.timeInMillis = milisegundos
                fechaSeleccionada = true
                val formatoVisual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                btnFecha.text = formatoVisual.format(calendarioAlarma.time)
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        // Reloj
        btnHora.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(calendarioAlarma.get(Calendar.HOUR_OF_DAY))
                .setMinute(calendarioAlarma.get(Calendar.MINUTE))
                .setTitleText("Hora de la alarma")
                .build()

            timePicker.addOnPositiveButtonClickListener {
                calendarioAlarma.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                calendarioAlarma.set(Calendar.MINUTE, timePicker.minute)
                calendarioAlarma.set(Calendar.SECOND, 0)
                horaSeleccionada = true
                btnHora.text = String.format(Locale.getDefault(), "%02d:%02d", timePicker.hour, timePicker.minute)
            }
            timePicker.show(parentFragmentManager, "TIME_PICKER")
        }

        // Guardado
        btnGuardar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val notas = etNotas.text.toString().trim()

            if (titulo.isBlank() || !fechaSeleccionada || !horaSeleccionada) {
                Toast.makeText(requireContext(), "Por favor, añade un asunto, fecha y hora", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (idAlarmaEditar == -1) {
                viewModel.guardarAlarma(titulo, calendarioAlarma.timeInMillis, notas)
            } else {
                val alarmaAntigua = com.azahara.proyecto_final_azahara.model.AlarmaGeneral(id = idAlarmaEditar, titulo = "", fechaHora = 0L, descripcion = "")
                com.azahara.proyecto_final_azahara.alarm.AlarmHelper(requireContext()).cancelarAlarmaGeneral(alarmaAntigua)

                viewModel.actualizarAlarma(idAlarmaEditar, titulo, calendarioAlarma.timeInMillis, notas, alarmaActivaOriginal)
            }
        }

        // Observar resultado de Room
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.guardadoExitoso.collect { exito: Boolean? ->
                    if (exito == true) {
                        Toast.makeText(requireContext(), "Alarma programada con éxito", Toast.LENGTH_SHORT).show()

                        viewModel.ultimaAlarmaGuardada?.let { alarma ->
                            com.azahara.proyecto_final_azahara.alarm.AlarmHelper(requireContext()).programarAlarmaGeneral(alarma)
                        }

                        viewModel.resetearEstado()
                        findNavController().navigateUp()
                    } else if (exito == false) {
                        Toast.makeText(requireContext(), "Error al guardar la alarma", Toast.LENGTH_SHORT).show()
                        viewModel.resetearEstado()
                    }
                }
            }
        }
    }
}