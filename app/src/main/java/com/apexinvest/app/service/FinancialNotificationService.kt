package com.apexinvest.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import com.apexinvest.app.R
import com.apexinvest.app.data.NotificationEntity
import com.apexinvest.app.data.repository.NotificationRepository
import java.util.Locale

class FinancialNotificationService(
    private val context: Context,
    private val notificationRepository: NotificationRepository
) {
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "apex_financial_alerts"

    init {
        val channel = NotificationChannel(
            channelId,
            "Financial Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Price alerts, market moves, and portfolio updates"
            enableLights(true)
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    suspend fun sendPriceAlert(symbol: String, currentPrice: Double, changePercent: Double) {
        val direction = if (changePercent >= 0) "up" else "down"
        val emoji = if (changePercent >= 0) "🚀" else "📉"
        val title = "Price Alert: $symbol"
        val message = "$symbol is $direction ${String.format(Locale.US, "%.2f%%", Math.abs(changePercent))} today! Current Price: $${String.format(Locale.US, "%.2f", currentPrice)} $emoji"
        
        sendNotification(title, message, "PriceAlert", symbol, "apexinvest://stock/$symbol")
    }

    suspend fun sendVolumeSpikeAlert(symbol: String, volumeRatio: Double) {
        val title = "Volume Spike: $symbol"
        val message = "$symbol trading volume is ${String.format(Locale.US, "%.1f", volumeRatio)}x higher than average! Big move incoming? 📊"
        
        sendNotification(title, message, "VolumeSpike", symbol, "apexinvest://stock/$symbol")
    }

    suspend fun sendMomentumAlert(symbol: String, indicator: String, status: String) {
        val title = "Momentum Shift: $symbol"
        val emoji = if (status.contains("Overbought") || status.contains("Bearish")) "⚠️" else "⚡"
        val message = "$indicator shows $symbol is $status $emoji"
        
        sendNotification(title, message, "MomentumShift", symbol, "apexinvest://stock/$symbol")
    }

    suspend fun sendForecastAlert(symbol: String, caseType: String, targetPrice: Double) {
        val title = "AI Forecast Alert: $symbol"
        val emoji = if (caseType == "Bull") "📈" else "📉"
        val message = "$symbol has entered its $caseType Case territory ($${String.format(Locale.US, "%.2f", targetPrice)}). AI Quant Synthesis updated! $emoji"
        
        sendNotification(title, message, "Forecast", symbol, "apexinvest://analysis/$symbol")
    }

    suspend fun sendHealthAlert(symbol: String, oldScore: String, newScore: String) {
        val title = "Fundamental Health: $symbol"
        val message = "Financial health for $symbol changed from $oldScore to $newScore. Review the updated fundamental thesis. 🏥"
        
        sendNotification(title, message, "Health", symbol, "apexinvest://analysis/$symbol")
    }

    suspend fun sendMarketSummary(upCount: Int, downCount: Int, totalChange: Double) {
        val status = if (totalChange >= 0) "Bullish" else "Bearish"
        val title = "Daily Market Wrap: $status"
        val message = "Your watchlist has $upCount stocks up and $downCount stocks down today. Overall change: ${String.format(Locale.US, "%.2f%%", totalChange)}"
        
        sendNotification(title, message, "MarketSummary", null, "apexinvest://watchlist")
    }

    suspend fun sendMilestoneAlert(milestone: String) {
        val title = "Portfolio Milestone! 🎉"
        val message = "Congratulations! Your portfolio just $milestone"
        
        sendNotification(title, message, "Milestone", null, "apexinvest://portfolio")
    }

    suspend fun sendNotification(
        title: String,
        message: String,
        type: String = "General",
        symbol: String? = null,
        deepLink: String? = null
    ) {
        val prefs = context.getSharedPreferences("apex_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notifications_enabled", true)) return

        // 1. Save to Database
        val entity = NotificationEntity(
            title = title,
            message = message,
            type = type,
            symbol = symbol
        )
        notificationRepository.insertNotification(entity)

        // 2. Show System Notification
        val intentUri = (deepLink ?: "apexinvest://notifications").toUri()
        val mainIntent = Intent(Intent.ACTION_VIEW, intentUri).apply {
            setPackage(context.packageName)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_trending_up)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            .setColor("#673AB7".toColorInt())
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
