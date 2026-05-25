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
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)

                    val medicamentosConHorarios = db.medicamentoDao().obtenerTodosLosMedicamentosConHorariosSync()
                    val alarmHelper = AlarmHelper(context)

                    for (wrapper in medicamentosConHorarios) {
                        alarmHelper.programarAlarma(wrapper)
                    }

                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}