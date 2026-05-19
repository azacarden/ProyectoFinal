package com.azahara.proyecto_final_azahara.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.azahara.proyecto_final_azahara.R
import com.azahara.proyecto_final_azahara.data.local.AppDatabase
import com.azahara.proyecto_final_azahara.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RegistroFragment : Fragment(R.layout.fragment_registro) {

    private lateinit var userRepository: UserRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        userRepository = UserRepository(db.usuarioDao(), FirebaseAuth.getInstance())

        val etUsuario = view.findViewById<EditText>(R.id.etUsuarioRegistro)
        val etPassword = view.findViewById<EditText>(R.id.etPasswordRegistro)
        val rgRol = view.findViewById<RadioGroup>(R.id.rgRol)
        val btnRegistrar = view.findViewById<Button>(R.id.btnRegistrar)

        btnRegistrar.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (usuario.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Faltan datos por rellenar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rol = if (rgRol.checkedRadioButtonId == R.id.rbPaciente) "Paciente" else "Cuidador"
            val correoGenerado = if (usuario.contains("@")) usuario else "$usuario@pastillero.app"

            viewLifecycleOwner.lifecycleScope.launch {
                val resultado = userRepository.registrarUsuarioLocalYSincronizar(
                    nombre = usuario,
                    correo = correoGenerado,
                    contrasenaPlana = password,
                    rol = rol
                )

                resultado.fold(
                    onSuccess = {
                        Toast.makeText(requireContext(), "Cuenta registrada con éxito para $usuario", Toast.LENGTH_LONG).show()
                        findNavController().navigateUp()
                    },
                    onFailure = { excepcion ->
                        Toast.makeText(requireContext(), excepcion.message, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}