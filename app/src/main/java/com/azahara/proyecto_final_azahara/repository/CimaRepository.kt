package com.azahara.proyecto_final_azahara.repository

import com.azahara.proyecto_final_azahara.data.network.CimaApi
import com.azahara.proyecto_final_azahara.data.network.MedicamentoBasicoDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class MedicamentoDetalle(
    val nregistro: String,
    val nombre: String,
    val indicaciones: String?,
    val posologia: String?
)

class CimaRepository(private val api: CimaApi) {

    suspend fun buscarPorNombre(query: String): List<MedicamentoBasicoDto> {
        return withContext(Dispatchers.IO) {
            api.buscarMedicamentos(query).resultados ?: emptyList()
        }
    }

    suspend fun obtenerDetalleCompleto(nregistro: String, nombre: String): MedicamentoDetalle {
        return withContext(Dispatchers.IO) {
            try {
                // Intentamos pedir la lista de secciones al CIMA (¡El formato Array correcto!)
                val secciones = api.getFichaTecnica(nregistro)

                val indicaciones = secciones.find { it.seccion == "4.1" }?.contenido
                val posologia = secciones.find { it.seccion == "4.2" }?.contenido

                MedicamentoDetalle(
                    nregistro = nregistro,
                    nombre = nombre,
                    indicaciones = indicaciones,
                    posologia = posologia
                )
            } catch (e: Exception) {
                // Si la API no tiene la ficha y nos manda un error o un formato loco,
                // lo atrapamos en silencio. Devolvemos el detalle vacío para que
                // la pantalla NO muestre error y el usuario pueda rellenarlo a mano.
                MedicamentoDetalle(
                    nregistro = nregistro,
                    nombre = nombre,
                    indicaciones = null,
                    posologia = null
                )
            }
        }
    }
}