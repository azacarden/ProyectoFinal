package com.azahara.proyecto_final_azahara.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.azahara.proyecto_final_azahara.R
import com.azahara.proyecto_final_azahara.model.CitaMedica
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppointmentAdapter(
    private var lista: List<CitaMedica> = emptyList(),
    private val onBorrarClick: (CitaMedica) -> Unit,
    private val onItemClick: (CitaMedica) -> Unit // <-- NUEVO: Para editar
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEspecialista: TextView = view.findViewById(R.id.tvEspecialistaItem)
        val tvMotivo: TextView = view.findViewById(R.id.tvMotivoItem)
        val tvFechaHora: TextView = view.findViewById(R.id.tvFechaHoraItem)
        val btnBorrar: ImageButton = view.findViewById(R.id.btnBorrarCita)
        val vistaTarjeta: View = view // Capturamos la tarjeta entera
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val item = lista[position]

        holder.tvEspecialista.text = "${item.medico} - ${item.especialidad}"
        holder.tvMotivo.text = item.motivo

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.tvFechaHora.text = sdf.format(Date(item.fechaHora))

        holder.btnBorrar.setOnClickListener {
            onBorrarClick(item)
        }

        holder.vistaTarjeta.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarDatos(nuevaLista: List<CitaMedica>) {
        this.lista = nuevaLista
        notifyDataSetChanged()
    }
}