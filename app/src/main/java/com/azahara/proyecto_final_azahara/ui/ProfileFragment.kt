package com.azahara.proyecto_final_azahara.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.azahara.proyecto_final_azahara.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val db = FirebaseFirestore.getInstance()
    private val miUid = FirebaseAuth.getInstance().currentUser?.uid ?: "USUARIO_PRUEBA_123"

    // 1. Preparamos el "Lanzador" de la cámara
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        } else {
            // ¡Hemos leído un código QR!
            val pacienteUidScaneado = result.contents
            vincularEnLaNube(pacienteUidScaneado)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivQrCode = view.findViewById<ImageView>(R.id.ivQrCode)
        val btnEscanear = view.findViewById<Button>(R.id.btnEscanearQr)

        // 2. Generamos el QR de nuestra propia cuenta al abrir la pantalla
        val miQrBitmap = generarQr(miUid)
        if (miQrBitmap != null) {
            ivQrCode.setImageBitmap(miQrBitmap)
        } else {
            Toast.makeText(requireContext(), "Error al crear tu QR", Toast.LENGTH_SHORT).show()
        }

        // 3. Botón para encender la cámara
        btnEscanear.setOnClickListener {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Enfoca el código QR del Paciente")
                setCameraId(0) // Usa la cámara trasera
                setBeepEnabled(true) // Pitido al escanear
                setBarcodeImageEnabled(false)
            }
            barcodeLauncher.launch(options)
        }
    }

    // Función mágica que convierte texto (UID) en una imagen cuadrada (QR)
    private fun generarQr(contenido: String): Bitmap? {
        return try {
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.encodeBitmap(contenido, BarcodeFormat.QR_CODE, 500, 500)
        } catch (e: Exception) {
            null
        }
    }

    // Conexión con Firestore para guardar la relación Cuidador-Paciente
    private fun vincularEnLaNube(pacienteUid: String) {
        val vinculacion = hashMapOf(
            "cuidadorId" to miUid,
            "pacienteId" to pacienteUid,
            "fechaVinculacion" to System.currentTimeMillis()
        )

        db.collection("vinculaciones")
            .add(vinculacion)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "¡Paciente vinculado con éxito!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error de red al vincular", Toast.LENGTH_SHORT).show()
            }
    }
}