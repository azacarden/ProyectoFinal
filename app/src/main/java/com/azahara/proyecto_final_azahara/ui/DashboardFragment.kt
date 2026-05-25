package com.azahara.proyecto_final_azahara.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.azahara.proyecto_final_azahara.R
import com.azahara.proyecto_final_azahara.data.local.AppDatabase
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val db = FirebaseFirestore.getInstance()
    private var miUsuarioNombre = "Usuario"
    private var miRol = "Paciente"
    private var miUid = ""

    private var pacienteSeleccionadoUid: String? = null
    private var pacienteSeleccionadoNombre: String? = null

    private var avisoNotificacionesMostrado = false

    private lateinit var tvSaludo: TextView
    private lateinit var btnVolverPacientes: Button
    private lateinit var llCuidadorHub: LinearLayout
    private lateinit var svDashboardSalud: View
    private lateinit var rvPacientes: RecyclerView

    private lateinit var ivIconoCuidadores: ImageView
    private lateinit var tvTituloCuidadores: TextView

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        } else {
            vincularNuevoPaciente(result.contents)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE)
        miUsuarioNombre = prefs.getString("usuario_identificado", "Usuario") ?: "Usuario"
        miRol = prefs.getString("rol_usuario", "Paciente") ?: "Paciente"
        miUid = prefs.getString("firebase_uid", "") ?: ""

        tvSaludo = view.findViewById(R.id.tvSaludoDashboard)
        btnVolverPacientes = view.findViewById(R.id.btnVolverPacientes)
        llCuidadorHub = view.findViewById(R.id.llCuidadorHub)
        svDashboardSalud = view.findViewById(R.id.svDashboardSalud)
        rvPacientes = view.findViewById(R.id.rvPacientesDashboard)

        ivIconoCuidadores = view.findViewById(R.id.ivIconoCuidadores)
        tvTituloCuidadores = view.findViewById(R.id.tvTituloCuidadores)

        view.findViewById<MaterialCardView>(R.id.cardEscanearPaciente).setOnClickListener {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Escanéa el código QR del Paciente")
                setBeepEnabled(true)
            }
            barcodeLauncher.launch(options)
        }

        btnVolverPacientes.setOnClickListener {
            pacienteSeleccionadoUid = null
            pacienteSeleccionadoNombre = null
            evaluarLayoutPorEstados()
        }

        // Aseguramos que el QR es de un paciente
        view.findViewById<MaterialCardView>(R.id.cardMedicacion).setOnClickListener {
            val bundle = Bundle().apply {
                putString("PACIENTE_UID", obtenerUidDestino())
                putString("PACIENTE_NOMBRE", if (miRol == "Cuidador") pacienteSeleccionadoNombre else miUsuarioNombre)
            }
            findNavController().navigate(R.id.action_dashboard_to_medicationList, bundle)
        }

        view.findViewById<MaterialCardView>(R.id.cardAdd).setOnClickListener {
            val bundle = Bundle().apply {
                putString("PACIENTE_UID", obtenerUidDestino())
                putString("PACIENTE_NOMBRE", if (miRol == "Cuidador") pacienteSeleccionadoNombre else miUsuarioNombre)
            }
            findNavController().navigate(R.id.action_dashboard_to_addMedication, bundle)
        }

        view.findViewById<MaterialCardView>(R.id.cardHistorial).setOnClickListener {
            val bundle = Bundle().apply {
                putString("PACIENTE_UID", obtenerUidDestino())
                putString("PACIENTE_NOMBRE", if (miRol == "Cuidador") pacienteSeleccionadoNombre else miUsuarioNombre)
            }
            findNavController().navigate(R.id.action_dashboard_to_appointmentHistory, bundle)
        }

        view.findViewById<MaterialCardView>(R.id.cardNuevaCita).setOnClickListener {
            val bundle = Bundle().apply {
                putString("PACIENTE_UID", obtenerUidDestino())
                putString("PACIENTE_NOMBRE", if (miRol == "Cuidador") pacienteSeleccionadoNombre else miUsuarioNombre)
            }
            findNavController().navigate(R.id.action_dashboard_to_addAppointment, bundle)
        }

        view.findViewById<MaterialCardView>(R.id.cardCitas).setOnClickListener {
            val bundle = Bundle().apply {
                putString("PACIENTE_UID", obtenerUidDestino())
                putString("PACIENTE_NOMBRE", if (miRol == "Cuidador") pacienteSeleccionadoNombre else miUsuarioNombre)
            }
            findNavController().navigate(R.id.action_dashboard_to_appointmentList, bundle)
        }

        view.findViewById<MaterialCardView>(R.id.cardCuidadores).setOnClickListener {
            val bundle = Bundle().apply {
                putString("PACIENTE_UID", obtenerUidDestino())
                if (miRol == "Paciente") {
                    putString("ORIGEN", "CARD_CUIDADORES")
                }
            }
            findNavController().navigate(R.id.action_dashboard_to_profile, bundle)
        }

        evaluarLayoutPorEstados()
    }

    private fun evaluarLayoutPorEstados() {
        if (miRol == "Cuidador") {
            if (pacienteSeleccionadoUid == null) {
                tvSaludo.text = "¡Hola, $miUsuarioNombre!\nGestión de Cuidados"
                llCuidadorHub.visibility = View.VISIBLE
                svDashboardSalud.visibility = View.GONE
                btnVolverPacientes.visibility = View.GONE
                cargarListaPacientes()
            } else {
                tvSaludo.text = "Monitorizando a:\n$pacienteSeleccionadoNombre"
                llCuidadorHub.visibility = View.GONE
                svDashboardSalud.visibility = View.VISIBLE
                btnVolverPacientes.visibility = View.VISIBLE

                ivIconoCuidadores.setImageResource(android.R.drawable.ic_menu_info_details)
                tvTituloCuidadores.text = "Info de paciente"

                comprobarTareasPendientesDeHoy()
            }
        } else {
            tvSaludo.text = "¡Hola, $miUsuarioNombre!\n¿Qué deseas hacer hoy?"
            llCuidadorHub.visibility = View.GONE
            svDashboardSalud.visibility = View.VISIBLE
            btnVolverPacientes.visibility = View.GONE

            ivIconoCuidadores.setImageResource(android.R.drawable.ic_menu_share)
            tvTituloCuidadores.text = "Cuidadores"

            comprobarTareasPendientesDeHoy()
        }
    }

    // Lanza una Notificación Push en el panel de Android
    private fun comprobarTareasPendientesDeHoy() {
        if (avisoNotificacionesMostrado) return

        lifecycleScope.launch {
            val dbLocal = AppDatabase.getDatabase(requireContext())

            val tieneMedicinas = withContext(Dispatchers.IO) {
                dbLocal.medicamentoDao().obtenerTodosLosMedicamentosConHorariosSync().isNotEmpty()
            }

            if (tieneMedicinas) {
                avisoNotificacionesMostrado = true
                val nombreSujeto = if (miRol == "Cuidador") pacienteSeleccionadoNombre else "tu planificación"

                val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val channel = android.app.NotificationChannel(
                        "canal_general",
                        "Avisos Generales",
                        android.app.NotificationManager.IMPORTANCE_DEFAULT
                    )
                    notificationManager.createNotificationChannel(channel)
                }

                val notification = androidx.core.app.NotificationCompat.Builder(requireContext(), "canal_general")
                    .setSmallIcon(android.R.drawable.ic_menu_agenda)
                    .setContentTitle("🔔 Control Diario")
                    .setContentText("Tienes tareas médicas pendientes hoy para $nombreSujeto.")
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(999, notification) // Lanzamos el aviso al panel
            }
        }
    }

    private fun obtenerUidDestino(): String {
        return if (miRol == "Cuidador") pacienteSeleccionadoUid ?: miUid else miUid
    }

    private fun cargarListaPacientes() {
        db.collection("vinculaciones")
            .whereEqualTo("cuidadorUid", miUid)
            .get()
            .addOnSuccessListener { documents ->
                val listaPacientes = documents.map { doc ->
                    Pair(
                        doc.getString("pacienteUid") ?: "",
                        doc.getString("pacienteNombre") ?: "Paciente Anónimo"
                    )
                }.filter { it.first.isNotEmpty() }.distinctBy { it.first }

                rvPacientes.layoutManager = LinearLayoutManager(requireContext())
                rvPacientes.adapter = PacientesDashboardAdapter(listaPacientes) { uid, nombre ->
                    pacienteSeleccionadoUid = uid
                    pacienteSeleccionadoNombre = nombre
                    evaluarLayoutPorEstados()
                }
            }
    }

    private fun desvincularPacienteDeLaNube(pacienteUid: String, pacienteNombre: String) {
        db.collection("vinculaciones")
            .whereEqualTo("cuidadorUid", miUid)
            .whereEqualTo("pacienteUid", pacienteUid)
            .get()
            .addOnSuccessListener { snapshots ->
                for (documento in snapshots.documents) {
                    documento.reference.delete()
                }
                Toast.makeText(requireContext(), "$pacienteNombre desvinculado con éxito", Toast.LENGTH_SHORT).show()
                cargarListaPacientes()
            }
    }

    private fun vincularNuevoPaciente(pacienteUid: String) {
        db.collection("usuarios").document(pacienteUid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nombrePaciente = doc.getString("nombreUsuario") ?: "Paciente"

                    val vinculacion = hashMapOf(
                        "cuidadorUid" to miUid,
                        "cuidadorNombre" to miUsuarioNombre,
                        "pacienteUid" to pacienteUid,
                        "pacienteNombre" to nombrePaciente,
                        "fechaVinculacion" to System.currentTimeMillis()
                    )

                    db.collection("vinculaciones").add(vinculacion)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "¡Vinculado con éxito a $nombrePaciente!", Toast.LENGTH_SHORT).show()
                            pacienteSeleccionadoUid = pacienteUid
                            pacienteSeleccionadoNombre = nombrePaciente
                            evaluarLayoutPorEstados()
                        }
                } else {
                    Toast.makeText(requireContext(), "El QR escaneado no pertenece a un usuario válido", Toast.LENGTH_LONG).show()
                }
            }
    }

    inner class PacientesDashboardAdapter(
        private val pacientes: List<Pair<String, String>>,
        private val onSelect: (String, String) -> Unit
    ) : RecyclerView.Adapter<PacientesDashboardAdapter.ViewHolder>() {

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
            holder.itemView.setOnClickListener { onSelect(uid, nombre) }

            holder.itemView.setOnLongClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("⚠️ Desvincular Paciente")
                    .setMessage("¿Estás seguro que quieres desvincularte de $nombre?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Sí, desvincular") { _, _ ->
                        desvincularPacienteDeLaNube(uid, nombre)
                    }
                    .show()
                true
            }
        }
        override fun getItemCount() = pacientes.size
    }
}