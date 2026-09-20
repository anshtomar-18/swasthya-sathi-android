package com.swasthyasathi.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.swasthyasathi.app.MainActivity

class HealthAlertManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_HEALTH = "swasthya_health_alerts"
        const val CHANNEL_EMERGENCY = "swasthya_emergency_alerts"
        const val CHANNEL_WEARABLE = "swasthya_wearable_alerts"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val healthChannel = NotificationChannel(
                CHANNEL_HEALTH,
                "Swasthya Health Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Proactive AI health and thermal stress alerts"
            }

            val emergencyChannel = NotificationChannel(
                CHANNEL_EMERGENCY,
                "Swasthya Emergency & SOS Alerts",
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "Critical SOS and Fall Detection alerts"
            }

            val wearableChannel = NotificationChannel(
                CHANNEL_WEARABLE,
                "Swasthya Watch Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Wearable watch telemetry & connection alerts"
            }

            notificationManager.createNotificationChannels(listOf(healthChannel, emergencyChannel, wearableChannel))
        }
    }

    fun sendProactiveHealthAlert(title: String, message: String, isEmergency: Boolean = false) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = if (isEmergency) CHANNEL_EMERGENCY else CHANNEL_HEALTH
        val icon = android.R.drawable.stat_notify_error

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(if (isEmergency) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
    }
}
