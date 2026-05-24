package com.azahara.proyecto_final_azahara.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.azahara.proyecto_final_azahara.R
import com.azahara.proyecto_final_azahara.model.MedicamentoConHorarios

sealed class MedicationListItem {
    data class Header(val titulo: String) : MedicationListItem()
    // 1. Actualizamos el ítem para que reciba la nueva estructura compuesta
    data class Item(val medicamentoWrapper: MedicamentoConHorarios) : MedicationListItem()
}

class MedicationAdapter(
    private val onBorrarClick: (MedicamentoConHorarios) -> Unit,
    private val onItemClick: (MedicamentoConHorarios) -> Unit,
    private val onProspectoClick: (String) -> Unit
) : ListAdapter<MedicationListItem, RecyclerView.ViewHolder>(MedicationDiffCallback()) {

    companion object {
        private const val TIPO_HEADER = 0
        private const val TIPO_ITEM = 1
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHeader: TextView = view.findViewById(R.id.tvHeaderFrecuencia)
    }

    class MedicationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreMed)
        val tvHora: TextView = view.findViewById(R.id.tvHoraMed)
        val tvMensaje: TextView = view.findViewById(R.id.tvMensajeMed)
        val btnBorrar: ImageButton = view.findViewById(R.id.btnBorrarMedicamento)
        val btnProspecto: Button = view.findViewById(R.id.btnVerProspectoItem)
        val vistaTarjeta: View = view
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is MedicationListItem.Header -> TIPO_HEADER
            is MedicationListItem.Item -> TIPO_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TIPO_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header_frecuencia, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medicamento, parent, false)
            MedicationViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val elemento = getItem(position)) {
            is MedicationListItem.Header -> {
                val headerHolder = holder as HeaderViewHolder
                headerHolder.tvHeader.text = elemento.titulo
            }
            is MedicationListItem.Item -> {
                val itemHolder = holder as MedicationViewHolder

                // 2. Desempaquetamos la entidad base y sus horarios
                val wrapper = elemento.medicamentoWrapper
                val med = wrapper.medicamento
                val listaHoras = wrapper.horarios

                itemHolder.tvNombre.text = med.nombre
                itemHolder.tvMensaje.text = if (med.mensajePersonalizado.isBlank()) "Sin descripción" else med.mensajePersonalizado

                // 3. Unimos los objetos HorarioMedicamento en un solo String visual
                val horasTexto = listaHoras.joinToString(", ") { it.horaToma }

                // Puesto que ya no tienes diaEspecifico, mostramos solo la frecuencia general
                val detalleCalendario = when (med.frecuencia) {
                    "Semanal" -> " - Semanal"
                    "Mensual" -> " - Mensual"
                    else -> " - A diario"
                }

                itemHolder.tvHora.text = "Horas: $horasTexto$detalleCalendario"

                if (!med.urlProspecto.isNullOrEmpty()) {
                    itemHolder.btnProspecto.visibility = View.VISIBLE
                    itemHolder.btnProspecto.setOnClickListener { onProspectoClick(med.urlProspecto) }
                } else {
                    itemHolder.btnProspecto.visibility = View.GONE
                }

                itemHolder.btnBorrar.setOnClickListener { onBorrarClick(wrapper) }
                itemHolder.vistaTarjeta.setOnClickListener { onItemClick(wrapper) }
            }
        }
    }

    fun actualizarDatos(medicamentos: List<MedicamentoConHorarios>) {
        val listaAgrupada = agruparYOrdenar(medicamentos)
        submitList(listaAgrupada)
    }

    private fun agruparYOrdenar(medicamentos: List<MedicamentoConHorarios>): List<MedicationListItem> {
        val listaFinal = mutableListOf<MedicationListItem>()

        val diarias = medicamentos.filter { it.medicamento.frecuencia == "Diaria" }.sortedBy { it.medicamento.nombre.lowercase() }
        val semanales = medicamentos.filter { it.medicamento.frecuencia == "Semanal" }.sortedBy { it.medicamento.nombre.lowercase() }
        val mensuales = medicamentos.filter { it.medicamento.frecuencia == "Mensual" }.sortedBy { it.medicamento.nombre.lowercase() }

        if (diarias.isNotEmpty()) {
            listaFinal.add(MedicationListItem.Header("📅️ Tomas Diarias"))
            listaFinal.addAll(diarias.map { MedicationListItem.Item(it) })
        }
        if (semanales.isNotEmpty()) {
            listaFinal.add(MedicationListItem.Header("📅 Tomas Semanales"))
            listaFinal.addAll(semanales.map { MedicationListItem.Item(it) })
        }
        if (mensuales.isNotEmpty()) {
            listaFinal.add(MedicationListItem.Header("📅 Tomas Mensuales"))
            listaFinal.addAll(mensuales.map { MedicationListItem.Item(it) })
        }

        return listaFinal
    }
}

class MedicationDiffCallback : DiffUtil.ItemCallback<MedicationListItem>() {
    override fun areItemsTheSame(oldItem: MedicationListItem, newItem: MedicationListItem): Boolean {
        return when {
            oldItem is MedicationListItem.Header && newItem is MedicationListItem.Header -> oldItem.titulo == newItem.titulo
            oldItem is MedicationListItem.Item && newItem is MedicationListItem.Item ->
                oldItem.medicamentoWrapper.medicamento.id == newItem.medicamentoWrapper.medicamento.id
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: MedicationListItem, newItem: MedicationListItem): Boolean {
        return oldItem == newItem
    }
}