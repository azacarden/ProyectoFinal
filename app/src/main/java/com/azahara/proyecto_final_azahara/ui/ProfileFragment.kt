package com.azahara.proyecto_final_azahara.ui

import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.azahara.proyecto_final_azahara.R
import com.google.android.material.button.MaterialButton
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

    private var vinculacionListener: ListenerRegistration? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("SesionUsuario", android.content.Context.MODE_PRIVATE)
        miUsuario = prefs.getString("usuario_identificado", "Usuario_Local") ?: "Usuario_Local"
        miRol = prefs.getString("rol_usuario", "Paciente") ?: "Paciente"
        miUid = prefs.getString("firebase_uid", "") ?: ""

        val ivQrCode = view.findViewById<ImageView>(R.id.ivQrCode)
        val btnAccionDinamica = view.findViewById<MaterialButton>(R.id.btnEscanearQr)
        val btnCerrarSesion = view.findViewById<MaterialButton>(R.id.btnCerrarSesion)
        val tvTituloFragment = view.findViewById<TextView>(R.id.tvTituloPerfil)
        val tvMiRol = view.findViewById<TextView>(R.id.tvMiRol)

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

        // Obtenemos el color directamente desde tus recursos de colores de forma segura
        val colorMarca = ContextCompat.getColor(requireContext(), R.color.md_theme_light_primary)

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
                            val nombreEmergencia = doc.getString("nombreContactoEmergencia") ?: "No registrado"
                            val nss = doc.getString("nss") ?: "No registrado"

                            tvTituloFragment.text = "Expediente de Salud"

                            tvMiRol.text = formatearFichaElegante(colorMarca,
                                "Paciente:" to nombre,
                                "Correo electrónico:" to correo,
                                "Teléfono personal:" to telefono,
                                "Número de Seguridad Social:" to nss,
                                "Nombre del contacto de emergencia:" to nombreEmergencia,
                                "Teléfono del contacto de emergencia / Tutor:" to emergencia
                            )
                        }
                    }
            } else {
                view.findViewById<View>(R.id.cardQr)?.visibility = View.GONE
                btnCerrarSesion.visibility = View.VISIBLE
                tvTituloFragment.text = "Mi Cuenta (Cuidador)"

                btnAccionDinamica.apply {
                    visibility = View.VISIBLE
                    text = "Editar Mis Datos"
                    setIconResource(android.R.drawable.ic_menu_edit)
                    setOnClickListener { mostrarDialogoModificarDatos(tvMiRol) }
                }
                renderizarDatosPropiosLocal(tvMiRol, colorMarca)
            }
        } else {
            if (origenVista == "CARD_CUIDADORES") {
                view.findViewById<View>(R.id.cardQr)?.visibility = View.VISIBLE
                ivQrCode.setImageBitmap(generarQr(miUid))
                btnAccionDinamica.visibility = View.GONE
                btnCerrarSesion.visibility = View.GONE

                tvTituloFragment.text = "Mi Cuidador Asignado"
                renderizarDatosDelCuidadorDelPaciente(tvMiRol, colorMarca)
            } else {
                view.findViewById<View>(R.id.cardQr)?.visibility = View.GONE
                btnCerrarSesion.visibility = View.VISIBLE
                tvTituloFragment.text = "Mi Perfil"

                btnAccionDinamica.apply {
                    visibility = View.VISIBLE
                    text = "Editar Mis Datos"
                    setIconResource(R.drawable.edit)
                    setOnClickListener { mostrarDialogoModificarDatos(tvMiRol) }
                }
                renderizarDatosPersonalesCompletosPaciente(tvMiRol, colorMarca)
            }
        }
    }

    private fun formatearFichaElegante(colorResaltado: Int, vararg lineas: Pair<String, String>): CharSequence {
        val builder = SpannableStringBuilder()
        lineas.forEachIndexed { index, par ->
            val inicio = builder.length
            builder.append(par.first)

            builder.setSpan(StyleSpan(Typeface.BOLD), inicio, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(ForegroundColorSpan(colorResaltado), inicio, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            builder.append("  ").append(par.second)
            if (index < lineas.size - 1) {
                builder.append("\n\n")
            }
        }
        return builder
    }

    private fun renderizarDatosPropiosLocal(textView: TextView, color: Int) {
        db.collection("usuarios").document(miUid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val nombreCompleto = doc.getString("nombreCompleto") ?: "No registrado"
                val tel = doc.getString("telefono") ?: "No registrado"

                textView.text = formatearFichaElegante(color,
                    "Nombre completo:" to nombreCompleto,
                    "Cuenta de usuario:" to miUsuario,
                    "Teléfono de contacto:" to tel
                )
            }
        }
    }

    private fun renderizarDatosPersonalesCompletosPaciente(textView: TextView, color: Int) {
        db.collection("usuarios").document(miUid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val nombreComp = doc.getString("nombreCompleto") ?: "No registrado"
                val tel = doc.getString("telefono") ?: "No registrado"
                val emg = doc.getString("contactoEmergencia") ?: "No registrado"
                val nss = doc.getString("nss") ?: "No registrado"
                val nombreEmergencia = doc.getString("nombreContactoEmergencia") ?: "No registrado"

                textView.text = formatearFichaElegante(color,
                    "Nombre completo:" to nombreComp,
                    "Cuenta de usuario:" to miUsuario,
                    "Teléfono personal:" to tel,
                    "Número de Seguridad Social:" to nss,
                    "Nombre del contacto de emergencia:" to nombreEmergencia,
                    "Teléfono del contacto de emergencia / Tutor:" to emg
                )
            }
        }
    }

    private fun renderizarDatosDelCuidadorDelPaciente(textView: TextView, color: Int) {
        vinculacionListener = db.collection("vinculaciones")
            .whereEqualTo("pacienteUid", miUid)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null || snapshots.isEmpty) {
                    textView.text = formatearFichaElegante(color,
                        "Estado:" to "Sin cuidador vinculado actualmente.",
                        "Información:" to "Muestra el código QR de arriba a tu cuidador o tutor legal para enlazar las cuentas de forma segura."
                    )
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

                        textView.text = formatearFichaElegante(color,
                            "Cuidador asignado:" to nombreC,
                            "Teléfono de contacto:" to telC,
                            "Correo electrónico:" to correoC,
                            "Información:" to "Puedes añadir a más acompañantes de confianza compartiendo este mismo código QR."
                        )
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

        val etNss = EditText(ctx).apply {
            hint = "Número de la Seguridad Social"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            layoutParams = lp
            if (miRol == "Cuidador") visibility = View.GONE
        }

        val etNombreEmergencia = EditText(ctx).apply {
            hint = "Nombre del contacto de emergencia / Tutor"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
            layoutParams = lp
            if (miRol == "Cuidador") visibility = View.GONE
        }

        val etEmergencia = EditText(ctx).apply {
            hint = "Teléfono de contacto de emergencia / Tutor"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            layoutParams = lp
            if (miRol == "Cuidador") visibility = View.GONE
        }

        contenedorProgramatico.addView(etNombreCompleto)
        contenedorProgramatico.addView(etTelefono)
        contenedorProgramatico.addView(etNss)
        contenedorProgramatico.addView(etNombreEmergencia)
        contenedorProgramatico.addView(etEmergencia)

        db.collection("usuarios").document(miUid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                etNombreCompleto.setText(doc.getString("nombreCompleto") ?: "")
                etTelefono.setText(doc.getString("telefono") ?: "")
                etNss.setText(doc.getString("nss") ?: "")
                etNombreEmergencia.setText(doc.getString("nombreContactoEmergencia") ?: "")
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
                val nombreEmergencia = etNombreEmergencia.text.toString().trim()
                val nuevoEmg = etEmergencia.text.toString().trim()

                if (nuevoNom.isBlank() || nuevoTel.isBlank()) {
                    Toast.makeText(ctx, "El nombre y el teléfono son obligatorios", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val mapaDatos = hashMapOf<String, Any>(
                    "nombreCompleto" to nuevoNom,
                    "telefono" to nuevoTel,
                    "nss" to nuevoNss,
                    "nombreContactoEmergencia" to nombreEmergencia,
                    "contactoEmergencia" to nuevoEmg
                )

                db.collection("usuarios").document(miUid).update(mapaDatos)
                    .addOnSuccessListener {
                        Toast.makeText(ctx, "Ficha actualizada con éxito", Toast.LENGTH_SHORT).show()
                        val colorMarca = ContextCompat.getColor(requireContext(), R.color.md_theme_light_primary)
                        if (miRol == "Paciente") {
                            val origenVista = arguments?.getString("ORIGEN")
                            if (origenVista == "CARD_CUIDADORES") {
                                renderizarDatosDelCuidadorDelPaciente(textViewActualizar, colorMarca)
                            } else {
                                renderizarDatosPersonalesCompletosPaciente(textViewActualizar, colorMarca)
                            }
                        } else {
                            renderizarDatosPropiosLocal(textViewActualizar, colorMarca)
                        }
                    }
            }
            .show()
    }

    override fun onDestroyView() {
        vinculacionListener?.remove()
        super.onDestroyView()
    }

    private fun generarQr(contenido: String): Bitmap? {
        return try {
            BarcodeEncoder().encodeBitmap(contenido, BarcodeFormat.QR_CODE, 500, 500)
        } catch (e: Exception) { null }
    }
}