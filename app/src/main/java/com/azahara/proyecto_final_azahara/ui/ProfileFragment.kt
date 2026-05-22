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
    private var miUsuario = "Usuario_Anónimo"
    private var miRol = "Paciente"

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        } else {
            vincularEnLaNube(result.contents)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("SesionUsuario", android.content.Context.MODE_PRIVATE)
        miUsuario = prefs.getString("usuario_identificado", "Usuario_Local") ?: "Usuario_Local"
        miRol = prefs.getString("rol_usuario", "Paciente") ?: "Paciente"

        val ivQrCode = view.findViewById<ImageView>(R.id.ivQrCode)
        val btnEscanear = view.findViewById<Button>(R.id.btnEscanearQr)
        val tvTituloFragment = view.findViewById<TextView>(R.id.tvTituloPerfil)
        val tvMiRol = view.findViewById<TextView>(R.id.tvMiRol)

        val btnCerrarSesion = view.findViewById<Button>(R.id.btnCerrarSesion)
        val tvTituloPacientes = view.findViewById<TextView>(R.id.tvTituloPacientes)
        val rvPacientes = view.findViewById<RecyclerView>(R.id.rvPacientes)

        // 1. LÓGICA DE CERRAR SESIÓN
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

        // 2. CONFIGURACIÓN DE LOS TEXTOS SOLICITADOS (Paso global para ambos roles)
        tvTituloFragment.text = "Perfil de $miUsuario"
        tvMiRol.text = "Rol: $miRol"

        // 3. ADAPTAR LA MÁSCARA VISUAL SEGÚN EL ROL
        if (miRol == "Cuidador") {
            view.findViewById<View>(R.id.cardQr)?.visibility = View.GONE
            btnEscanear.visibility = View.VISIBLE

            tvTituloPacientes.visibility = View.VISIBLE
            rvPacientes.visibility = View.VISIBLE
            cargarPacientesDelCuidador(rvPacientes)

        } else {
            view.findViewById<View>(R.id.cardQr)?.visibility = View.VISIBLE
            ivQrCode.setImageBitmap(generarQr(miUsuario))
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
        db.collection("vinculaciones")
            .whereEqualTo("cuidadorId", miUsuario)
            .get()
            .addOnSuccessListener { documents ->
                val pacientes = documents.map { it.getString("pacienteId") ?: "Desconocido" }.distinct()

                rv.layoutManager = LinearLayoutManager(requireContext())
                rv.adapter = PacientesAdapter(pacientes) { pacienteUid ->
                    val bundle = Bundle().apply { putString("PACIENTE_UID", pacienteUid) }
                    findNavController().navigate(R.id.medicationListFragment, bundle)
                }
            }
    }

    inner class PacientesAdapter(private val pacientes: List<String>, private val onClick: (String) -> Unit) : RecyclerView.Adapter<PacientesAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvNombre: TextView = view.findViewById(R.id.tvNombrePacienteItem)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_paciente, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val paciente = pacientes[position]
            holder.tvNombre.text = paciente
            holder.itemView.setOnClickListener { onClick(paciente) }
        }
        override fun getItemCount() = pacientes.size
    }

    private fun generarQr(contenido: String): Bitmap? {
        return try {
            BarcodeEncoder().encodeBitmap(contenido, BarcodeFormat.QR_CODE, 500, 500)
        } catch (e: Exception) { null }
    }

    private fun vincularEnLaNube(pacienteUid: String) {
        val vinculacion = hashMapOf("cuidadorId" to miUsuario, "pacienteId" to pacienteUid, "fechaVinculacion" to System.currentTimeMillis())
        db.collection("vinculaciones").add(vinculacion)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "¡Paciente vinculado con éxito!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.profileFragment)
            }
    }
}