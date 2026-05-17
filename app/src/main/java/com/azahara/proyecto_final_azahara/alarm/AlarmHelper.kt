package com.azahara.proyecto_final_azahara.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.azahara.proyecto_final_azahara.model.CitaMedica
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
    // --- FUNCIÓN PARA CITAS MÉDICAS ---
    @SuppressLint("ScheduleExactAlarm")
    fun programarAlarmaCita(cita: CitaMedica) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            // Le ponemos una "etiqueta" para que el Receiver sepa que esto es una cita y no una pastilla
            putExtra("TIPO_ALARMA", "CITA")
            putExtra("CITA_ID", cita.id)
            putExtra("CITA_TITULO", cita.titulo)
            putExtra("CITA_ESPECIALISTA", cita.especialista)
            putExtra("CITA_NOTAS", cita.notas)
        }

        // Le sumamos 10000 al ID para asegurarnos de que el ID de la cita nunca choque
        // por accidente con el ID de una pastilla (si ambos fueran el ID 1, se sobrescribirían)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            cita.id + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculamos a qué hora debe sonar la alarma:
        // Hora de la cita (en milisegundos) MENOS los minutos de aviso previo (convertidos a milisegundos)
        val milisegundosPrevios = cita.recordatorioPrevio * 60 * 1000L
        val tiempoAlarma = cita.fechaHora - milisegundosPrevios

        // Envolvemos en try/catch por si el móvil tiene restricciones severas de batería
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                tiempoAlarma,
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
        // Usamos exactamente el mismo ID único (+ 10000) para localizar la alarma correcta
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            cita.id + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Le decimos al sistema operativo que la destruya
        alarmManager.cancel(pendingIntent)
    }
}