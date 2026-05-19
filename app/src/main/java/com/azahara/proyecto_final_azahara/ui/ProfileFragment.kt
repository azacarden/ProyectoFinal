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

        // 1. LEER LA SESIÓN: Recuperamos quién está usando la app de verdad
        val prefs = requireContext().getSharedPreferences("SesionUsuario", android.content.Context.MODE_PRIVATE)
        miUsuario = prefs.getString("usuario_identificado", "Usuario_Local") ?: "Usuario_Local"
        miRol = prefs.getString("rol_usuario", "Paciente") ?: "Paciente"

        // Buscamos las vistas en el XML
        val ivQrCode = view.findViewById<ImageView>(R.id.ivQrCode)
        val btnEscanear = view.findViewById<Button>(R.id.btnEscanearQr)

        // Buscamos si tienes algún TextView para el título o subtítulo (opcional)
        // Si tu XML tiene un TextView para el título, puedes buscarlo aquí para cambiar el texto
        val tvTituloFragment = view.findViewById<TextView>(R.id.tvTituloPerfil)

        // ESCUDO PROTECTOR: Validamos que las vistas principales existan
        if (ivQrCode == null || btnEscanear == null) {
            Toast.makeText(
                requireContext(),
                "Error: Los IDs 'ivQrCode' o 'btnEscanearQr' no coinciden con tu XML",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // 2. ADAPTAR LA INTERFAZ SEGÚN EL ROL REAL
        if (miRol == "Cuidador") {
            // Si eres CUIDADOR: Tu misión es escanear al paciente
            tvTituloFragment?.text = "Perfil: Cuidador ($miUsuario)"

            // Los cuidadores no necesitan mostrar un QR, se lo ocultamos para que no confunda
            ivQrCode.visibility = View.GONE

            // Mostramos el botón de escanear bien visible
            btnEscanear.visibility = View.VISIBLE
            btnEscanear.text = "Escanear QR del Paciente"

        } else {
            // Si eres PACIENTE: Tu misión es mostrar tu QR para que te escaneen
            tvTituloFragment?.text = "Perfil: Paciente ($miUsuario)"

            // Mostramos el QR generado con tu nombre de usuario real de Room
            ivQrCode.visibility = View.VISIBLE
            val miQrBitmap = generarQr(miUsuario)
            if (miQrBitmap != null) {
                ivQrCode.setImageBitmap(miQrBitmap)
            }

            // Un paciente no necesita el botón de escanear, se lo ocultamos
            btnEscanear.visibility = View.GONE
        }

        // 3. Configuración del botón de la cámara (solo lo usará el Cuidador)
        btnEscanear.setOnClickListener {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Enfoca el código QR del Paciente para vincularlo")
                setCameraId(0)
                setBeepEnabled(true)
                setBarcodeImageEnabled(false)
            }
            barcodeLauncher.launch(options)
        }
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