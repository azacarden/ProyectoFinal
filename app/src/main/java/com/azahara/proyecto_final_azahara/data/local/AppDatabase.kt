package com.azahara.proyecto_final_azahara.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.azahara.proyecto_final_azahara.model.Historial
import com.azahara.proyecto_final_azahara.model.Medicamento
import com.azahara.proyecto_final_azahara.model.Usuario

/**
 * Base de Datos Central de la Aplicación
 * Agrupa las entidades y expone los DAOs
 */
@Database(
    entities = [Usuario::class, Medicamento::class, Historial::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Exponemos los DAOs construidos en la Tarea 5
    abstract fun usuarioDao(): UsuarioDao
    abstract fun medicamentoDao(): MedicamentoDao
    abstract fun historialDao(): HistorialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Patrón Singleton para asegurar que solo exista una instancia
         * de la base de datos en toda la aplicación.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pastillero_db" // Nombre del archivo de la base de datos
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}