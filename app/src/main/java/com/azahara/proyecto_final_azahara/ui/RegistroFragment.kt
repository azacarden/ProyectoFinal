package com.azahara.proyecto_final_azahara.ui

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioGroup
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
import com.azahara.proyecto_final_azahara.repository.UserRepository
import com.azahara.proyecto_final_azahara.viewmodel.AuthState
import com.azahara.proyecto_final_azahara.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class RegistroFragment : Fragment(R.layout.fragment_registro) {

    private val viewModel: AuthViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val dao = AppDatabase.getDatabase(requireContext()).usuarioDao()
                val auth = FirebaseAuth.getInstance()
                val firestore = FirebaseFirestore.getInstance()
                val repo = UserRepository(dao, auth, firestore)
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(repo) as T
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etUsuario = view.findViewById<EditText>(R.id.etRegUsuario)
        val etCorreo = view.findViewById<EditText>(R.id.etRegCorreo)
        val etPass = view.findViewById<EditText>(R.id.etRegPass)
        val rgRol = view.findViewById<RadioGroup>(R.id.rgRolRegistro)
        val btnCrear = view.findViewById<Button>(R.id.btnRegCrear)
        val pbRegistro = view.findViewById<ProgressBar>(R.id.pbRegistro)
        val tvIrLogin = view.findViewById<TextView>(R.id.tvIrLogin)

        tvIrLogin.setOnClickListener {
            findNavController().navigateUp()
        }

        btnCrear.setOnClickListener {
            val user = etUsuario.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val pass = etPass.text.toString().trim()

            val rol = if (rgRol.checkedRadioButtonId == R.id.rbRolCuidador) "Cuidador" else "Paciente"

            if (user.isEmpty() || correo.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                Toast.makeText(requireContext(), "Formato de correo inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 6) {
                Toast.makeText(requireContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.registrarUsuario(user, correo, pass, rol)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Idle -> pbRegistro.visibility = View.GONE
                        is AuthState.Loading -> pbRegistro.visibility = View.VISIBLE
                        is AuthState.Success -> {
                            pbRegistro.visibility = View.GONE

                            val prefs = requireContext().getSharedPreferences("SesionUsuario", android.content.Context.MODE_PRIVATE)
                            prefs.edit().apply {
                                putString("usuario_identificado", state.nombreUsuario)
                                putString("rol_usuario", state.rol)
                                apply()
                            }

                            Toast.makeText(requireContext(), "Cuenta registrada: ${state.nombreUsuario}", Toast.LENGTH_SHORT).show()

                            viewModel.resetState()

                            findNavController().navigate(R.id.dashboardFragment)
                        }
                        is AuthState.Error -> {
                            pbRegistro.visibility = View.GONE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            viewModel.resetState()
                        }
                    }
                }
            }
        }
    }
}