package com.azahara.proyecto_final_azahara.ui

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
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
    private val onItemClick: (CitaMedica) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMotivo: TextView = view.findViewById(R.id.tvMotivoItem)
        val tvMedico: TextView = view.findViewById(R.id.tvMedicoItem)
        val tvEspecialidad: TextView = view.findViewById(R.id.tvEspecialidadItem)
        val tvHospital: TextView = view.findViewById(R.id.tvHospitalItem)
        val tvNotas: TextView = view.findViewById(R.id.tvNotasCitaItem)
        val tvFechaHora: TextView = view.findViewById(R.id.tvFechaHoraItem)
        val btnBorrar: ImageButton = view.findViewById(R.id.btnBorrarCita)
        val vistaTarjeta: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val item = lista[position]

        // Título principal con autoría
        holder.tvMotivo.text = "${item.motivo} (${item.creadoPorNombre})"

        // Rellena los campos aplicando negrita limpia solo al prefijo
        holder.tvMedico.text = darFormatoEtiqueta("Médico:", item.medico)
        holder.tvEspecialidad.text = darFormatoEtiqueta("Especialidad:", item.especialidad)
        holder.tvHospital.text = darFormatoEtiqueta("Hospital/Centro:", item.centroHospital)

        // Manejo dinámico de las observaciones
        if (item.notas.isBlank()) {
            holder.tvNotas.visibility = View.GONE
            holder.tvNotas.text = darFormatoEtiqueta("Notas:", item.notas)
        } else {
            holder.tvNotas.visibility = View.VISIBLE
            holder.tvNotas.text = darFormatoEtiqueta("Notas:", item.notas)
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", Locale.getDefault())
        holder.tvFechaHora.text = sdf.format(Date(item.fechaHora))

        holder.btnBorrar.setOnClickListener {
            onBorrarClick(item)
        }

        holder.vistaTarjeta.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = lista.size

    private fun darFormatoEtiqueta(etiqueta: String, valor: String): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append(etiqueta)
        builder.setSpan(StyleSpan(Typeface.BOLD), 0, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append("  ").append(valor)
        return builder
    }

    fun actualizarDatos(nuevaLista: List<CitaMedica>) {
        this.lista = nuevaLista
        notifyDataSetChanged()
    }
}