package com.example.tennisapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "imu_recording_channel"
        const val CHANNEL_NAME = "IMU Recording"
        const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when IMU recording is active"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 🔥 FIX 1: Update the method signature to accept relevant KPIs
    fun showRecordingNotification(
        hitCount: Int,
        power: Float,
        spin: Float
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // 🔥 FIX 2: Format the content text to show the new data
        val contentText = "Hits: $hitCount | Power: %.0fW | Spin: %.0f RPM".format(power, spin)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_record)
            .setContentTitle("Tennis Session Active")
            .setContentText(contentText)
            .setOngoing(true) // Can't be swiped away while recording
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true) // Don't make noise for every update
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    // 🔥 FIX 3: Update this method as well
    fun updateNotification(hitCount: Int, power: Float, spin: Float) {
        // Re-using the show notification method works perfectly for updating
        showRecordingNotification(hitCount, power, spin)
    }

    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}