package com.azahara.proyecto_final_azahara.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.azahara.proyecto_final_azahara.data.local.Medicamento
import java.util.Calendar

class AlarmHelper(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun programarAlarma(medicamento: Medicamento) {
        medicamento.horaToma.forEachIndexed { index, horaTexto ->
            val partes = horaTexto.trim().split(":")
            if (partes.size == 2) {
                val hora = partes[0].toIntOrNull() ?: return@forEachIndexed
                val minuto = partes[1].toIntOrNull() ?: return@forEachIndexed

                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("TIPO_ALARMA", "MEDICAMENTO")
                    putExtra("MED_NOMBRE", medicamento.nombre)
                    putExtra("MED_MENSAJE", medicamento.mensajePersonalizado)
                    putExtra("MED_ID", medicamento.id)
                }

                val requestCode = (medicamento.id.hashCode() * 31) + index

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hora)
                    set(Calendar.MINUTE, minuto)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (before(Calendar.getInstance())) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            }
        }
    }

    fun cancelarAlarma(medicamento: Medicamento) {
        medicamento.horaToma.forEachIndexed { index, _ ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val requestCode = (medicamento.id.hashCode() * 31) + index
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}