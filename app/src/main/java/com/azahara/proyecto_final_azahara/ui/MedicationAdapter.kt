package com.azahara.proyecto_final_azahara.ui

import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.azahara.proyecto_final_azahara.R
import com.azahara.proyecto_final_azahara.model.Medicamento

class MedicationAdapter(
    private var lista: List<Medicamento> = emptyList(),
    private val onBorrarClick: (Medicamento) -> Unit,
    private val onItemClick: (Medicamento) -> Unit,
    private val onProspectoClick: (String) -> Unit // <-- NUEVA ACCIÓN
) : RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>() {

    class MedicationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreMed)
        val tvHora: TextView = view.findViewById(R.id.tvHoraMed)
        val tvMensaje: TextView = view.findViewById(R.id.tvMensajeMed)
        val btnBorrar: ImageButton = view.findViewById(R.id.btnBorrarMedicamento)
        val btnProspecto: Button = view.findViewById(R.id.btnVerProspectoItem) // Nuevo botón
        val vistaTarjeta: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medicamento, parent, false)
        return MedicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        val item = lista[position]
        holder.tvNombre.text = item.nombre
        holder.tvHora.text = "Horas de toma: ${item.horaToma}"
        holder.tvMensaje.text = if (item.mensajePersonalizado.isBlank()) "Sin descripción" else item.mensajePersonalizado

        // Si guardamos un prospecto válido, mostramos el botón en la lista
        if (!item.urlProspecto.isNullOrEmpty()) {
            holder.btnProspecto.visibility = View.VISIBLE
            holder.btnProspecto.setOnClickListener {
                onProspectoClick(item.urlProspecto)
            }
        } else {
            holder.btnProspecto.visibility = View.GONE
        }

        holder.btnBorrar.setOnClickListener {
            onBorrarClick(item)
        }

        holder.vistaTarjeta.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarDatos(nuevaLista: List<Medicamento>) {
        this.lista = nuevaLista
        notifyDataSetChanged()
    }
}