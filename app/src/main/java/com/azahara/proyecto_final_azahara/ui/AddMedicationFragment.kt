package com.azahara.proyecto_final_azahara.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.azahara.proyecto_final_azahara.R
import com.azahara.proyecto_final_azahara.alarm.AlarmHelper
import com.azahara.proyecto_final_azahara.data.local.AppDatabase
import com.azahara.proyecto_final_azahara.data.network.RetrofitClient
import com.azahara.proyecto_final_azahara.repository.CimaRepository
import com.azahara.proyecto_final_azahara.viewmodel.AddMedicationViewModel
import com.azahara.proyecto_final_azahara.viewmodel.CimaUiState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class AddMedicationFragment : Fragment(R.layout.fragment_add_medication) {

    // Inicialización correcta usando Factory para inyectar dependencias y preservar estado
    private val viewModel: AddMedicationViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(requireContext())
                val dao = database.medicamentoDao()
                val repository = CimaRepository(RetrofitClient.cimaApi)
                @Suppress("UNCHECKED_CAST")
                return AddMedicationViewModel(repository, dao) as T
            }
        }
    }

    private val listaHoras = ArrayList<String>()
    private var idMedEditar: Int = -1
    private var urlProspectoGuardada: String? = null
    private var contraindicacionesGuardadas: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        val rgFrecuencia = view.findViewById<RadioGroup>(R.id.rgFrecuencia)
        val rbSemanal = view.findViewById<RadioButton>(R.id.rbSemanal)
        val rbMensual = view.findViewById<RadioButton>(R.id.rbMensual)
        val rbDiaria = view.findViewById<RadioButton>(R.id.rbDiaria)

        val llOpcionesFrecuencia = view.findViewById<View>(R.id.llOpcionesFrecuencia)
        val tilDiaSemana = view.findViewById<View>(R.id.tilDiaSemana)
        val tilDiaMes = view.findViewById<View>(R.id.tilDiaMes)
        val actvDiaSemana = view.findViewById<AutoCompleteTextView>(R.id.actvDiaSemana)
        val actvDiaMes = view.findViewById<AutoCompleteTextView>(R.id.actvDiaMes)

        val diasSemana = arrayOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val diasMes = (1..31).map { it.toString() }.toTypedArray()

        actvDiaSemana.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, diasSemana))
        actvDiaMes.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, diasMes))

        rgFrecuencia.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbDiaria -> llOpcionesFrecuencia.visibility = View.GONE
                R.id.rbSemanal -> {
                    llOpcionesFrecuencia.visibility = View.VISIBLE
                    tilDiaSemana.visibility = View.VISIBLE
                    tilDiaMes.visibility = View.GONE
                }
                R.id.rbMensual -> {
                    llOpcionesFrecuencia.visibility = View.VISIBLE
                    tilDiaSemana.visibility = View.GONE
                    tilDiaMes.visibility = View.VISIBLE
                }
            }
        }

        if (idMedEditar != -1) {
            tvTituloPantalla.text = "Modificar Medicamento"
            btnGuardar.text = "Guardar Cambios"
            view.findViewById<View>(R.id.llBuscador).visibility = View.GONE

            viewLifecycleOwner.lifecycleScope.launch {
                val med = withContext(Dispatchers.IO) { AppDatabase.getDatabase(requireContext()).medicamentoDao().getMedicamentoById(idMedEditar) }
                med?.let {
                    etNombre.setText(it.nombre)
                    etMensaje.setText(it.mensajePersonalizado)
                    urlProspectoGuardada = it.urlProspecto
                    contraindicacionesGuardadas = it.contraindicaciones
                    when (it.frecuencia) {
                        "Semanal" -> { rbSemanal.isChecked = true; actvDiaSemana.setText(it.diaEspecifico, false) }
                        "Mensual" -> { rbMensual.isChecked = true; actvDiaMes.setText(it.diaEspecifico, false) }
                        else -> rbDiaria.isChecked = true
                    }
                    if (it.horaToma.isNotBlank()) {
                        listaHoras.addAll(it.horaToma.split(", "))
                        tvHorasSeleccionadas.text = "Horas seleccionadas: ${it.horaToma}"
                    }
                }
            }
        }

        btnAgregarHora.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).setTitleText("Añadir hora de toma").build()
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
            val frecuencia = when (rgFrecuencia.checkedRadioButtonId) {
                R.id.rbSemanal -> "Semanal"
                R.id.rbMensual -> "Mensual"
                else -> "Diaria"
            }
            val diaEspecifico = if (frecuencia == "Semanal") actvDiaSemana.text.toString() else if (frecuencia == "Mensual") actvDiaMes.text.toString() else null

            viewModel.validarYGuardar(nombre, horasTexto, mensaje, frecuencia, diaEspecifico, urlProspectoGuardada, contraindicacionesGuardadas, idMedEditar)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is CimaUiState.Loading -> progressBar.visibility = View.VISIBLE
                        is CimaUiState.SuccessList -> {
                            progressBar.visibility = View.GONE
                            val nombres = state.medicamentos.map { "${it.nombre} (${it.labtitular})" }.toTypedArray()
                            MaterialAlertDialogBuilder(requireContext()).setTitle("Seleccione").setItems(nombres) { _, idx ->
                                etNombre.setText(state.medicamentos[idx].nombre)
                                viewModel.cargarDetalle(state.medicamentos[idx])
                            }.show()
                        }
                        is CimaUiState.SuccessDetail -> {
                            progressBar.visibility = View.GONE
                            urlProspectoGuardada = state.detalle.urlProspecto
                            contraindicacionesGuardadas = state.detalle.contraindicaciones
                            tvViasAdministracion.apply { visibility = View.VISIBLE; text = "Vía: ${state.detalle.viasAdministracion}" }
                            tvContraindicaciones.apply { visibility = View.VISIBLE; text = "⚠️ Contraindicaciones:\n${Html.fromHtml(state.detalle.contraindicaciones ?: "", Html.FROM_HTML_MODE_LEGACY)}" }
                            btnVerProspecto.apply { visibility = View.VISIBLE; setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(state.detalle.urlProspecto))) } }
                        }
                        is CimaUiState.Error -> {
                            progressBar.visibility = View.GONE
                            Snackbar.make(view, state.message, Snackbar.LENGTH_INDEFINITE).setAction("Reintentar") { viewModel.buscarMedicamento(etSearch.text.toString().trim()) }.show()
                        }
                        is CimaUiState.SaveSuccess -> {
                            Toast.makeText(requireContext(), "Guardado", Toast.LENGTH_SHORT).show()
                            viewModel.ultimoMedicamentoGuardado?.let { AlarmHelper(requireContext()).programarAlarma(it) }
                            findNavController().navigateUp()
                        }
                        else -> progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }
}