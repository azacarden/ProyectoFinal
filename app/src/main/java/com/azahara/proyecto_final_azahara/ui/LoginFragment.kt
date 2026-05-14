package com.azahara.proyecto_final_azahara.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.azahara.proyecto_final_azahara.R

class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etUsuario = view.findViewById<EditText>(R.id.etUsuarioLogin)
        val etPassword = view.findViewById<EditText>(R.id.etPasswordLogin)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val btnIrRegistro = view.findViewById<Button>(R.id.btnIrRegistro)

        btnLogin.setOnClickListener {
            val usuario = etUsuario.text.toString()
            val password = etPassword.text.toString()

            // REQUISITO TÉCNICO: Validaciones de cliente
            if (usuario.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Por favor, rellena todos los datos", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: Aquí conectaremos el AuthViewModel mediante inyección de dependencias
                Toast.makeText(requireContext(), "Iniciando sesión...", Toast.LENGTH_SHORT).show()

                // Navegamos al Dashboard si el login es correcto
                findNavController().navigate(R.id.action_login_to_dashboard)
            }
        }

        btnIrRegistro.setOnClickListener {
            // Navegamos a la pantalla de registro
            findNavController().navigate(R.id.action_login_to_registro)
        }
    }
}