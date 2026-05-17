package com.azahara.proyecto_final_azahara.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.azahara.proyecto_final_azahara.R
import com.azahara.proyecto_final_azahara.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etUsuario = view.findViewById<EditText>(R.id.etUsuarioLogin)
        val etPassword = view.findViewById<EditText>(R.id.etPasswordLogin)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val btnIrRegistro = view.findViewById<Button>(R.id.btnIrRegistro)

        btnLogin.setOnClickListener {
            val usuarioIntroducido = etUsuario.text.toString().trim()
            val passwordIntroducida = etPassword.text.toString().trim()

            if (usuarioIntroducido.isBlank() || passwordIntroducida.isBlank()) {
                Toast.makeText(requireContext(), "Por favor, rellena todos los datos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(requireContext(), "Verificando credenciales...", Toast.LENGTH_SHORT).show()

            viewLifecycleOwner.lifecycleScope.launch {
                val db = AppDatabase.getDatabase(requireContext())

                val usuarioEncontrado = withContext(Dispatchers.IO) {
                    db.usuarioDao().login(usuarioIntroducido, passwordIntroducida)
                }

                if (usuarioEncontrado != null) {
                    Toast.makeText(requireContext(), "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_login_to_dashboard)
                } else {
                    Toast.makeText(requireContext(), "Usuario o contraseña incorrectos", Toast.LENGTH_LONG).show()
                }
            }
        }

        btnIrRegistro.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_registro)
        }
    }
}