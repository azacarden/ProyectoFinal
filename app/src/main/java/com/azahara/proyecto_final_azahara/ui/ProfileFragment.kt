package com.azahara.proyecto_final_azahara.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.azahara.proyecto_final_azahara.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val db = FirebaseFirestore.getInstance()
    private var miUsuario = "Usuario_Local"
    private var miRol = "Paciente"
    private var miUid = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("SesionUsuario", android.content.Context.MODE_PRIVATE)
        miUsuario = prefs.getString("usuario_identificado", "Usuario_Local") ?: "Usuario_Local"
        miRol = prefs.getString("rol_usuario", "Paciente") ?: "Paciente"
        miUid = prefs.getString("firebase_uid", "") ?: ""

        val ivQrCode = view.findViewById<ImageView>(R.id.ivQrCode)
        val btnAccionDinamica = view.findViewById<Button>(R.id.btnEscanearQr)
        val tvTituloFragment = view.findViewById<TextView>(R.id.tvTituloPerfil)
        val tvMiRol = view.findViewById<TextView>(R.id.tvMiRol)
        val btnCerrarSesion = view.findViewById<Button>(R.id.btnCerrarSesion)

        // Limpieza de remanentes visuales
        view.findViewById<TextView>(R.id.tvTituloPacientes)?.visibility = View.GONE
        view.findViewById<RecyclerView>(R.id.rvPacientes)?.visibility = View.GONE

        val targetPacienteUid = arguments?.getString("PACIENTE_UID")
        val origenVista = arguments?.getString("ORIGEN") // Capturamos la bandera oculta del Paciente

        btnCerrarSesion.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres salir de tu cuenta?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Sí, salir") { _, _ ->
                    prefs.edit().clear().apply()
                    findNavController().navigate(R.id.loginFragment)
                }
                .show()
        }

        // ========================================================
        // 🛠️ MOTOR DE SEGMENTACIÓN E INYECCIÓN DE VISTAS (UX/UI)
        // ========================================================
        if (miRol == "Cuidador") {
            if (targetPacienteUid != null && targetPacienteUid != miUid) {
                // MODO A: Cuidador inspeccionando la ficha del abuelo/paciente
                view.findViewById<View>(R.id.cardQr)?.visibility = View.GONE
                btnAccionDinamica.visibility = View.GONE
                btnCerrarSesion.visibility = View.GONE

                db.collection("usuarios").document(targetPacienteUid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val nombre = doc.getString("nombreCompleto") ?: doc.getString("nombreUsuario") ?: "Desconocido"
                            val correo = doc.getString("correo") ?: "No aportado"
                            val telefono = doc.getString("telefono") ?: "No registrado"
                            val emergencia = doc.getString("contactoEmergencia") ?: "No registrado"

                            tvTituloFragment.text = "Expediente de Salud"
                            tvMiRol.text = "👤 Paciente: $nombre\n\n📧 Correo: $correo\n\n📞 Teléfono Personal: $telefono\n\n🚨 Contacto de Emergencia / Tutor: $emergencia"
                        }
                    }
            } else {
                // MODO B: Perfil personal del propio Cuidador
                view.findViewById<View>(R.id.cardQr)?.visibility = View.GONE
                btnCerrarSesion.visibility = View.VISIBLE
                tvTituloFragment.text = "Mi Cuenta (Cuidador)"

                btnAccionDinamica.apply {
                    visibility = View.VISIBLE
                    text = "Editar Mis Datos de Contacto"
                    setOnClickListener { mostrarDialogoModificarDatos(tvMiRol) }
                }
                renderizarDatosPropiosLocal(tvMiRol)
            }
        } else {
            // MODO C: EL PACIENTE LOGUEADO (DESDOBLAMIENTO DE INTERFAZ SOLICITADO)
            if (origenVista == "CARD_CUIDADORES") {
                // MODO C.1: El paciente pulsa el botón "Cuidadores" del Dashboard
                view.findViewById<View>(R.id.cardQr)?.visibility = View.VISIBLE
                ivQrCode.setImageBitmap(generarQr(miUid)) // Solo aquí mostramos el QR para que le escaneen
                btnAccionDinamica.visibility = View.GONE
                btnCerrarSesion.visibility = View.GONE // Ocultamos cerrar sesión para limpiar la vista

                tvTituloFragment.text = "Mi Cuidador Asignado"
                renderizarDatosDelCuidadorDelPaciente(tvMiRol)
            } else {
                // MODO C.2: El paciente abre "Perfil" en la barra de navegación superior (Mi Cuenta)
                view.findViewById<View>(R.id.cardQr)?.visibility = View.GONE // ¡OCULTADO EL QR!
                btnCerrarSesion.visibility = View.VISIBLE
                tvTituloFragment.text = "Mi Perfil"

                btnAccionDinamica.apply {
                    visibility = View.VISIBLE
                    text = "Editar Datos Personales"
                    setOnClickListener { mostrarDialogoModificarDatos(tvMiRol) }
                }
                renderizarDatosPersonalesCompletosPaciente(tvMiRol)
            }
        }
    }

    private fun renderizarDatosPropiosLocal(textView: TextView) {
        db.collection("usuarios").document(miUid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val nombreCompleto = doc.getString("nombreCompleto") ?: "No registrado"
                val tel = doc.getString("telefono") ?: "No registrado"
                textView.text = "👤 Nombre: $nombreCompleto\n\n📧 Usuario: $miUsuario\n\n📞 Teléfono: $tel"
            }
        }
    }

    // Renderiza la información del paciente (Nombre completo, teléfono y tutor/emergencia) sin QR
    private fun renderizarDatosPersonalesCompletosPaciente(textView: TextView) {
        db.collection("usuarios").document(miUid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val nombreComp = doc.getString("nombreCompleto") ?: "No registrado"
                val tel = doc.getString("telefono") ?: "No registrado"
                val emg = doc.getString("contactoEmergencia") ?: "No registrado"

                textView.text = "👤 Nombre Completo: $nombreComp\n\n👤 Cuenta de Usuario: $miUsuario\n\n📞 Mi Teléfono: $tel\n\n🚨 Contacto Urgencia / Tutor Legal: $emg"
            }
        }
    }

    // Descarga y muestra los datos del cuidador asignado en la pestaña del QR
    private fun renderizarDatosDelCuidadorDelPaciente(textView: TextView) {
        db.collection("vinculaciones").whereEqualTo("pacienteUid", miUid).get()
            .addOnSuccessListener { snapshots ->
                if (!snapshots.isEmpty) {
                    val docVinculo = snapshots.documents.first()
                    val cuidadorUid = docVinculo.getString("cuidadorUid") ?: ""

                    db.collection("usuarios").document(cuidadorUid).get().addOnSuccessListener { docCuidador ->
                        if (docCuidador.exists()) {
                            val nombreC = docCuidador.getString("nombreCompleto") ?: docCuidador.getString("nombreUsuario") ?: "Cuidador"
                            val telC = docCuidador.getString("telefono") ?: "No aportado"
                            val correoC = docCuidador.getString("correo") ?: "No aportado"

                            textView.text = "👤 Mi Cuidador: $nombreC\n\n📞 Teléfono: $telC\n\n📧 Correo: $correoC\n\n" +
                                    "-----------------------------------------\n" +
                                    "ℹ️ Si necesitas vincular a otro acompañante, pídele que escanee el código QR de arriba."
                        }
                    }
                } else {
                    textView.text = "⚠️ Actualmente no tienes ningún cuidador asignado.\n\n" +
                            "👉 Muestra el código QR de arriba a tu cuidador o tutor legal para enlazar vuestras cuentas."
                }
            }
    }

    // Formulario inflado dinámicamente para rellenar los datos solicitados
    private fun mostrarDialogoModificarDatos(textViewActualizar: TextView) {
        val ctx = requireContext()
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val contenedorProgramatico = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }

        val etNombreCompleto = EditText(ctx).apply {
            hint = "Nombre y Apellidos Completos"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
            layoutParams = lp
        }

        val etTelefono = EditText(ctx).apply {
            hint = "Número de Teléfono"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            layoutParams = lp
        }

        // Este edittext servirá como contacto de emergencia para pacientes, o se ocultará sutilmente si es cuidador
        val etEmergencia = EditText(ctx).apply {
            hint = "Persona de contacto de emergencia / Tutor"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
            layoutParams = lp
            if (miRol == "Cuidador") visibility = View.GONE // Los cuidadores no lo necesitan obligatoriamente
        }

        contenedorProgramatico.addView(etNombreCompleto)
        contenedorProgramatico.addView(etTelefono)
        contenedorProgramatico.addView(etEmergencia)

        // Precarga de los datos vigentes en la nube
        db.collection("usuarios").document(miUid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                etNombreCompleto.setText(doc.getString("nombreCompleto") ?: "")
                etTelefono.setText(doc.getString("telefono") ?: "")
                etEmergencia.setText(doc.getString("contactoEmergencia") ?: "")
            }
        }

        MaterialAlertDialogBuilder(ctx)
            .setTitle("Actualizar Información de Contacto")
            .setView(contenedorProgramatico)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNom = etNombreCompleto.text.toString().trim()
                val nuevoTel = etTelefono.text.toString().trim()
                val nuevoEmg = etEmergencia.text.toString().trim()

                if (nuevoNom.isBlank() || nuevoTel.isBlank()) {
                    Toast.makeText(ctx, "El nombre y el teléfono son obligatorios", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val mapaDatos = hashMapOf<String, Any>(
                    "nombreCompleto" to nuevoNom,
                    "telefono" to nuevoTel,
                    "contactoEmergencia" to nuevoEmg
                )

                db.collection("usuarios").document(miUid).update(mapaDatos)
                    .addOnSuccessListener {
                        Toast.makeText(ctx, "Ficha actualizada con éxito", Toast.LENGTH_SHORT).show()
                        if (miRol == "Paciente") {
                            val origenVista = arguments?.getString("ORIGEN")
                            if (origenVista == "CARD_CUIDADORES") {
                                renderizarDatosDelCuidadorDelPaciente(textViewActualizar)
                            } else {
                                renderizarDatosPersonalesCompletosPaciente(textViewActualizar)
                            }
                        } else {
                            renderizarDatosPropiosLocal(textViewActualizar)
                        }
                    }
            }
            .show()
    }

    private fun generarQr(contenido: String): Bitmap? {
        return try {
            BarcodeEncoder().encodeBitmap(contenido, BarcodeFormat.QR_CODE, 500, 500)
        } catch (e: Exception) { null }
    }
}