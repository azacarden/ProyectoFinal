package com.azahara.proyecto_final_azahara.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

    // CORREGIDO: Usamos la factoría
    private val viewModel: GeneralAlarmViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val dao = AppDatabase.getDatabase(requireContext()).alarmaGeneralDao()
                @Suppress("UNCHECKED_CAST")
                return GeneralAlarmViewModel(dao) as T
            }
        }
    }

    private val calendarioAlarma = Calendar.getInstance()
    private var fechaSeleccionada = false
    private var horaSeleccionada = false
    private var idAlarmaEditar: Int = -1
    private var alarmaActivaOriginal: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        idAlarmaEditar = arguments?.getInt("ALARMA_ID_EDITAR", -1) ?: -1

        val tvTituloPantalla = view.findViewById<TextView>(R.id.tvTituloAddGeneral)
        val etTitulo = view.findViewById<EditText>(R.id.etTituloAlarma)
        val etNotas = view.findViewById<EditText>(R.id.etNotasAlarma)
        val btnFecha = view.findViewById<Button>(R.id.btnElegirFechaAlarma)
        val btnHora = view.findViewById<Button>(R.id.btnElegirHoraAlarma)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarAlarmaGeneral)

        if (idAlarmaEditar != -1) {
            tvTituloPantalla.text = "Modificar Alarma"
            btnGuardar.text = "Guardar Cambios"

            viewLifecycleOwner.lifecycleScope.launch {
                val daoLectura = AppDatabase.getDatabase(requireContext()).alarmaGeneralDao()
                val alarma: com.azahara.proyecto_final_azahara.model.AlarmaGeneral? = withContext(Dispatchers.IO) {
                    daoLectura.getAlarmaById(idAlarmaEditar)
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

        btnFecha.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Día de la alarma")
                .setSelection(calendarioAlarma.timeInMillis)
                .build()

            datePicker.addOnPositiveButtonClickListener { milisegundos: Long ->
                calendarioAlarma.timeInMillis = milisegundos
                fechaSeleccionada = true
                val formatoVisual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                btnFecha.text = formatoVisual.format(calendarioAlarma.time)
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

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