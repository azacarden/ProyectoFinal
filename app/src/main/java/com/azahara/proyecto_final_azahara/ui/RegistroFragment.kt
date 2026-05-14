package com.azahara.proyecto_final_azahara.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.azahara.proyecto_final_azahara.R

class RegistroFragment : Fragment(R.layout.fragment_registro) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etUsuario = view.findViewById<EditText>(R.id.etUsuarioRegistro)
        val etCorreo = view.findViewById<EditText>(R.id.etCorreoRegistro)
        val etPassword = view.findViewById<EditText>(R.id.etPasswordRegistro)
        val rgRol = view.findViewById<RadioGroup>(R.id.rgRol)
        val btnRegistrar = view.findViewById<Button>(R.id.btnRegistrar)

        btnRegistrar.setOnClickListener {
            val usuario = etUsuario.text.toString()
            val correo = etCorreo.text.toString()
            val password = etPassword.text.toString()

            // Validaciones de cliente
            if (usuario.isBlank() || correo.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Faltan datos por rellenar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Identificar el rol seleccionado
            val rol = if (rgRol.checkedRadioButtonId == R.id.rbPaciente) "Paciente" else "Cuidador"

            // TODO: Conectar con AuthViewModel para guardar en Room y Firebase
            Toast.makeText(requireContext(), "Cuenta creada como $rol", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp() // Volver al Login tras registrarse
        }
    }
}