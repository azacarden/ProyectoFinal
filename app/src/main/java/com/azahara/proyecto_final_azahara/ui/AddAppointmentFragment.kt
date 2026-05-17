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
import com.azahara.proyecto_final_azahara.viewmodel.AppointmentViewModel
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

class AddAppointmentFragment : Fragment(R.layout.fragment_add_appointment) {

    private lateinit var viewModel: AppointmentViewModel
    private val calendarioCita = Calendar.getInstance()
    private var fechaSeleccionada = false
    private var horaSeleccionada = false

    // Si esta variable se queda en -1 es una cita NUEVA; si cambia, es una EDICIÓN
    private var idCitaEditar: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val dao = database.citaMedicaDao()
        viewModel = AppointmentViewModel(dao)

        // Recuperamos el ID si venimos de pulsar una tarjeta
        idCitaEditar = arguments?.getInt("CITA_ID_EDITAR", -1) ?: -1

        val tvTituloPantalla = view.findViewById<TextView>(R.id.tvTituloAddCita)
        val etTitulo = view.findViewById<EditText>(R.id.etTituloCita)
        val etEspecialista = view.findViewById<EditText>(R.id.etEspecialista)
        val etNotas = view.findViewById<EditText>(R.id.etNotasCita)
        val btnFecha = view.findViewById<Button>(R.id.btnElegirFecha)
        val btnHora = view.findViewById<Button>(R.id.btnElegirHora)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarCita)

        // MODO EDICIÓN: Si recibimos un ID válido, rellenamos los campos con la información de Room
        if (idCitaEditar != -1) {
            tvTituloPantalla.text = "Modificar Cita Médica"
            btnGuardar.text = "Guardar Cambios"

            viewLifecycleOwner.lifecycleScope.launch {
                val cita = withContext(Dispatchers.IO) { dao.getCitaById(idCitaEditar) }
                cita?.let {
                    etTitulo.setText(it.titulo)
                    etEspecialista.setText(it.especialista)
                    etNotas.setText(it.notas)

                    calendarioCita.timeInMillis = it.fechaHora
                    fechaSeleccionada = true
                    horaSeleccionada = true

                    val sdfFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val sdfHora = SimpleDateFormat("HH:mm", Locale.getDefault())
                    btnFecha.text = sdfFecha.format(Date(it.fechaHora))
                    btnHora.text = sdfHora.format(Date(it.fechaHora))
                }
            }
        }

        // Selector de Fecha
        btnFecha.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Seleccionar fecha")
                .setSelection(calendarioCita.timeInMillis)
                .build()

            datePicker.addOnPositiveButtonClickListener { milisegundos ->
                calendarioCita.timeInMillis = milisegundos
                fechaSeleccionada = true
                val formatoVisual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                btnFecha.text = formatoVisual.format(calendarioCita.time)
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        // Selector de Hora
        btnHora.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(calendarioCita.get(Calendar.HOUR_OF_DAY))
                .setMinute(calendarioCita.get(Calendar.MINUTE))
                .setTitleText("Seleccionar hora")
                .build()

            timePicker.addOnPositiveButtonClickListener {
                calendarioCita.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                calendarioCita.set(Calendar.MINUTE, timePicker.minute)
                calendarioCita.set(Calendar.SECOND, 0)
                horaSeleccionada = true
                btnHora.text = String.format(Locale.getDefault(), "%02d:%02d", timePicker.hour, timePicker.minute)
            }
            timePicker.show(parentFragmentManager, "TIME_PICKER")
        }

        // Guardar o Actualizar
        btnGuardar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val especialista = etEspecialista.text.toString().trim()
            val notas = etNotas.text.toString().trim()

            if (titulo.isBlank() || especialista.isBlank() || !fechaSeleccionada || !horaSeleccionada) {
                Toast.makeText(requireContext(), "Por favor, rellena todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (idCitaEditar == -1) {
                // Modo Crear
                viewModel.guardarCita(titulo, especialista, calendarioCita.timeInMillis, notas)
            } else {
                // Modo Editar: Primero cancelamos la alarma antigua por si cambió de hora
                viewModel.ultimaCitaGuardada?.let { com.azahara.proyecto_final_azahara.alarm.AlarmHelper(requireContext()).cancelarAlarmaCita(it) }
                // Guardamos los cambios
                viewModel.actualizarCita(idCitaEditar, titulo, especialista, calendarioCita.timeInMillis, notas)
            }
        }

        // Observar resultado
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.guardadoExitoso.collect { exito ->
                    if (exito == true) {
                        Toast.makeText(requireContext(), "Cita procesada correctamente", Toast.LENGTH_SHORT).show()

                        // Programamos la alarma (sea nueva o modificada con la nueva hora)
                        viewModel.ultimaCitaGuardada?.let { cita ->
                            com.azahara.proyecto_final_azahara.alarm.AlarmHelper(requireContext()).programarAlarmaCita(cita)
                        }

                        viewModel.resetearEstado()
                        findNavController().navigateUp()
                    } else if (exito == false) {
                        Toast.makeText(requireContext(), "Error al procesar la cita", Toast.LENGTH_SHORT).show()
                        viewModel.resetearEstado()
                    }
                }
            }
        }
    }
}