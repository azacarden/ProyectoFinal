package com.azahara.proyecto_final_azahara.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.azahara.proyecto_final_azahara.R
import com.azahara.proyecto_final_azahara.data.local.AppDatabase
import com.azahara.proyecto_final_azahara.data.network.RetrofitClient
import com.azahara.proyecto_final_azahara.repository.CimaRepository
import com.azahara.proyecto_final_azahara.viewmodel.AddMedicationViewModel
import com.azahara.proyecto_final_azahara.viewmodel.CimaUiState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class AddMedicationFragment : Fragment(R.layout.fragment_add_medication) {

    private lateinit var viewModel: AddMedicationViewModel
    private val listaHoras = ArrayList<String>()
    private var idMedEditar: Int = -1

    // Variables dinámicas para retener las URLs clínicas y guardarlas en Room
    private var urlProspectoGuardada: String? = null
    private var contraindicacionesGuardadas: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val dao = database.medicamentoDao()
        val repository = CimaRepository(RetrofitClient.cimaApi)
        viewModel = AddMedicationViewModel(repository, dao)

        idMedEditar = arguments?.getInt("MEDICAMENTO_ID_EDITAR", -1) ?: -1

        val tvTituloPantalla = view.findViewById<TextView>(R.id.tvTituloAdd)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        val btnSearch = view.findViewById<Button>(R.id.btnSearch)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etMensaje = view.findViewById<EditText>(R.id.etMensaje)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardar)

        val btnAgregarHora = view.findViewById<Button>(R.id.btnAgregarHora)
        val tvHorasSeleccionadas = view.findViewById<TextView>(R.id.tvHorasSeleccionadas)

        val tvViasAdministracion = view.findViewById<TextView>(R.id.tvViasAdministracion)
        val tvContraindicaciones = view.findViewById<TextView>(R.id.tvContraindicaciones)
        val btnVerProspecto = view.findViewById<Button>(R.id.btnVerProspecto)

        if (idMedEditar != -1) {
            tvTituloPantalla.text = "Modificar Medicamento"
            btnGuardar.text = "Guardar Cambios"
            view.findViewById<View>(R.id.llBuscador).visibility = View.GONE

            viewLifecycleOwner.lifecycleScope.launch {
                val med = withContext(Dispatchers.IO) { dao.getMedicamentoById(idMedEditar) }
                med?.let {
                    etNombre.setText(it.nombre)
                    etMensaje.setText(it.mensajePersonalizado)
                    urlProspectoGuardada = it.urlProspecto
                    contraindicacionesGuardadas = it.contraindicaciones

                    if (it.horaToma.isNotBlank()) {
                        listaHoras.addAll(it.horaToma.split(", "))
                        tvHorasSeleccionadas.text = "Horas seleccionadas: ${it.horaToma}"
                    }
                }
            }
        }

        btnAgregarHora.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Añadir hora de toma")
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val horaFormateada = String.format(Locale.getDefault(), "%02d:%02d", timePicker.hour, timePicker.minute)
                if (!listaHoras.contains(horaFormateada)) {
                    listaHoras.add(horaFormateada)
                    listaHoras.sort()
                    tvHorasSeleccionadas.text = "Horas seleccionadas: ${listaHoras.joinToString(", ")}"
                }
            }
            timePicker.show(parentFragmentManager, "TIME_PICKER")
        }

        btnSearch.setOnClickListener {
            val query = etSearch.text.toString().trim()
            viewModel.buscarMedicamento(query)
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val mensaje = etMensaje.text.toString().trim()
            val horasTexto = listaHoras.joinToString(", ")

            if (nombre.isBlank() || horasTexto.isBlank()) {
                Toast.makeText(requireContext(), "El nombre y al menos una hora son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (idMedEditar == -1) {
                // Pasamos los nuevos datos al guardar
                viewModel.guardarMedicamentoLocal(nombre, horasTexto, mensaje, urlProspectoGuardada, contraindicacionesGuardadas)
            } else {
                // Pasamos los nuevos datos al editar
                viewModel.actualizarMedicamentoLocal(idMedEditar, nombre, horasTexto, mensaje, urlProspectoGuardada, contraindicacionesGuardadas)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is CimaUiState.Idle -> { progressBar.visibility = View.GONE }
                        is CimaUiState.Loading -> { progressBar.visibility = View.VISIBLE }
                        is CimaUiState.SuccessList -> {
                            progressBar.visibility = View.GONE
                            if (!state.medicamentos.isNullOrEmpty()) {
                                val nombres = state.medicamentos.map { "${it.nombre} (${it.labtitular})" }.toTypedArray()
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Seleccione el medicamento exacto")
                                    .setItems(nombres) { _, idx ->
                                        val seleccion = state.medicamentos[idx]
                                        etNombre.setText(seleccion.nombre)
                                        viewModel.cargarDetalle(seleccion)
                                    }.show()
                            } else {
                                Toast.makeText(requireContext(), "No se han encontrado medicamentos", Toast.LENGTH_SHORT).show()
                            }
                        }
                        is CimaUiState.SuccessDetail -> {
                            progressBar.visibility = View.GONE
                            val detalle = state.detalle

                            // Retenemos los enlaces clínicos en el fragmento
                            urlProspectoGuardada = detalle.urlProspecto
                            contraindicacionesGuardadas = detalle.contraindicaciones

                            if (!detalle.viasAdministracion.isNullOrEmpty()) {
                                tvViasAdministracion.visibility = View.VISIBLE
                                tvViasAdministracion.text = "Vía de administración: ${detalle.viasAdministracion}"
                            }
                            if (!detalle.contraindicaciones.isNullOrEmpty()) {
                                tvContraindicaciones.visibility = View.VISIBLE
                                val textoLimpio = Html.fromHtml(detalle.contraindicaciones, Html.FROM_HTML_MODE_LEGACY).toString()
                                tvContraindicaciones.text = "⚠️ Contraindicaciones:\n$textoLimpio"
                            }
                            if (!detalle.urlProspecto.isNullOrEmpty()) {
                                btnVerProspecto.visibility = View.VISIBLE
                                btnVerProspecto.setOnClickListener {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(detalle.urlProspecto))
                                    startActivity(intent)
                                }
                            }
                        }
                        is CimaUiState.Error -> {
                            progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                        is CimaUiState.SaveSuccess -> {
                            progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Medicamento guardado con éxito", Toast.LENGTH_SHORT).show()

                            // -----------------------------------------------------
                            // ¡EL DISPARADOR QUE FALTABA!
                            // -----------------------------------------------------
                            viewModel.ultimoMedicamentoGuardado?.let { med ->
                                com.azahara.proyecto_final_azahara.alarm.AlarmHelper(requireContext()).programarAlarma(med)
                            }

                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }
    }
}