package com.azahara.proyecto_final_azahara.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.azahara.proyecto_final_azahara.model.MedicamentoConHorarios
import java.util.Calendar

class AlarmHelper(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun programarAlarma(wrapper: MedicamentoConHorarios) {
        val medicamento = wrapper.medicamento

        wrapper.horarios.forEachIndexed { index, horario ->
            val partes = horario.horaToma.trim().split(":")
            if (partes.size == 2) {
                val hora = partes[0].toIntOrNull() ?: return@forEachIndexed
                val minuto = partes[1].toIntOrNull() ?: return@forEachIndexed

                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("TIPO_ALARMA", "MEDICAMENTO")
                    putExtra("MED_NOMBRE", medicamento.nombre)
                    putExtra("MED_MENSAJE", medicamento.mensajePersonalizado)
                    putExtra("MED_ID", medicamento.id)
                    putExtra("MED_PACIENTE", medicamento.pacienteNombre)
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

                // Vuelve al metodo exacto para broadcasts invisibles.
                // Esto hará que las notificaciones vuelvan a aparecer en el panel del teléfono.
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

    fun cancelarAlarma(wrapper: MedicamentoConHorarios) {
        val medicamento = wrapper.medicamento

        wrapper.horarios.forEachIndexed { index, _ ->
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

    fun programarAlarmaCita(cita: com.azahara.proyecto_final_azahara.model.CitaMedica) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TIPO_ALARMA", "CITA")
            putExtra("CITA_MOTIVO", cita.motivo)
            putExtra("CITA_MEDICO", cita.medico)
            putExtra("CITA_ID", cita.id)
            putExtra("CITA_PACIENTE", cita.creadoPorNombre)
        }

        val requestCode = cita.id.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val milisegundosPrevios = cita.recordatorioPrevio * 60 * 1000L
        val tiempoAviso = cita.fechaHora - milisegundosPrevios

        if (tiempoAviso > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    tiempoAviso,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    tiempoAviso,
                    pendingIntent
                )
            }
        }
    }

    fun cancelarAlarmaCita(cita: com.azahara.proyecto_final_azahara.model.CitaMedica) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val requestCode = cita.id.hashCode()

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

    fun programarAlarmaGeneral(alarma: com.azahara.proyecto_final_azahara.model.AlarmaGeneral) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TIPO_ALARMA", "ALARMA_GENERAL")
            putExtra("GENERAL_TITULO", alarma.titulo)
            putExtra("GENERAL_NOTAS", alarma.descripcion)
            putExtra("GENERAL_ID", alarma.id)
        }

        val requestCode = alarma.id

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (alarma.fechaHora > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarma.fechaHora,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarma.fechaHora,
                    pendingIntent
                )
            }
        }
    }

    fun cancelarAlarmaGeneral(alarma: com.azahara.proyecto_final_azahara.model.AlarmaGeneral) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val requestCode = alarma.id

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