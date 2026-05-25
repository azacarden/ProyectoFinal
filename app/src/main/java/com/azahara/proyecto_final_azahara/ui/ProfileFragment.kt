package com.azahara.proyecto_final_azahara.ui

import android.graphics.Bitmap
import android.os.Bundle
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
        val btnAccionDinamica = view.findViewById<Button>(R.id.btnEscanearQr) // Reutilizamos este botón libre del XML
        val tvTituloFragment = view.findViewById<TextView>(R.id.tvTituloPerfil)
        val tvMiRol = view.findViewById<TextView>(R.id.tvMiRol)
        val btnCerrarSesion = view.findViewById<Button>(R.id.btnCerrarSesion)

        // Ocultamos los componentes antiguos que ya controla el Dashboard
        view.findViewById<TextView>(R.id.tvTituloPacientes)?.visibility = View.GONE
        view.findViewById<RecyclerView>(R.id.rvPacientes)?.visibility = View.GONE

        val targetPacienteUid = arguments?.getString("PACIENTE_UID")

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

        // 🛠️ MOTOR DE GESTIÓN DE EXPEDIENTES DE DATOS CRUZADOS (UX AVANZADA)
        if (miRol == "Cuidador") {
            if (targetPacienteUid != null && targetPacienteUid != miUid) {
                // MODO A: Cuidador inspeccionando la ficha de contacto y urgencias del paciente
                view.findViewById<View>(R.id.cardQr)?.visibility = View.GONE
                btnAccionDinamica.visibility = View.GONE
                btnCerrarSesion.visibility = View.GONE

                db.collection("usuarios").document(targetPacienteUid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val nombre = doc.getString("nombreUsuario") ?: "Desconocido"
                            val correo = doc.getString("correo") ?: "No aportado"
                            val telefono = doc.getString("telefono") ?: "No registrado"
                            val emergencia = doc.getString("contactoEmergencia") ?: "No registrado"

                            tvTituloFragment.text = "Expediente de: $nombre"
                            tvMiRol.text = "📧 Correo: $correo\n\n📞 Teléfono Personal: $telefono\n\n🚨 Contacto de Emergencia: $emergencia"
                        }
                    }
            } else {
                // MODO B: Perfil personal del propio Cuidador. Reutilizamos el botón para que edite sus datos
                view.findViewById<View>(R.id.cardQr)?.visibility = View.GONE
                btnCerrarSesion.visibility = View.VISIBLE

                tvTituloFragment.text = "Mi Cuenta"
                btnAccionDinamica.apply {
                    visibility = View.VISIBLE
                    text = "Editar Mis Datos de Contacto"
                    setOnClickListener { mostrarDialogoModificarDatos(tvMiRol) }
                }
                renderizarDatosPropiosLocal(tvMiRol)
            }
        } else {
            // MODO C: Perfil del Paciente. Ve su QR, puede editar sus datos Y VE A SU CUIDADOR A CARGO
            view.findViewById<View>(R.id.cardQr)?.visibility = View.VISIBLE
            ivQrCode.setImageBitmap(generarQr(miUid))
            btnCerrarSesion.visibility = View.VISIBLE

            tvTituloFragment.text = "Mi Perfil de Salud"
            btnAccionDinamica.apply {
                visibility = View.VISIBLE
                text = "Editar Mis Datos de Contacto"
                setOnClickListener { mostrarDialogoModificarDatos(tvMiRol) }
            }
            renderizarDatosPacienteYSuCuidador(tvMiRol)
        }
    }

    private fun renderizarDatosPropiosLocal(textView: TextView) {
        db.collection("usuarios").document(miUid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val tel = doc.getString("telefono") ?: "No registrado"
                val emg = doc.getString("contactoEmergencia") ?: "No registrado"
                textView.text = "Rol: $miRol\n\n📞 Mi Teléfono: $tel\n🚨 Contacto de Urgencia: $emg"
            }
        }
    }

    // 🛠️ ¡NUEVA LÓGICA! El paciente ve sus datos y abajo se descarga la ficha de quién le cuida
    private fun renderizarDatosPacienteYSuCuidador(textView: TextView) {
        db.collection("usuarios").document(miUid).get().addOnSuccessListener { docPersonal ->
            val miTel = docPersonal.getString("telefono") ?: "No registrado"
            val miEmg = docPersonal.getString("contactoEmergencia") ?: "No registrado"

            db.collection("vinculaciones").whereEqualTo("pacienteUid", miUid).get()
                .addOnSuccessListener { snapshots ->
                    if (!snapshots.isEmpty) {
                        val docVinculo = snapshots.documents.first()
                        val cuidadorUid = docVinculo.getString("cuidadorUid") ?: ""
                        val cuidadorNombre = docVinculo.getString("cuidadorNombre") ?: "Asignado"

                        db.collection("usuarios").document(cuidadorUid).get()
                            .addOnSuccessListener { docCuidador ->
                                val telCuidador = docCuidador.getString("telefono") ?: "No aportado"
                                textView.text = "📞 Mi Teléfono: $miTel\n🚨 Mi Contacto Urgencia: $miEmg\n\n" +
                                        "-----------------------------------------\n\n" +
                                        "👤 Mi Cuidador: $cuidadorNombre\n📞 Teléfono del Cuidador: $telCuidador"
                            }
                    } else {
                        textView.text = "📞 Mi Teléfono: $miTel\n🚨 Mi Contacto Urgencia: $miEmg\n\n" +
                                "-----------------------------------------\n\n" +
                                "⚠️ Estado: Sin cuidador vinculado todavía. Muestra tu código QR para asociarte."
                    }
                }
        }
    }

    // 🛠️ ¡NUEVA LÓGICA! Abre un formulario inflado por código para rellenar campos extra
    private fun mostrarDialogoModificarDatos(textViewActualizar: TextView) {
        val ctx = requireContext()
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val contenedorProgramatico = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }

        val etTelefono = EditText(ctx).apply {
            hint = "Introduce tu Teléfono Personal"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            layoutParams = lp
        }

        val etEmergencia = EditText(ctx).apply {
            hint = "Teléfono de Emergencia / Familiar"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            layoutParams = lp
        }

        contenedorProgramatico.addView(etTelefono)
        contenedorProgramatico.addView(etEmergencia)

        // Precarga de seguridad de datos anteriores
        db.collection("usuarios").document(miUid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                etTelefono.setText(doc.getString("telefono") ?: "")
                etEmergencia.setText(doc.getString("contactoEmergencia") ?: "")
            }
        }

        MaterialAlertDialogBuilder(ctx)
            .setTitle("Actualizar Información de Contacto")
            .setView(contenedorProgramatico)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar Cambios") { _, _ ->
                val nuevoTel = etTelefono.text.toString().trim()
                val nuevoEmg = etEmergencia.text.toString().trim()

                val mapaDatos = hashMapOf<String, Any>(
                    "telefono" to nuevoTel,
                    "contactoEmergencia" to nuevoEmg
                )

                db.collection("usuarios").document(miUid).update(mapaDatos)
                    .addOnSuccessListener {
                        Toast.makeText(ctx, "Información guardada correctamente", Toast.LENGTH_SHORT).show()
                        if (miRol == "Paciente") {
                            renderizarDatosPacienteYSuCuidador(textViewActualizar)
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