package com.weox.upisoundbox.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

/**
 * DEBUG helper: posts a notification from this app itself, worded so that
 * TestSelfParser recognizes it. This lets you verify the entire pipeline
 * (listener -> parser -> voice cache/backend -> volume boost -> playback)
 * without needing GPay/PhonePe/Paytm installed or a real payment.
 */
object TestNotificationSender {
    private const val CHANNEL_ID = "upi_sound_box_test"
    private const val NOTIFICATION_ID = 9001

    fun sendTestPayment(context: Context, amountRupees: Double) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Test payments",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        // "TESTPAY" is the marker TestSelfParser looks for — must match exactly.
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentTitle("TESTPAY received")
            .setContentText("₹$amountRupees received from Test User")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }
}
