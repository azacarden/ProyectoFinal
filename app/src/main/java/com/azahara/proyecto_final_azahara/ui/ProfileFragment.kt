package com.azahara.proyecto_final_azahara.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.text.Html
import android.view.View
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
import com.google.firebase.firestore.ListenerRegistration
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val db = FirebaseFirestore.getInstance()
    private var miUsuario = "Usuario_Local"
    private var miRol = "Paciente"
    private var miUid = ""

    // Registro para poder limpiar el listener en tiempo real de Firebase al salir del fragment
    private var vinculacionListener: ListenerRegistration? = null

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

        view.findViewById<TextView>(R.id.tvTituloPacientes)?.visibility = View.GONE
        view.findViewById<RecyclerView>(R.id.rvPacientes)?.visibility = View.GONE

        val targetPacienteUid = arguments?.getString("PACIENTE_UID")
        val origenVista = arguments?.getString("ORIGEN")

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


        // Aquí he usado html para el estilo de negrita del perfil

        if (miRol == "Cuidador") {
            if (targetPacienteUid != null && targetPacienteUid != miUid) {
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
                            val nss = doc.getString("nss") ?: "No registrado"

                            tvTituloFragment.text = "Expediente de Salud"

                            // Etiquetas en negrita y valores normales usando HTML
                            val htmlContenido = "<b>👤 Paciente:</b> $nombre<br><br>" +
                                    "<b>📧 Correo:</b> $correo<br><br>" +
                                    "<b>📞 Teléfono Personal:</b> $telefono<br><br>" +
                                    "<b>🪪 Nº Seguridad Social:</b> $nss<br><br>" +
                                    "<b>🚨 Emergencia / Tutor:</b> $emergencia"

                            tvMiRol.text = Html.fromHtml(htmlContenido, Html.FROM_HTML_MODE_LEGACY)
                        }
                    }
            } else {
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
            if (origenVista == "CARD_CUIDADORES") {
                view.findViewById<View>(R.id.cardQr)?.visibility = View.VISIBLE
                ivQrCode.setImageBitmap(generarQr(miUid))
                btnAccionDinamica.visibility = View.GONE
                btnCerrarSesion.visibility = View.GONE

                tvTituloFragment.text = "Mi Cuidador Asignado"
                renderizarDatosDelCuidadorDelPaciente(tvMiRol)
            } else {
                view.findViewById<View>(R.id.cardQr)?.visibility = View.GONE
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

                val html = "<b>👤 Nombre:</b> $nombreCompleto<br><br>" +
                        "<b>📧 Usuario:</b> $miUsuario<br><br>" +
                        "<b>📞 Teléfono:</b> $tel"
                textView.text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            }
        }
    }

    private fun renderizarDatosPersonalesCompletosPaciente(textView: TextView) {
        db.collection("usuarios").document(miUid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val nombreComp = doc.getString("nombreCompleto") ?: "No registrado"
                val tel = doc.getString("telefono") ?: "No registrado"
                val emg = doc.getString("contactoEmergencia") ?: "No registrado"
                val nss = doc.getString("nss") ?: "No registrado"

                val html = "<b>👤 Nombre Completo:</b> $nombreComp<br><br>" +
                        "<b>📧 Cuenta de Usuario:</b> $miUsuario<br><br>" +
                        "<b>📞 Mi Teléfono:</b> $tel<br><br>" +
                        "<b>🪪 Nº Seguridad Social:</b> $nss<br><br>" +
                        "<b>🚨 Contacto Urgencia / Tutor Legal:</b> $emg"
                textView.text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            }
        }
    }

    // Usam SnapshotListener para pintar al cuidador al instante
    private fun renderizarDatosDelCuidadorDelPaciente(textView: TextView) {
        vinculacionListener = db.collection("vinculaciones")
            .whereEqualTo("pacienteUid", miUid)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null || snapshots.isEmpty) {
                    val htmlVacio = "<b>⚠️ Estado:</b> Sin cuidador vinculado todavía.<br><br>" +
                            "👉 Muestra el código QR de arriba a tu cuidador o tutor legal para enlazar vuestras cuentas."
                    textView.text = Html.fromHtml(htmlVacio, Html.FROM_HTML_MODE_LEGACY)
                    return@addSnapshotListener
                }

                val docVinculo = snapshots.documents.first()
                val cuidadorUid = docVinculo.getString("cuidadorUid") ?: ""
                val cuidadorNombre = docVinculo.getString("cuidadorNombre") ?: "Asignado"

                db.collection("usuarios").document(cuidadorUid).get().addOnSuccessListener { docCuidador ->
                    if (docCuidador.exists()) {
                        val nombreC = docCuidador.getString("nombreCompleto") ?: docCuidador.getString("nombreUsuario") ?: cuidadorNombre
                        val telC = docCuidador.getString("telefono") ?: "No aportado"
                        val correoC = docCuidador.getString("correo") ?: "No aportado"

                        val htmlConCuidador = "<b>👤 Mi Cuidador:</b> $nombreC<br><br>" +
                                "<b>📞 Teléfono:</b> $telC<br><br>" +
                                "<b>📧 Correo:</b> $correoC<br><br>" +
                                "-----------------------------------------<br>" +
                                "<i>ℹ️ Puedes añadir a más cuidadores. Sólo deben escanear el QR.</i>"
                        textView.text = Html.fromHtml(htmlConCuidador, Html.FROM_HTML_MODE_LEGACY)
                    }
                }
            }
    }

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

        // Campo exclusivo para introducir el Número de la Seguridad Social (NSS)
        val etNss = EditText(ctx).apply {
            hint = "Número de la Seguridad Social"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            layoutParams = lp
            if (miRol == "Cuidador") visibility = View.GONE
        }

        val etEmergencia = EditText(ctx).apply {
            hint = "Persona de contacto de emergencia / Tutor"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
            layoutParams = lp
            if (miRol == "Cuidador") visibility = View.GONE
        }

        contenedorProgramatico.addView(etNombreCompleto)
        contenedorProgramatico.addView(etTelefono)
        contenedorProgramatico.addView(etNss)
        contenedorProgramatico.addView(etEmergencia)

        db.collection("usuarios").document(miUid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                etNombreCompleto.setText(doc.getString("nombreCompleto") ?: "")
                etTelefono.setText(doc.getString("telefono") ?: "")
                etNss.setText(doc.getString("nss") ?: "")
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
                val nuevoNss = etNss.text.toString().trim()
                val nuevoEmg = etEmergencia.text.toString().trim()

                if (nuevoNom.isBlank() || nuevoTel.isBlank()) {
                    Toast.makeText(ctx, "El nombre y el teléfono son obligatorios", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val mapaDatos = hashMapOf<String, Any>(
                    "nombreCompleto" to nuevoNom,
                    "telefono" to nuevoTel,
                    "nss" to nuevoNss, // Guardamos el NSS en Firebase
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

    override fun onDestroyView() {
        // Limpieza del listener en tiempo real al destruir la vista para evitar fugas de memoria
        vinculacionListener?.remove()
        super.onDestroyView()
    }

    private fun generarQr(contenido: String): Bitmap? {
        return try {
            BarcodeEncoder().encodeBitmap(contenido, BarcodeFormat.QR_CODE, 500, 500)
        } catch (e: Exception) { null }
    }
}