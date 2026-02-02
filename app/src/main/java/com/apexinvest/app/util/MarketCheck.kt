package com.apexinvest.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.core.app.NotificationCompat
import com.apexinvest.app.MainActivity
import com.apexinvest.app.R
import com.apexinvest.app.data.PortfolioRepository
import kotlinx.coroutines.flow.first
import java.util.Locale
import kotlin.math.abs

object MarketCheck {

    suspend fun checkAndNotify(context: Context, repository: PortfolioRepository, isTestMode: Boolean) {
        try {
            Log.d("MarketCheck", "Starting Check. Force Mode: $isTestMode")

            val watchlist = repository.getLocalWatchlist().first()
            if (watchlist.isEmpty()) return

            val fullListBuilder = SpannableStringBuilder()

            // Loop through ALL stocks (No filtering)
            for (stock in watchlist) {
                try {
                    val detailResult = repository.getFullStockDetails(stock.symbol, "1D")

                    detailResult.getOrNull()?.let { detail ->
                        val price = detail.price
                        val change = detail.changePercent

                        // --- CHANGED: REMOVED 2% LIMIT ---
                        // We now add every stock to the notification list

                        val isUp = change >= 0
                        val arrow = if (isUp) "▲" else "▼"
                        val color = if (isUp) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")

                        // Format: "AAPL: $150.23 "
                        val prefix = String.format(Locale.US, "%s: $%.2f ", stock.symbol, price)
                        fullListBuilder.append(prefix)

                        // Format: "(▲ 1.25%)"
                        val coloredPart = String.format(Locale.US, "(%s %.2f%%)", arrow, abs(change))
                        val start = fullListBuilder.length
                        fullListBuilder.append(coloredPart)
                        val end = fullListBuilder.length

                        // Color the percentage
                        fullListBuilder.setSpan(
                            ForegroundColorSpan(color),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        fullListBuilder.append("\n")

                        // Update DB
                        repository.addWatchlistStock(stock.symbol)
                    }
                } catch (e: Exception) {
                    Log.e("MarketCheck", "Error ${stock.symbol}", e)
                }
            }

            if (fullListBuilder.isNotEmpty()) {
                // Remove trailing newline
                fullListBuilder.delete(fullListBuilder.length - 1, fullListBuilder.length)

                // Generic Title since it's a periodic status update
                val title = "Market Status (${watchlist.size} Stocks)"
                showNotification(context, title, fullListBuilder)
            }

        } catch (e: Exception) {
            Log.e("MarketCheck", "Check failed", e)
        }
    }

    private fun showNotification(context: Context, title: String, content: CharSequence) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "apex_alerts_v2"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Stock Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true)
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_WATCHLIST", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setSmallIcon(R.drawable.ic_trending_up)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}