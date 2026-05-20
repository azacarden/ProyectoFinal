package com.azahara.proyecto_final_azahara.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.azahara.proyecto_final_azahara.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val db = FirebaseFirestore.getInstance()

    private var miUsuario = "Usuario_Anónimo"
    private var miRol = "Paciente" // Por defecto

    // Lanzador de la cámara para el escaneo QR
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(context ?: requireContext(), "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        } else {
            val pacienteUidScaneado = result.contents
            vincularEnLaNube(pacienteUidScaneado)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. LEER LA SESIÓN
        val prefs = requireContext().getSharedPreferences("SesionUsuario", android.content.Context.MODE_PRIVATE)
        miUsuario = prefs.getString("usuario_identificado", "Usuario_Local") ?: "Usuario_Local"
        miRol = prefs.getString("rol_usuario", "Paciente") ?: "Paciente"

        // Buscamos las vistas en el XML
        val ivQrCode = view.findViewById<ImageView>(R.id.ivQrCode)
        val btnEscanear = view.findViewById<Button>(R.id.btnEscanearQr)
        val tvTituloFragment = view.findViewById<TextView>(R.id.tvTituloPerfil)

        // ¡NUEVO! Enlazamos el TextView del rol que se nos había olvidado
        val tvMiRol = view.findViewById<TextView>(R.id.tvMiRol)

        if (ivQrCode == null || btnEscanear == null) {
            Toast.makeText(requireContext(), "Error en IDs", Toast.LENGTH_LONG).show()
            return
        }

        // 2. ADAPTAR LA INTERFAZ SEGÚN EL ROL REAL
        if (miRol == "Cuidador") {
            tvTituloFragment?.text = "Perfil: Cuidador ($miUsuario)"

            // Actualizamos el texto para que diga Cuidador
            tvMiRol?.text = "Rol: Cuidador"

            ivQrCode.visibility = View.GONE
            btnEscanear.visibility = View.VISIBLE
            btnEscanear.text = "Escanear QR del Paciente"

        } else {
            tvTituloFragment?.text = "Perfil: Paciente ($miUsuario)"

            // ¡NUEVO! Actualizamos el texto para que diga Paciente
            tvMiRol?.text = "Rol: Paciente"

            ivQrCode.visibility = View.VISIBLE
            val miQrBitmap = generarQr(miUsuario)
            if (miQrBitmap != null) {
                ivQrCode.setImageBitmap(miQrBitmap)
            }
            btnEscanear.visibility = View.GONE
        }

        // ... resto de tu código (configuración del botón cámara) ...
    }

    // Generador de QR
    private fun generarQr(contenido: String): Bitmap? {
        return try {
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.encodeBitmap(contenido, BarcodeFormat.QR_CODE, 500, 500)
        } catch (e: Exception) {
            null
        }
    }

    // Guardar la vinculación en Firebase Firestore
    private fun vincularEnLaNube(pacienteUid: String) {
        val vinculacion = hashMapOf(
            "cuidadorId" to miUsuario,
            "pacienteId" to pacienteUid,
            "fechaVinculacion" to System.currentTimeMillis()
        )

        db.collection("vinculaciones")
            .add(vinculacion)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "¡Paciente $pacienteUid vinculado con éxito!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error de red al vincular en Firebase", Toast.LENGTH_SHORT).show()
            }
    }
}