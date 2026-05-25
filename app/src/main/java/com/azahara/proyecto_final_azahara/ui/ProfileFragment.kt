package com.azahara.proyecto_final_azahara.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.azahara.proyecto_final_azahara.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val db = FirebaseFirestore.getInstance()
    private var miUsuario = "Usuario_Local"
    private var miRol = "Paciente"
    private var miUid = ""

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        } else {
            verificarYVincularPaciente(result.contents)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("SesionUsuario", android.content.Context.MODE_PRIVATE)
        miUsuario = prefs.getString("usuario_identificado", "Usuario_Local") ?: "Usuario_Local"
        miRol = prefs.getString("rol_usuario", "Paciente") ?: "Paciente"
        miUid = prefs.getString("firebase_uid", "") ?: ""

        val ivQrCode = view.findViewById<ImageView>(R.id.ivQrCode)
        val btnEscanear = view.findViewById<Button>(R.id.btnEscanearQr)
        val tvTituloFragment = view.findViewById<TextView>(R.id.tvTituloPerfil)
        val tvMiRol = view.findViewById<TextView>(R.id.tvMiRol)

        val btnCerrarSesion = view.findViewById<Button>(R.id.btnCerrarSesion)
        val tvTituloPacientes = view.findViewById<TextView>(R.id.tvTituloPacientes)
        val rvPacientes = view.findViewById<RecyclerView>(R.id.rvPacientes)

        // LEER ARGUMENTO DE MONITORIZACIÓN DE CUIDADOR
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

        // CONTROL DE RENDERIZADO TRIPLE DE INTERFAZ (UX MEJORADA)
        if (miRol == "Cuidador") {
            if (targetPacienteUid != null && targetPacienteUid != miUid) {
                // MODO A: Cuidador inspeccionando la información personal del paciente seleccionado
                view.findViewById<View>(R.id.cardQr)?.visibility = View.GONE
                btnEscanear.visibility = View.GONE
                tvTituloPacientes.visibility = View.GONE
                rvPacientes.visibility = View.GONE
                btnCerrarSesion.visibility = View.GONE // Ocultamos cerrar sesión aquí para no confundir

                db.collection("usuarios").document(targetPacienteUid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val nombre = doc.getString("nombreUsuario") ?: "Desconocido"
                            val correo = doc.getString("correo") ?: "No aportado"

                            tvTituloFragment.text = "Información del Paciente"
                            tvMiRol.text = "Nombre completo: $nombre\n\nContacto: $correo\n\nEstado: Vinculado correctamente"
                        }
                    }
            } else {
                // MODO B: Cuidador en su menú de perfil propio general (Hub de escaneo)
                view.findViewById<View>(R.id.cardQr)?.visibility = View.GONE
                btnEscanear.visibility = View.VISIBLE
                tvTituloPacientes.visibility = View.VISIBLE
                rvPacientes.visibility = View.VISIBLE
                btnCerrarSesion.visibility = View.VISIBLE

                tvTituloFragment.text = "Perfil de $miUsuario"
                tvMiRol.text = "Rol: $miRol"
                cargarPacientesDelCuidador(rvPacientes)
            }
        } else {
            // MODO C: Paciente nativo viendo su propio código QR de vinculación
            view.findViewById<View>(R.id.cardQr)?.visibility = View.VISIBLE
            ivQrCode.setImageBitmap(generarQr(miUid))
            btnEscanear.visibility = View.GONE
            tvTituloPacientes.visibility = View.GONE
            rvPacientes.visibility = View.GONE
            btnCerrarSesion.visibility = View.VISIBLE

            tvTituloFragment.text = "Perfil de $miUsuario"
            tvMiRol.text = "Rol: $miRol"
        }

        btnEscanear.setOnClickListener {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Enfoca el código QR del Paciente")
                setBeepEnabled(true)
            }
            barcodeLauncher.launch(options)
        }
    }

    private fun cargarPacientesDelCuidador(rv: RecyclerView) {
        db.collection("vinculaciones")
            .whereEqualTo("cuidadorUid", miUid)
            .get()
            .addOnSuccessListener { documents ->
                val datosPacientes = documents.map { doc ->
                    Pair(
                        doc.getString("pacienteUid") ?: "",
                        doc.getString("pacienteNombre") ?: "Paciente Anónimo"
                    )
                }.filter { it.first.isNotEmpty() }.distinctBy { it.first }

                rv.layoutManager = LinearLayoutManager(requireContext())
                rv.adapter = PacientesAdapter(datosPacientes) { pacienteUid ->
                    val bundle = Bundle().apply { putString("PACIENTE_UID", pacienteUid) }
                    findNavController().navigate(R.id.medicationListFragment, bundle)
                }
            }
    }

    private fun verificarYVincularPaciente(pacienteUid: String) {
        db.collection("usuarios").document(pacienteUid).get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    val nombrePaciente = documento.getString("nombreUsuario") ?: "Paciente"

                    val vinculacion = hashMapOf(
                        "cuidadorUid" to miUid,
                        "cuidadorNombre" to miUsuario,
                        "pacienteUid" to pacienteUid,
                        "pacienteNombre" to nombrePaciente,
                        "fechaVinculacion" to System.currentTimeMillis()
                    )

                    db.collection("vinculaciones").add(vinculacion)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "¡Vinculado con con éxito a $nombrePaciente!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.profileFragment)
                        }
                } else {
                    Toast.makeText(requireContext(), "Error: Código no válido", Toast.LENGTH_LONG).show()
                }
            }
    }

    inner class PacientesAdapter(
        private val pacientes: List<Pair<String, String>>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<PacientesAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvNombre: TextView = view.findViewById(R.id.tvNombrePacienteItem)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_paciente, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val (uid, nombre) = pacientes[position]
            holder.tvNombre.text = nombre
            holder.itemView.setOnClickListener { onClick(uid) }
        }
        override fun getItemCount() = pacientes.size
    }

    private fun generarQr(contenido: String): Bitmap? {
        return try {
            BarcodeEncoder().encodeBitmap(contenido, BarcodeFormat.QR_CODE, 500, 500)
        } catch (e: Exception) { null }
    }
}