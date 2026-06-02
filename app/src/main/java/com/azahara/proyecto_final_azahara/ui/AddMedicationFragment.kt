package com.azahara.proyecto_final_azahara.ui

import android.content.Context
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
import com.azahara.proyecto_final_azahara.repository.MedicationRepository
import com.azahara.proyecto_final_azahara.viewmodel.AddMedicationViewModel
import com.azahara.proyecto_final_azahara.viewmodel.CimaUiState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class AddMedicationFragment : Fragment(R.layout.fragment_add_medication) {

    private val viewModel: AddMedicationViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(requireContext())
                val dao = database.medicamentoDao()
                val cimaRepo = CimaRepository(RetrofitClient.cimaApi)
                val medRepo = MedicationRepository(dao, com.google.firebase.firestore.FirebaseFirestore.getInstance())
                @Suppress("UNCHECKED_CAST")
                return AddMedicationViewModel(cimaRepo, medRepo, dao) as T
            }
        }
    }

    private val listaHoras = ArrayList<String>()
    private var idMedEditar: String? = null
    private var urlProspectoGuardada: String? = null
    private var contraindicacionesGuardadas: String? = null
    private lateinit var llOpcionesFrecuencia: View
    private lateinit var tilDiaSemana: View
    private lateinit var tilDiaMes: View
    private lateinit var actvDiaSemana: AutoCompleteTextView
    private lateinit var actvDiaMes: AutoCompleteTextView


    // Abre el escaner de códigos de barras
    private val barcodeLauncher = registerForActivityResult(com.journeyapps.barcodescanner.ScanContract()) { result ->
        if (result.contents != null) {
            procesarDataMatrix(result.contents)
        } else {
            Toast.makeText(requireContext(), "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    // Extrae el CN de la caja del medicamento y lo busca en la API de CIMA
    private fun procesarDataMatrix(datosBrutos: String) {
        val indice = datosBrutos.indexOf("847000")

        if (indice != -1 && datosBrutos.length >= indice + 12) {
            val inicioCN = indice + 6
            val codigoNacional = datosBrutos.substring(inicioCN, inicioCN + 6)
            viewModel.buscarMedicamentoPorCN(codigoNacional)
        } else {
            Toast.makeText(requireContext(), "Código no válido o medicamento no español.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        idMedEditar = arguments?.getString("MEDICAMENTO_ID_EDITAR")

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
        val btnEscanearCaja = view.findViewById<ImageButton>(R.id.btnEscanearCaja)

        llOpcionesFrecuencia = view.findViewById(R.id.llOpcionesFrecuencia)
        tilDiaSemana = view.findViewById(R.id.tilDiaSemana)
        tilDiaMes = view.findViewById(R.id.tilDiaMes)
        actvDiaSemana = view.findViewById(R.id.actvDiaSemana)
        actvDiaMes = view.findViewById(R.id.actvDiaMes)

        val diasSemana = arrayOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val diasMes = (1..31).map { it.toString() }.toTypedArray()
        actvDiaSemana.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, diasSemana))
        actvDiaMes.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, diasMes))

        fun actualizarVisibilidadFrecuencia(checkedId: Int) {
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

        rgFrecuencia.setOnCheckedChangeListener { _, checkedId ->
            actualizarVisibilidadFrecuencia(checkedId)
        }

        if (idMedEditar != null) {
            tvTituloPantalla.text = "Modificar Medicamento"
            btnGuardar.text = "Guardar Cambios"
            view.findViewById<View>(R.id.llBuscador).visibility = View.GONE

            viewLifecycleOwner.lifecycleScope.launch {
                val wrapper = withContext(Dispatchers.IO) {
                    AppDatabase.getDatabase(requireContext()).medicamentoDao()
                        .getMedicamentoConHorariosPorId(idMedEditar!!)
                        .firstOrNull()
                }

                wrapper?.let {
                    val med = it.medicamento
                    etNombre.setText(med.nombre)
                    etMensaje.setText(med.mensajePersonalizado)
                    urlProspectoGuardada = med.urlProspecto
                    contraindicacionesGuardadas = med.contraindicaciones

                    val idRadioButton = when (med.frecuencia) {
                        "Semanal" -> { actvDiaSemana.setText(med.diaEspecifico, false); R.id.rbSemanal }
                        "Mensual" -> { actvDiaMes.setText(med.diaEspecifico, false); R.id.rbMensual }
                        else -> R.id.rbDiaria
                    }
                    rgFrecuencia.check(idRadioButton)

                    if (it.horarios.isNotEmpty()) {
                        listaHoras.clear()
                        listaHoras.addAll(it.horarios.map { horario -> horario.horaToma })
                        tvHorasSeleccionadas.text = "Pulsa aquí si quieres eliminar algun horario: ${listaHoras.joinToString(", ")}"
                    }
                }
            }
        }

        btnAgregarHora.setOnClickListener {
            val horaActual = Calendar.getInstance()

            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(horaActual.get(Calendar.HOUR_OF_DAY)) // Asigna la hora actual
                .setMinute(horaActual.get(Calendar.MINUTE))    // Asigna el minuto actual
                .setTitleText("Añadir hora de toma")
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val horaFormateada = String.format(Locale.getDefault(), "%02d:%02d", timePicker.hour, timePicker.minute)
                if (!listaHoras.contains(horaFormateada)) {
                    listaHoras.add(horaFormateada)
                    listaHoras.sort()
                    tvHorasSeleccionadas.text = "Pulsa aquí si quieres eliminar algun horario: ${listaHoras.joinToString(", ")}"
                }
            }
            timePicker.show(parentFragmentManager, "TIME_PICKER")
        }

        tvHorasSeleccionadas.setOnClickListener {
            if (listaHoras.isEmpty()) return@setOnClickListener

            val horasArray = listaHoras.toTypedArray()
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Selecciona la hora que deseas eliminar:")
                .setItems(horasArray) { _, idx ->
                    val horaEliminar = horasArray[idx]
                    listaHoras.remove(horaEliminar)

                    if (listaHoras.isEmpty()) {
                        tvHorasSeleccionadas.text = "Ninguna hora seleccionada todavía"
                    } else {
                        tvHorasSeleccionadas.text = "Pulsa aquí si quieres eliminar algun horario: ${listaHoras.joinToString(", ")}"
                    }
                    Toast.makeText(requireContext(), "Hora $horaEliminar eliminada", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Acción del botón escanear
        btnEscanearCaja.setOnClickListener {
            val options = com.journeyapps.barcodescanner.ScanOptions().apply {
                setPrompt("Escanea el código cuadrado de la caja (Data Matrix)")
                setBeepEnabled(true)
                setOrientationLocked(false)
            }
            barcodeLauncher.launch(options)
        }
        btnSearch.setOnClickListener {
            viewModel.buscarMedicamento(etSearch.text.toString().trim())
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val mensaje = etMensaje.text.toString().trim()

            if (listaHoras.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, debes añadir al menos una hora de toma", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val horas = listaHoras.joinToString(",")
            val freq = if (rbSemanal.isChecked) "Semanal" else if (rbMensual.isChecked) "Mensual" else "Diaria"
            val dia = if (rbSemanal.isChecked) actvDiaSemana.text.toString() else if (rbMensual.isChecked) actvDiaMes.text.toString() else null

            viewLifecycleOwner.lifecycleScope.launch {
                val aviso = viewModel.verificarContraindicaciones(nombre)
                if (aviso != null) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("⚠️ Alerta de Seguridad")
                        .setMessage("$aviso\n\n¿Deseas guardarlo de todas formas?")
                        .setPositiveButton("Guardar") { _, _ -> realizarGuardado(nombre, horas, mensaje, freq, dia) }
                        .setNegativeButton("Cancelar", null)
                        .show()
                } else {
                    realizarGuardado(nombre, horas, mensaje, freq, dia)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is CimaUiState.Loading -> progressBar.visibility = View.VISIBLE
                        is CimaUiState.SuccessList -> {
                            progressBar.visibility = View.GONE
                            val nombres = state.medicamentos.map { "${it.nombre} (${it.labtitular})" }.toTypedArray()
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Seleccione")
                                .setItems(nombres) { _, idx ->
                                    etNombre.setText(state.medicamentos[idx].nombre)
                                    viewModel.cargarDetalle(state.medicamentos[idx])
                                }
                                .show()
                        }
                        is CimaUiState.SuccessDetail -> {
                            progressBar.visibility = View.GONE
                            urlProspectoGuardada = state.detalle.urlProspecto
                            contraindicacionesGuardadas = state.detalle.contraindicaciones ?: "Información no disponible digitalmente."
                            tvViasAdministracion.apply { visibility = View.VISIBLE; text = "Vía: ${state.detalle.viasAdministracion}" }
                            tvContraindicaciones.apply {
                                visibility = View.VISIBLE
                                text = "⚠️ Contraindicaciones:\n${Html.fromHtml(contraindicacionesGuardadas, Html.FROM_HTML_MODE_LEGACY)}"
                            }
                            btnVerProspecto.apply {
                                visibility = View.VISIBLE
                                setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlProspectoGuardada))) }
                            }
                        }
                        is CimaUiState.Error -> {
                            progressBar.visibility = View.GONE
                            Snackbar.make(view, state.message, Snackbar.LENGTH_LONG)
                                .setAction("Reintentar") { viewModel.buscarMedicamento(etSearch.text.toString().trim()) }
                                .show()
                        }
                        is CimaUiState.SaveSuccess -> {
                            progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Guardado", Toast.LENGTH_SHORT).show()

                            viewModel.ultimoMedicamentoGuardado?.let { wrapper ->
                                AlarmHelper(requireContext()).programarAlarma(wrapper)
                            }
                            findNavController().navigateUp()
                        }
                        else -> progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun realizarGuardado(nombre: String, hora: String, msg: String, freq: String, dia: String?) {
        val prefs = requireContext().getSharedPreferences("SesionUsuario", android.content.Context.MODE_PRIVATE)
        val miNombre = prefs.getString("usuario_identificado", "Paciente") ?: "Paciente"
        val miUidLocal = prefs.getString("firebase_uid", "") ?: ""
        val targetUid = arguments?.getString("PACIENTE_UID") ?: miUidLocal
        val targetPacienteNombre = arguments?.getString("PACIENTE_NOMBRE") ?: miNombre

        viewModel.validarYGuardar(nombre, hora, msg, freq, dia, urlProspectoGuardada, contraindicacionesGuardadas, idMedEditar, targetUid, "Añadido por: $miNombre", targetPacienteNombre)
    }
}