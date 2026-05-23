package com.azahara.proyecto_final_azahara.repository

import android.util.Log
import com.azahara.proyecto_final_azahara.data.network.CimaApi
import com.azahara.proyecto_final_azahara.data.network.MedicamentoBasicoDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// 1. Modificamos el modelo para reflejar lo que tú has pedido
data class MedicamentoDetalle(
    val nregistro: String,
    val nombre: String,
    val viasAdministracion: String?,
    val contraindicaciones: String?,
    val urlProspecto: String?
)

class CimaRepository(private val api: CimaApi) {

    suspend fun buscarPorNombre(query: String): List<MedicamentoBasicoDto> {
        return withContext(Dispatchers.IO) {
            api.buscarMedicamentos(query).resultados ?: emptyList()
        }
    }

    // Le pasamos el objeto entero para poder sacar sus vías y su prospecto
    suspend fun obtenerDetalleCompleto(medicamento: MedicamentoBasicoDto): MedicamentoDetalle {
        return withContext(Dispatchers.IO) {

            // Juntamos las vías (Ej: VÍA ORAL)
            val vias = medicamento.viasAdministracion?.joinToString(", ") { it.nombre }

            // Buscamos el documento tipo "2" (El prospecto) y sacamos su enlace web
            val prospectoUrl = medicamento.docs?.find { it.tipo == 2 }?.let { it.urlHtml ?: it.url }

            try {
                // Buscamos las contraindicaciones (Sección 4.3 de la AEMPS)
                val secciones = api.getFichaTecnica(medicamento.nregistro)
                val contraindicaciones = secciones.find { it.seccion == "4.3" }?.contenido
                if (contraindicaciones.isNullOrBlank()) {
                    Log.w("CIMA_API", "No se encontró sección 4.3 para: ${medicamento.nombre}")
                }

                MedicamentoDetalle(
                    nregistro = medicamento.nregistro,
                    nombre = medicamento.nombre,
                    viasAdministracion = vias,
                    urlProspecto = prospectoUrl,
                    contraindicaciones = contraindicaciones ?: "Información no disponible digitalmente."
                )
            } catch (e: Exception) {
                // Si la pastilla no tiene ficha técnica digitalizada, no rompemos la app,
                // simplemente devolvemos las vías y el prospecto, sin contraindicaciones
                MedicamentoDetalle(
                    nregistro = medicamento.nregistro,
                    nombre = medicamento.nombre,
                    viasAdministracion = vias,
                    contraindicaciones = null,
                    urlProspecto = prospectoUrl
                )
            }
        }
    }
}