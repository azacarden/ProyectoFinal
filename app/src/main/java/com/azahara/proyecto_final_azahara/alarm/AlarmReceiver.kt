package com.azahara.proyecto_final_azahara.alarm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.azahara.proyecto_final_azahara.data.local.AppDatabase
import com.azahara.proyecto_final_azahara.model.Historial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val tipoAlarma = intent.getStringExtra("TIPO_ALARMA") ?: "MEDICAMENTO"
        Log.d("ALARMAS_AZAHARA", "¡Receiver disparado en background para: $tipoAlarma!")

        // 1. goAsync() es el "Background Service" moderno.
        // Le dice a Android: "Mantén el proceso vivo, voy a trabajar en un hilo secundario".
        val pendingResult = goAsync()

        // 2. Pasamos TODA la ejecución al hilo secundario de Entrada/Salida (IO)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // El hilo principal (UI) ya está libre. Operamos en background.
                when (tipoAlarma) {
                    "CITA" -> procesarAlarmaCita(context, intent)
                    "GENERAL" -> procesarAlarmaGeneral(context, intent)
                    "MEDICAMENTO" -> procesarAlarmaMedicamento(context, intent)
                }
            } catch (e: Exception) {
                Log.e("ALARMAS_AZAHARA", "Error crítico en background: ${e.message}")
            } finally {
                // 3. OBLIGATORIO: Avisamos a Android que hemos terminado y puede liberar memoria
                pendingResult.finish()
            }
        }
    }

    // Funciones suspendidas: Solo pueden ser llamadas desde una Corrutina (hilo secundario)

    private suspend fun procesarAlarmaCita(context: Context, intent: Intent) {
        val tituloCita = intent.getStringExtra("CITA_TITULO") ?: "Cita Médica"
        val especialista = intent.getStringExtra("CITA_ESPECIALISTA") ?: ""
        val notas = intent.getStringExtra("CITA_NOTAS") ?: ""

        val mensajeCompleto = "Especialista: $especialista\nNotas: $notas"

        mostrarNotificacion(context, "📅 Próxima cita: $tituloCita", mensajeCompleto)
    }

    private suspend fun procesarAlarmaGeneral(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra("ALARMA_TITULO") ?: "Aviso"
        val notas = intent.getStringExtra("ALARMA_NOTAS") ?: ""

        mostrarNotificacion(context, "🔔 Aviso: $titulo", notas)
    }

    private suspend fun procesarAlarmaMedicamento(context: Context, intent: Intent) {
        val horarioId = intent.getIntExtra("MED_ID", 0)
        val nombreMedicamento = intent.getStringExtra("MED_NOMBRE") ?: "Medicamento"
        val mensaje = intent.getStringExtra("MED_MENSAJE") ?: "Es hora de tu toma."

        // 1. Mostramos la interfaz al usuario
        mostrarNotificacion(context, "💊 ¡Toma de: $nombreMedicamento!", mensaje)

        // 2. Operación pesada de Base de Datos en Background puro
        val db = AppDatabase.getDatabase(context)

        // Recuperamos al usuario activo de las SharedPreferences (Opcional pero recomendado)
        val prefs = context.getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE)
        val idUsuarioActivo = prefs.getInt("usuario_id_bd", 1) // 1 por defecto

        val nuevoRegistro = Historial(
            usuarioId = idUsuarioActivo,
            horarioId = horarioId,
            fechaHoraReal = System.currentTimeMillis(),
            estado = "Notificada"
        )

        db.historialDao().insertHistorial(nuevoRegistro)
        Log.d("ALARMAS_AZAHARA", "Historial guardado en background con éxito.")
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

        // 2. Construcción de la Notificación
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Icono de sistema
            .setContentTitle(titulo) // ¡Ahora usamos la variable dinámica!
            .setContentText(mensaje)
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