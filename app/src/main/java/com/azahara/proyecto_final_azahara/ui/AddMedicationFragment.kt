package com.azahara.proyecto_final_azahara.ui

import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.azahara.proyecto_final_azahara.R
import com.azahara.proyecto_final_azahara.data.local.AppDatabase
import com.azahara.proyecto_final_azahara.data.network.RetrofitClient
import com.azahara.proyecto_final_azahara.repository.CimaRepository
import com.azahara.proyecto_final_azahara.viewmodel.AddMedicationViewModel
import com.azahara.proyecto_final_azahara.viewmodel.CimaUiState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class AddMedicationFragment : Fragment(R.layout.fragment_add_medication) {

    private lateinit var viewModel: AddMedicationViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicializar Base de Datos, Repositorio y ViewModel
        val database = AppDatabase.getDatabase(requireContext())
        val repository = CimaRepository(RetrofitClient.cimaApi)
        viewModel = AddMedicationViewModel(repository, database.medicamentoDao())

        // 2. Enlazar los componentes de la interfaz XML
        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        val btnSearch = view.findViewById<Button>(R.id.btnSearch)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etHora = view.findViewById<EditText>(R.id.etHora)
        val etMensaje = view.findViewById<EditText>(R.id.etMensaje)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardar)

        // 3. Listener para buscar el fármaco
        btnSearch.setOnClickListener {
            val query = etSearch.text.toString().trim()
            viewModel.buscarMedicamento(query)
        }

        // 4. Listener para validar y guardar en local
        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val hora = etHora.text.toString().trim()
            val mensaje = etMensaje.text.toString().trim()

            if (nombre.isBlank() || hora.isBlank()) {
                Toast.makeText(requireContext(), "El nombre y la hora son obligatorios", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.guardarMedicamentoLocal(nombre, hora, mensaje)
            }
        }

        // 5. Escucha de los estados (UDF con Flow)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->

                    when (state) {
                        is CimaUiState.Idle -> {
                            progressBar.visibility = View.GONE
                        }
                        is CimaUiState.Loading -> {
                            progressBar.visibility = View.VISIBLE
                        }
                        is CimaUiState.SuccessList -> {
                            progressBar.visibility = View.GONE
                            if (!state.medicamentos.isNullOrEmpty()) {
                                val nombres = state.medicamentos.map { "${it.nombre} (${it.labtitular})" }.toTypedArray()
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Seleccione el medicamento exacto")
                                    .setItems(nombres) { _, idx ->
                                        val seleccion = state.medicamentos[idx]
                                        etNombre.setText(seleccion.nombre)
                                        viewModel.cargarDetalle(seleccion.nregistro, seleccion.nombre)
                                    }.show()
                            } else {
                                Toast.makeText(requireContext(), "No se han encontrado medicamentos", Toast.LENGTH_SHORT).show()
                            }
                        }
                        is CimaUiState.SuccessDetail -> {
                            progressBar.visibility = View.GONE
                            // CORREGIDO: state.detalle (en singular)
                            state.detalle.posologia?.let {
                                val textoLimpio = Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY).toString()
                                etMensaje.setText(textoLimpio)
                            }
                        }
                        is CimaUiState.Error -> {
                            progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                        is CimaUiState.SaveSuccess -> {
                            progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Medicamento guardado con éxito", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                    }

                }
            }
        }
    }
}