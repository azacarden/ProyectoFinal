package com.azahara.proyecto_final_azahara.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.azahara.proyecto_final_azahara.model.Historial
import com.azahara.proyecto_final_azahara.model.Medicamento
import com.azahara.proyecto_final_azahara.model.Usuario
import com.azahara.proyecto_final_azahara.model.CitaMedica
import com.azahara.proyecto_final_azahara.model.AlarmaGeneral // Nuevo import

/**
 * Base de Datos Central de la Aplicación (Versión 3)
 */
@Database(
    entities = [Usuario::class, Medicamento::class, Historial::class, CitaMedica::class, AlarmaGeneral::class],
    version = 3, // ¡SUBIMOS A VERSIÓN 3 POR LA NUEVA TABLA!
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun medicamentoDao(): MedicamentoDao
    abstract fun historialDao(): HistorialDao
    abstract fun citaMedicaDao(): CitaMedicaDao
    abstract fun alarmaGeneralDao(): AlarmaGeneralDao // Exponemos el nuevo DAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pastillero_db"
                )
                    .fallbackToDestructiveMigration() // Escudo protector para desarrollo
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}