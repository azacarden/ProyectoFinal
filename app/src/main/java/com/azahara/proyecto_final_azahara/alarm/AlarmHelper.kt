package com.azahara.proyecto_final_azahara.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.azahara.proyecto_final_azahara.model.Medicamento
import java.util.*

class AlarmHelper(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Le decimos a Android Studio que nosotros nos hacemos cargo de la seguridad
    @SuppressLint("ScheduleExactAlarm")
    fun programarAlarma(medicamento: Medicamento) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("MED_NOMBRE", medicamento.nombre)
            putExtra("MED_MENSAJE", medicamento.mensajePersonalizado)
            putExtra("MED_ID", medicamento.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicamento.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val partes = medicamento.horaToma.split(":")
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, partes[0].toInt())
            set(Calendar.MINUTE, partes[1].toInt())
            set(Calendar.SECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }
        // Envolvemos la llamada en un try/catch de seguridad
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Si el usuario ha bloqueado las alarmas en los ajustes, capturamos el error
            // Aquí en el futuro podríamos enviar un aviso a la pantalla
            e.printStackTrace()
        }
    }
}