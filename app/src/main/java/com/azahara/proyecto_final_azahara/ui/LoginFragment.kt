package com.azahara.proyecto_final_azahara.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
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

class LoginFragment : Fragment(R.layout.fragment_login) {

    // CORREGIDO: Usamos la factoría para construir el ViewModel con sus dependencias
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

        val etUsuario = view.findViewById<EditText>(R.id.etLoginUsuario)
        val etPass = view.findViewById<EditText>(R.id.etLoginPass)
        val btnEntrar = view.findViewById<Button>(R.id.btnLoginEntrar)
        val pbLogin = view.findViewById<ProgressBar>(R.id.pbLogin)
        val tvIrRegistro = view.findViewById<TextView>(R.id.tvIrRegistro)

        tvIrRegistro.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_registro)
        }

        btnEntrar.setOnClickListener {
            val user = etUsuario.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.loginConUsuario(user, pass)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Idle -> pbLogin.visibility = View.GONE
                        is AuthState.Loading -> pbLogin.visibility = View.VISIBLE
                        is AuthState.Success -> {
                            pbLogin.visibility = View.GONE // pbRegistro en RegistroFragment

                            val prefs = requireContext().getSharedPreferences("SesionUsuario", android.content.Context.MODE_PRIVATE)
                            prefs.edit().apply {
                                putString("firebase_uid", state.uid) // <--- ¡NUEVO! Guardamos el UID alfanumérico seguro
                                putString("usuario_identificado", state.nombreUsuario)
                                putString("rol_usuario", state.rol)
                                apply()
                            }

                            viewModel.resetState()
                            findNavController().navigate(R.id.dashboardFragment)
                        }
                        is AuthState.Error -> {
                            pbLogin.visibility = View.GONE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            viewModel.resetState()
                        }
                    }
                }
            }
        }
    }
}