package com.azahara.proyecto_final_azahara.alarm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.azahara.proyecto_final_azahara.data.local.AppDatabase
import com.azahara.proyecto_final_azahara.model.Historial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Primero preguntamos: ¿Qué tipo de alarma eres?
        val tipoAlarma = intent.getStringExtra("TIPO_ALARMA") ?: "MEDICAMENTO"

        if (tipoAlarma == "CITA") {
            // -----------------------------------------------------
            // 1. ES UNA ALARMA DE CITA MÉDICA
            // -----------------------------------------------------
            val tituloCita = intent.getStringExtra("CITA_TITULO") ?: "Cita Médica"
            val especialista = intent.getStringExtra("CITA_ESPECIALISTA") ?: ""
            val notas = intent.getStringExtra("CITA_NOTAS") ?: ""

            val mensajeCompleto = "Especialista: $especialista\nNotas: $notas"

            // Usamos la misma función visual, pero adaptada a las citas
            mostrarNotificacion(context, "Próxima cita: $tituloCita", mensajeCompleto)

        } else {
            // -----------------------------------------------------
            // 2. ES UNA ALARMA DE PASTILLA (El código que ya tenías)
            // -----------------------------------------------------
            val medicamentoId = intent.getIntExtra("MED_ID", 0)
            val nombreMedicamento = intent.getStringExtra("MED_NOMBRE") ?: "Medicamento"
            val mensaje = intent.getStringExtra("MED_MENSAJE") ?: "Es hora de tu toma."

            mostrarNotificacion(context, nombreMedicamento, mensaje)

            // Guardamos en el Historial en segundo plano (solo para medicamentos)
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val nuevoRegistro = Historial(
                        usuarioId = 1,
                        medicamentoId = medicamentoId,
                        fechaHoraReal = System.currentTimeMillis(),
                        estado = "Notificada"
                    )
                    db.historialDao().insertHistorial(nuevoRegistro)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun mostrarNotificacion(context: Context, titulo: String, mensaje: String) {
        val channelId = "pastillero_alarmas_criticas"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Configuración del Canal (Requisito nativo para Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas de Medicación",
                NotificationManager.IMPORTANCE_HIGH // Hace que la notificación "salte" en pantalla
            ).apply {
                description = "Canal para los avisos del pastillero"
                enableVibration(true) // Ayuda al usuario a notar la alerta
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Construcción de la Notificación con los datos de la Tarea 14
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Icono de sistema
            .setContentTitle("💊 ¡Toma de: $titulo!") // Nombre del medicamento
            .setContentText(mensaje) // "La pastilla azul del colesterol"
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje)) // Permite ver el mensaje largo
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true) // Se borra al pulsarla
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Usa sonido y vibración por defecto
            .build()

        // 3. Emisión local (Sin servidores externos)
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}