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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.azahara.proyecto_final_azahara.R
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val db = FirebaseFirestore.getInstance()
    private var miUsuarioNombre = "Usuario"
    private var miRol = "Paciente"
    private var miUid = ""

    // Estado del paciente seleccionado en sesión de Cuidador
    private var pacienteSeleccionadoUid: String? = null
    private var pacienteSeleccionadoNombre: String? = null

    // Vistas del Layout
    private lateinit var tvSaludo: TextView
    private lateinit var btnVolverPacientes: Button
    private lateinit var llCuidadorHub: LinearLayout
    private lateinit var svDashboardSalud: View
    private lateinit var rvPacientes: RecyclerView

    // Elementos mutables del botón Cuidadores
    private lateinit var ivIconoCuidadores: ImageView
    private lateinit var tvTituloCuidadores: TextView

    // Receptor del Escáner QR
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        } else {
            vincularNuevoPaciente(result.contents)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Cargar preferencias del usuario logueado
        val prefs = requireContext().getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE)
        miUsuarioNombre = prefs.getString("usuario_identificado", "Usuario") ?: "Usuario"
        miRol = prefs.getString("rol_usuario", "Paciente") ?: "Paciente"
        miUid = prefs.getString("firebase_uid", "") ?: ""

        // 2. Vincular elementos visuales
        tvSaludo = view.findViewById(R.id.tvSaludoDashboard)
        btnVolverPacientes = view.findViewById(R.id.btnVolverPacientes)
        llCuidadorHub = view.findViewById(R.id.llCuidadorHub)
        svDashboardSalud = view.findViewById(R.id.svDashboardSalud)
        rvPacientes = view.findViewById(R.id.rvPacientesDashboard)

        ivIconoCuidadores = view.findViewById(R.id.ivIconoCuidadores)
        tvTituloCuidadores = view.findViewById(R.id.tvTituloCuidadores)

        // 3. Configurar Acción de Escaneo
        view.findViewById<MaterialCardView>(R.id.cardEscanearPaciente).setOnClickListener {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Enfoca el código QR del Paciente")
                setBeepEnabled(true)
            }
            barcodeLauncher.launch(options)
        }

        // 4. Configurar Botón "Volver" para el cuidador
        btnVolverPacientes.setOnClickListener {
            pacienteSeleccionadoUid = null
            pacienteSeleccionadoNombre = null
            evaluarLayoutPorEstados()
        }

        // 5. Configuración de clics en las tarjetas de salud con inyección de IDs dinámicos
        view.findViewById<MaterialCardView>(R.id.cardMedicacion).setOnClickListener {
            val bundle = Bundle().apply { putString("PACIENTE_UID", obtenerUidDestino()) }
            findNavController().navigate(R.id.action_dashboard_to_medicationList, bundle)
        }

        view.findViewById<MaterialCardView>(R.id.cardAdd).setOnClickListener {
            val bundle = Bundle().apply { putString("PACIENTE_UID", obtenerUidDestino()) }
            findNavController().navigate(R.id.action_dashboard_to_addMedication, bundle)
        }

        view.findViewById<MaterialCardView>(R.id.cardHistorial).setOnClickListener {
            val bundle = Bundle().apply { putString("PACIENTE_UID", obtenerUidDestino()) }
            findNavController().navigate(R.id.action_dashboard_to_appointmentHistory, bundle)
        }

        view.findViewById<MaterialCardView>(R.id.cardNuevaCita).setOnClickListener {
            val bundle = Bundle().apply { putString("PACIENTE_UID", obtenerUidDestino()) }
            findNavController().navigate(R.id.action_dashboard_to_addAppointment, bundle)
        }

        view.findViewById<MaterialCardView>(R.id.cardCitas).setOnClickListener {
            val bundle = Bundle().apply { putString("PACIENTE_UID", obtenerUidDestino()) }
            findNavController().navigate(R.id.action_dashboard_to_appointmentList, bundle)
        }

        view.findViewById<MaterialCardView>(R.id.cardCuidadores).setOnClickListener {
            // Navega al Perfil (Allí implementaremos la visualización de datos personales)
            val bundle = Bundle().apply { putString("PACIENTE_UID", obtenerUidDestino()) }
            findNavController().navigate(R.id.action_dashboard_to_profile, bundle)
        }

        // 6. Lanzar renderizado inicial
        evaluarLayoutPorEstados()
    }

    /**
     * Motor de renderizado dinámico según el Rol y la selección de pacientes
     */
    private fun evaluarLayoutPorEstados() {
        if (miRol == "Cuidador") {
            if (pacienteSeleccionadoUid == null) {
                // Modo A: Panel de Selección de pacientes
                tvSaludo.text = "¡Hola, $miUsuarioNombre!\nGestión de Cuidados"
                llCuidadorHub.visibility = View.VISIBLE
                svDashboardSalud.visibility = View.GONE
                btnVolverPacientes.visibility = View.GONE
                cargarListaPacientes()
            } else {
                // Modo B: Monitorizando a un paciente seleccionado
                tvSaludo.text = "Monitorizando a:\n$pacienteSeleccionadoNombre"
                llCuidadorHub.visibility = View.GONE
                svDashboardSalud.visibility = View.VISIBLE
                btnVolverPacientes.visibility = View.VISIBLE

                // UX: Mutamos el botón Cuidadores porque es el Cuidador quien mira
                ivIconoCuidadores.setImageResource(android.R.drawable.ic_menu_info_details)
                tvTituloCuidadores.text = "Info Paciente"
            }
        } else {
            // Modo C: Flujo natural del Paciente de toda la vida
            tvSaludo.text = "¡Hola, $miUsuarioNombre!\n¿Qué deseas hacer hoy?"
            llCuidadorHub.visibility = View.GONE
            svDashboardSalud.visibility = View.VISIBLE
            btnVolverPacientes.visibility = View.GONE

            // Reestablecemos valores nativos del Paciente
            ivIconoCuidadores.setImageResource(android.R.drawable.ic_menu_share)
            tvTituloCuidadores.text = "Cuidadores"
        }
    }

    private fun obtenerUidDestino(): String {
        // Si soy cuidador devuelvo el ID del abuelo/paciente; si soy paciente devuelvo mi propio UID
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
                    // Al pulsar sobre un paciente, mutamos la pantalla
                    pacienteSeleccionadoUid = uid
                    pacienteSeleccionadoNombre = nombre
                    evaluarLayoutPorEstados()
                }
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
                            // Auto-seleccionamos al paciente recién escaneado para mejorar la UX
                            pacienteSeleccionadoUid = pacienteUid
                            pacienteSeleccionadoNombre = nombrePaciente
                            evaluarLayoutPorEstados()
                        }
                } else {
                    Toast.makeText(requireContext(), "El QR escaneado no pertenece a un usuario válido", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Adaptador interno optimizado para renderizar los pacientes en el Dashboard
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
        }
        override fun getItemCount() = pacientes.size
    }
}