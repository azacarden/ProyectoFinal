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
import com.azahara.proyecto_final_azahara.model.Medicamento

sealed class MedicationListItem {
    data class Header(val titulo: String) : MedicationListItem()
    data class Item(val medicamento: Medicamento) : MedicationListItem()
}

// 1. Heredamos de ListAdapter y le pasamos nuestro MedicationDiffCallback
class MedicationAdapter(
    private val onBorrarClick: (Medicamento) -> Unit,
    private val onItemClick: (Medicamento) -> Unit,
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
        // getItem(position) es un método nativo de ListAdapter
        when (val elemento = getItem(position)) {
            is MedicationListItem.Header -> {
                val headerHolder = holder as HeaderViewHolder
                headerHolder.tvHeader.text = elemento.titulo
            }
            is MedicationListItem.Item -> {
                val itemHolder = holder as MedicationViewHolder
                val item = elemento.medicamento

                itemHolder.tvNombre.text = item.nombre
                itemHolder.tvMensaje.text = if (item.mensajePersonalizado.isBlank()) "Sin descripción" else item.mensajePersonalizado

                val detalleCalendario = when (item.frecuencia) {
                    "Semanal" -> " - Semanal (Cada ${item.diaEspecifico})"
                    "Mensual" -> " - Mensual (Los días ${item.diaEspecifico})"
                    else -> " - A diario"
                }
                itemHolder.tvHora.text = "Horas: ${item.horaToma}$detalleCalendario"

                if (!item.urlProspecto.isNullOrEmpty()) {
                    itemHolder.btnProspecto.visibility = View.VISIBLE
                    itemHolder.btnProspecto.setOnClickListener { onProspectoClick(item.urlProspecto) }
                } else {
                    itemHolder.btnProspecto.visibility = View.GONE
                }

                itemHolder.btnBorrar.setOnClickListener { onBorrarClick(item) }
                itemHolder.vistaTarjeta.setOnClickListener { onItemClick(item) }
            }
        }
    }

    // 2. Esta función sigue siendo tuya, pero ahora llama a submitList()
    fun actualizarDatos(medicamentos: List<Medicamento>) {
        val listaAgrupada = agruparYOrdenar(medicamentos)
        submitList(listaAgrupada)
    }

    private fun agruparYOrdenar(medicamentos: List<Medicamento>): List<MedicationListItem> {
        val listaFinal = mutableListOf<MedicationListItem>()

        val diarias = medicamentos.filter { it.frecuencia == "Diaria" }.sortedBy { it.nombre.lowercase() }
        val semanales = medicamentos.filter { it.frecuencia == "Semanal" }.sortedBy { it.nombre.lowercase() }
        val mensuales = medicamentos.filter { it.frecuencia == "Mensual" }.sortedBy { it.nombre.lowercase() }

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

// 3. El motor de las animaciones: Le enseñamos a Android a comparar
class MedicationDiffCallback : DiffUtil.ItemCallback<MedicationListItem>() {

    // ¿Es el mismo elemento lógico? (Comparamos IDs)
    override fun areItemsTheSame(oldItem: MedicationListItem, newItem: MedicationListItem): Boolean {
        return when {
            oldItem is MedicationListItem.Header && newItem is MedicationListItem.Header -> oldItem.titulo == newItem.titulo
            oldItem is MedicationListItem.Item && newItem is MedicationListItem.Item -> oldItem.medicamento.id == newItem.medicamento.id
            else -> false
        }
    }

    // ¿Ha cambiado visualmente algo en su contenido? (Comparamos el objeto)
    override fun areContentsTheSame(oldItem: MedicationListItem, newItem: MedicationListItem): Boolean {
        return oldItem == newItem
    }
}