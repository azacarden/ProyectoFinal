package com.azahara.proyecto_final_azahara.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val tipoAlarma = intent.getStringExtra("TIPO_ALARMA")

        if (tipoAlarma == "MEDICAMENTO") {
            val nombre = intent.getStringExtra("MED_NOMBRE") ?: "Medicamento"
            val mensaje = intent.getStringExtra("MED_MENSAJE") ?: "Es hora de tu toma diaria"

            mostrarNotificacion(context, "Toma de $nombre", mensaje, "canal_medicacion")

        } else if (tipoAlarma == "CITA") {
            val motivo = intent.getStringExtra("CITA_MOTIVO") ?: "Revisión médica"
            val medico = intent.getStringExtra("CITA_MEDICO") ?: "tu especialista"

            mostrarNotificacion(context, "Recordatorio de Cita", "Tienes cita para $motivo con $medico", "canal_citas")

        } else if (tipoAlarma == "ALARMA_GENERAL") {
            val titulo = intent.getStringExtra("GENERAL_TITULO") ?: "Aviso"
            val notas = intent.getStringExtra("GENERAL_NOTAS") ?: "Tienes una alarma programada"

            mostrarNotificacion(context, titulo, notas, "canal_general")
        }
    }

    private fun mostrarNotificacion(context: Context, titulo: String, mensaje: String, channelId: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombreCanal = when (channelId) {
                "canal_medicacion" -> "Alertas de Medicación"
                "canal_citas" -> "Avisos de Citas"
                else -> "Avisos Generales"
            }

            val channel = NotificationChannel(
                channelId,
                nombreCanal,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para los avisos de la aplicación"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}