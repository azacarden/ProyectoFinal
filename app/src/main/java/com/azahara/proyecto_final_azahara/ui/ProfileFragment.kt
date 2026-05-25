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
    private var miUid = "" // <- NUEVO

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        } else {
            // El resultado del QR ahora es el UID alfanumérico del paciente
            verificarYVincularPaciente(result.contents)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("SesionUsuario", android.content.Context.MODE_PRIVATE)
        miUsuario = prefs.getString("usuario_identificado", "Usuario_Local") ?: "Usuario_Local"
        miRol = prefs.getString("rol_usuario", "Paciente") ?: "Paciente"
        miUid = prefs.getString("firebase_uid", "") ?: "" // <- Extraemos el UID seguro

        val ivQrCode = view.findViewById<ImageView>(R.id.ivQrCode)
        val btnEscanear = view.findViewById<Button>(R.id.btnEscanearQr)
        val tvTituloFragment = view.findViewById<TextView>(R.id.tvTituloAddGeneral) // Ajustado al ID real de tu XML para el título
        val tvMiRol = view.findViewById<TextView>(R.id.tvMiRol)

        val btnCerrarSesion = view.findViewById<Button>(R.id.btnCerrarSesion)
        val tvTituloPacientes = view.findViewById<TextView>(R.id.tvTituloPacientes)
        val rvPacientes = view.findViewById<RecyclerView>(R.id.rvPacientes)

        // LÓGICA DE CERRAR SESIÓN
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

        // CONFIGURACIÓN DE LOS TEXTOS
        if (tvTituloFragment != null) {
            tvTituloFragment.text = "Perfil de $miUsuario"
        }
        tvMiRol.text = "Rol: $miRol"

        // ADAPTAR LA MÁSCARA VISUAL SEGÚN EL ROL
        if (miRol == "Cuidador") {
            view.findViewById<View>(R.id.cardQr)?.visibility = View.GONE
            btnEscanear.visibility = View.VISIBLE

            tvTituloPacientes.visibility = View.VISIBLE
            rvPacientes.visibility = View.VISIBLE
            cargarPacientesDelCuidador(rvPacientes)
        } else {
            view.findViewById<View>(R.id.cardQr)?.visibility = View.VISIBLE
            ivQrCode.setImageBitmap(generarQr(miUid)) // <- El QR ahora expone el UID alfanumérico seguro
            btnEscanear.visibility = View.GONE
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
        // CORREGIDO: Buscamos las vinculaciones usando tu UID alfanumérico de Cuidador
        db.collection("vinculaciones")
            .whereEqualTo("cuidadorUid", miUid)
            .get()
            .addOnSuccessListener { documents ->
                // Mapeamos guardando tanto el ID como el Nombre para el listado
                val datosPacientes = documents.map { doc ->
                    Pair(
                        doc.getString("pacienteUid") ?: "Desconocido",
                        doc.getString("pacienteNombre") ?: "Paciente Anónimo"
                    )
                }.distinctBy { it.first }

                rv.layoutManager = LinearLayoutManager(requireContext())
                rv.adapter = PacientesAdapter(datosPacientes) { pacienteUid ->
                    // Al pulsar, viajamos a la lista de medicación enviándole el UID seguro del paciente
                    val bundle = Bundle().apply { putString("PACIENTE_UID", pacienteUid) }
                    findNavController().navigate(R.id.medicationListFragment, bundle)
                }
            }
    }

    // Comprobamos en Firestore que el QR escaneado pertenece a un usuario real antes de vincular
    private fun verificarYVincularPaciente(pacienteUid: String) {
        db.collection("usuarios").document(pacienteUid).get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    val nombrePaciente = documento.getString("nombreUsuario") ?: "Paciente"

                    // Creamos el mapa relacional perfecto utilizando los UIDs de ambos
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
                            // Refrescamos la pantalla
                            findNavController().navigate(R.id.profileFragment)
                        }
                } else {
                    Toast.makeText(requireContext(), "Error: El código QR no pertenece a un paciente válido", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error de conexión al verificar el código", Toast.LENGTH_SHORT).show()
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
            holder.tvNombre.text = nombre // Pintamos el nombre legible del paciente
            holder.itemView.setOnClickListener { onClick(uid) } // Al pulsar, enviamos su UID al fragmento destino
        }
        override fun getItemCount() = pacientes.size
    }

    private fun generarQr(contenido: String): Bitmap? {
        return try {
            BarcodeEncoder().encodeBitmap(contenido, BarcodeFormat.QR_CODE, 500, 500)
        } catch (e: Exception) { null }
    }
}