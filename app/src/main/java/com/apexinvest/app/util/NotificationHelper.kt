package com.apexinvest.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.graphics.toColorInt
import com.apexinvest.app.MainActivity
import com.apexinvest.app.R
import kotlin.random.Random

fun showLocalTestNotification(context: Context) {
    val channelId = "test_premium_alerts"
    val notificationId = Random.nextInt() // Unique ID to show multiple alerts

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // 1. Create Channel with high importance for heads-up display
    val channel = NotificationChannel(
        channelId,
        "Test Premium Notifications",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "Channel for testing premium UI and deep links"
        enableLights(true)
        enableVibration(true)
    }
    manager.createNotificationChannel(channel)

    // 2. Create the Deep Link Intent (Routes strictly to Watchlist)
    val mainIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("apexinvest://watchlist"),
        context,
        MainActivity::class.java
    )

    val pendingIntent = PendingIntent.getActivity(
        context,
        notificationId, // Use the random ID so multiple test intents don't overwrite each other
        mainIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    // 3. Build a dummy InboxStyle to test the premium look on your device
    val inboxStyle = NotificationCompat.InboxStyle()
        .setBigContentTitle("Watchlist Update (Test)")
        .addLine("AAPL     ${getCurrencySymbol("USD")}150.23   ▲ 1.25%")
        .addLine("RELIANCE ${getCurrencySymbol("INR")}2500.00  ▼ 0.50%")
        .setSummaryText("▲ 1 Up   •   ▼ 1 Down")

    // 4. Build the Premium Notification
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_trending_up) // Your new official vector asset
        .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)) // App logo
        .setContentTitle("Watchlist Update (Test)")
        .setContentText("Tap to view your Watchlist")
        .setStyle(inboxStyle)
        .setColor("#673AB7".toColorInt()) // Your app's purple accent
        .setContentIntent(pendingIntent) // Attach the deep link for testing
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    // 5. Show it
    manager.notify(notificationId, builder.build())
}