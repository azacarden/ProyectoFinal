package com.azahara.proyecto_final_azahara.data.network

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class MedicamentosResponse(
    @SerializedName("resultados") val resultados: List<MedicamentoBasicoDto>?
)

data class MedicamentoBasicoDto(
    @SerializedName("nregistro") val nregistro: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("labtitular") val labtitular: String
)

data class SeccionDocDto(
    @SerializedName("seccion") val seccion: String,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("contenido") val contenido: String
)
data class FichaTecnicaResponse(
    @SerializedName("secciones") val secciones: List<SeccionDocDto>?
)

// --- INTERFAZ DE ENDPOINTS ---
interface CimaApi {
    @GET("medicamentos")
    suspend fun buscarMedicamentos(
        @Query("nombre") nombre: String
    ): MedicamentosResponse

    // Volvemos a la lista original, sin la caja
    @GET("docSegmentado/contenido/1")
    suspend fun getFichaTecnica(
        @Query("nregistro") nregistro: String
    ): List<SeccionDocDto>
}

// --- CLIENTE RETROFIT SINGLETON ---
object RetrofitClient {
    private const val BASE_URL = "https://cima.aemps.es/cima/rest/"

    // 1. Creamos un traductor Gson "relajado" que no se asuste con el formato del CIMA
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    val cimaApi: CimaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(CimaApi::class.java)
    }
}