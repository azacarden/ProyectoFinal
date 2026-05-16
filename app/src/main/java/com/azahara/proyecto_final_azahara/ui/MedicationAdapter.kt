package com.azahara.proyecto_final_azahara.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.azahara.proyecto_final_azahara.R
import com.azahara.proyecto_final_azahara.model.Medicamento

class MedicationAdapter(private var lista: List<Medicamento> = emptyList()) :
    RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>() {

    class MedicationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreMed)
        val tvHora: TextView = view.findViewById(R.id.tvHoraMed)
        val tvMensaje: TextView = view.findViewById(R.id.tvMensajeMed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medicamento, parent, false)
        return MedicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        val item = lista[position]
        holder.tvNombre.text = item.nombre
        holder.tvHora.text = item.horaToma
        holder.tvMensaje.text = item.mensajePersonalizado
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarDatos(nuevaLista: List<Medicamento>) {
        this.lista = nuevaLista
        notifyDataSetChanged()
    }
}