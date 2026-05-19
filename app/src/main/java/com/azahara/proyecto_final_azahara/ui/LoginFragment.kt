package com.azahara.proyecto_final_azahara.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.azahara.proyecto_final_azahara.R
import com.azahara.proyecto_final_azahara.data.local.AppDatabase
import com.azahara.proyecto_final_azahara.repository.UserRepository
import com.azahara.proyecto_final_azahara.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var authViewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicialización correcta de la cadena de dependencias bajo MVVM
        val db = AppDatabase.getDatabase(requireContext())
        val repository = UserRepository(db.usuarioDao(), FirebaseAuth.getInstance())
        authViewModel = AuthViewModel(repository)

        val etUsuario = view.findViewById<EditText>(R.id.etUsuarioLogin)
        val etPassword = view.findViewById<EditText>(R.id.etPasswordLogin)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val btnIrRegistro = view.findViewById<Button>(R.id.btnIrRegistro)

        btnLogin.setOnClickListener {
            val usuarioIntroducido = etUsuario.text.toString().trim()
            val passwordIntroducida = etPassword.text.toString().trim()

            // Delegamos la validación técnica y ejecución al ViewModel de manera limpia
            authViewModel.iniciarSesion(usuarioIntroducido, passwordIntroducida)
        }

        btnIrRegistro.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_registro)
        }

        // Observamos de manera segura y reactiva el flujo de estado de autenticación (StateFlow)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.estadoLogin.collect { estado ->
                    when {
                        estado.contains("¡Bienvenido") -> {
                            // Extraemos el rol de la cadena de estado para persistir la sesión localmente
                            val esCuidador = estado.contains("Rol: Cuidador")
                            val rolAsignado = if (esCuidador) "Cuidador" else "Paciente"

                            val prefs = requireContext().getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE)
                            prefs.edit().apply {
                                putString("usuario_identificado", etUsuario.text.toString().trim())
                                putString("rol_usuario", rolAsignado)
                                apply()
                            }

                            Toast.makeText(requireContext(), estado, Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_login_to_dashboard)
                        }
                        estado.contains("Error") || estado.contains("denegado") -> {
                            Toast.makeText(requireContext(), estado, Toast.LENGTH_LONG).show()
                        }
                        estado.contains("Comprobando") -> {
                            // Aquí podrías mostrar un Spinner de carga si lo deseas
                        }
                    }
                }
            }
        }
    }
}