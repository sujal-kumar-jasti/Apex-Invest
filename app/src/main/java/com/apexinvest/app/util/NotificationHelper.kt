package com.apexinvest.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlin.random.Random

fun showLocalTestNotification(context: Context) {
    val channelId = "test_channel_id"
    val notificationId = Random.nextInt() // Unique ID to show multiple alerts

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // 1. Create Channel (Required for Android 8+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Test Notifications",
            NotificationManager.IMPORTANCE_HIGH // HIGH makes it pop up/make sound
        ).apply {
            description = "Channel for local testing"
        }
        manager.createNotificationChannel(channel)
    }

    // 2. Build Notification
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info) // Default icon
        .setContentTitle("Test Alert #${notificationId % 100}")
        .setContentText("This is a local notification test.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    // 3. Show it
    manager.notify(notificationId, builder.build())
}