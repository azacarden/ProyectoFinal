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
            val medId = intent.getStringExtra("MED_ID") ?: ""

            mostrarNotificacion(context, "Toma de $nombre", mensaje)
        }
    }

    private fun mostrarNotificacion(context: Context, titulo: String, mensaje: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "canal_medicacion"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas de Medicación",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para los avisos del pastillero"
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