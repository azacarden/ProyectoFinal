package com.azahara.proyecto_final_azahara.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.azahara.proyecto_final_azahara.model.CitaMedica
import com.azahara.proyecto_final_azahara.model.Medicamento
import com.azahara.proyecto_final_azahara.model.AlarmaGeneral
import java.util.*

class AlarmHelper(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun programarAlarma(medicamento: Medicamento) {
        // Cortamos el texto por las comas. Ej: "08:00, 16:00" -> Crea dos alarmas separadas
        val listaHoras = medicamento.horaToma.split(", ")

        listaHoras.forEachIndexed { index, horaTexto ->
            val partes = horaTexto.trim().split(":")

            if (partes.size == 2) { // Nos aseguramos de que el formato sea HH:mm
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("TIPO_ALARMA", "MEDICAMENTO")
                    putExtra("MED_NOMBRE", medicamento.nombre)
                    putExtra("MED_MENSAJE", medicamento.mensajePersonalizado)
                    putExtra("MED_ID", medicamento.id)
                }

                // Sumamos el index al ID para que cada hora tenga su propio canal y no se pisen
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (medicamento.id * 100) + index,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, partes[0].toInt())
                    set(Calendar.MINUTE, partes[1].toInt())
                    set(Calendar.SECOND, 0)

                    // Si la hora ya pasó hoy, la programamos para mañana
                    if (before(Calendar.getInstance())) {
                        add(Calendar.DATE, 1)
                    }
                }

                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
    }

    // -----------------------------------------------------
    // 2. ALARMAS DE CITAS MÉDICAS (¡Las que faltaban!)
    // -----------------------------------------------------
    @SuppressLint("ScheduleExactAlarm")
    fun programarAlarmaCita(cita: CitaMedica) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TIPO_ALARMA", "CITA")
            putExtra("CITA_MOTIVO", cita.motivo)
            putExtra("CITA_MEDICO", cita.medico)
            putExtra("CITA_ESPECIALIDAD", cita.especialidad)
            putExtra("CITA_NOTAS", cita.notas)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            cita.id + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val tiempoAviso = cita.fechaHora - (cita.recordatorioPrevio * 60 * 1000)
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                tiempoAviso,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancelarAlarmaCita(cita: CitaMedica) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TIPO_ALARMA", "CITA")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            cita.id + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    // -----------------------------------------------------
    // 3. ALARMAS GENERALES
    // -----------------------------------------------------
    @SuppressLint("ScheduleExactAlarm")
    fun programarAlarmaGeneral(alarma: AlarmaGeneral) {
        if (!alarma.activa) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TIPO_ALARMA", "GENERAL")
            putExtra("ALARMA_ID", alarma.id)
            putExtra("ALARMA_TITULO", alarma.titulo)
            putExtra("ALARMA_NOTAS", alarma.descripcion)
        }

        // Le sumamos 20000 al ID para crear un canal único
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarma.id + 20000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarma.fechaHora,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancelarAlarmaGeneral(alarma: AlarmaGeneral) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TIPO_ALARMA", "GENERAL")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarma.id + 20000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}