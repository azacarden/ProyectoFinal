package com.azahara.proyecto_final_azahara.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.azahara.proyecto_final_azahara.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Verificamos que el aviso sea realmente que el móvil se acaba de encender
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    // Obtenemos todos los medicamentos guardados
                    val medicamentos = db.medicamentoDao().obtenerTodosLosMedicamentosSync()
                    val alarmHelper = AlarmHelper(context)

                    // Volvemos a programar las alarmas una por una
                    for (medicamento in medicamentos) {
                        alarmHelper.programarAlarma(medicamento)
                    }

                    // Aquí también podrías hacer un bucle para reprogramar citas médicas
                    // val citas = db.citaMedicaDao().obtenerTodasLasCitasSync() ...

                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}