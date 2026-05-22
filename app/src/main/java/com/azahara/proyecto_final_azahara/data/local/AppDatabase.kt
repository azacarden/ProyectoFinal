package com.azahara.proyecto_final_azahara.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.azahara.proyecto_final_azahara.model.AlarmaGeneral
import com.azahara.proyecto_final_azahara.model.CitaMedica
import com.azahara.proyecto_final_azahara.model.Historial
import com.azahara.proyecto_final_azahara.model.HorarioMedicamento
import com.azahara.proyecto_final_azahara.model.Medicamento
import com.azahara.proyecto_final_azahara.model.Usuario

@Database(
    entities = [
        Usuario::class,
        Medicamento::class,
        HorarioMedicamento::class, // <--- INTEGRADO: Entidad de horarios atómicos
        Historial::class,
        CitaMedica::class,
        AlarmaGeneral::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun medicamentoDao(): MedicamentoDao
    abstract fun historialDao(): HistorialDao
    abstract fun citaMedicaDao(): CitaMedicaDao
    abstract fun alarmaGeneralDao(): AlarmaGeneralDao

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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}