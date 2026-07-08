package com.apexinvest.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import com.apexinvest.app.ApexApplication
import com.apexinvest.app.R
import com.apexinvest.app.data.NotificationEntity
import com.apexinvest.app.data.PortfolioRepository
import kotlinx.coroutines.flow.first
import java.util.Locale
import kotlin.math.abs

object MarketCheck {

    suspend fun checkAndNotify(context: Context, repository: PortfolioRepository, isTestMode: Boolean = false) {
        try {
            val prefs = context.getSharedPreferences("apex_prefs", Context.MODE_PRIVATE)
            if (!prefs.getBoolean("notifications_enabled", true) && !isTestMode) return

            Log.d("MarketCheck", "Starting Check with Trending Icons")

            val watchlist = repository.getLocalWatchlist().first()
            if (watchlist.isEmpty()) return

            val app = context.applicationContext as? ApexApplication
            val notificationRepo = app?.container?.notificationRepository
            val notificationService = app?.container?.financialNotificationService
            val analysisCacheDao = app?.container?.appDatabase?.analysisCacheDao()

            val inboxStyle = NotificationCompat.InboxStyle()

            var upCount = 0
            var downCount = 0
            var linesAdded = 0
            var totalChange = 0.0

            val colorUp = "#4CAF50".toColorInt()
            val colorDown = "#F44336".toColorInt()

            for (stock in watchlist) {
                try {
                    val detailResult = repository.fetchLivePriceOnly(stock.symbol)

                    detailResult?.let { detail ->
                        val price = detail.price
                        val change = detail.changePercent
                        totalChange += change

                        // Check for major move alert (> 5%)
                        if (abs(change) >= 5.0 && !isTestMode) {
                            notificationService?.sendPriceAlert(stock.symbol, price, change)
                        }

                        val isUp = change >= 0
                        if (isUp) upCount++ else downCount++

                        val rowColor = if (isUp) colorUp else colorDown
                        val iconRes = if (isUp) R.drawable.ic_trending_up else R.drawable.ic_trending_down

                        val currencySym = getCurrencySymbol(guessCurrencyFromSymbol(stock.symbol))

                        val lineBuilder = SpannableStringBuilder()
                        val prefix = String.format(Locale.US, "%-8s %s%.2f   ", stock.symbol, currencySym, price)
                        lineBuilder.append(prefix)

                        val iconStart = lineBuilder.length
                        lineBuilder.append("  ")
                        val iconSpan = createIconSpan(context, iconRes, rowColor)
                        if (iconSpan != null) {
                            lineBuilder.setSpan(
                                iconSpan,
                                iconStart,
                                iconStart + 1,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }

                        val pctStart = lineBuilder.length
                        lineBuilder.append(String.format(Locale.US, " %.2f%%", abs(change)))
                        lineBuilder.setSpan(
                            ForegroundColorSpan(rowColor),
                            pctStart,
                            lineBuilder.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        inboxStyle.addLine(lineBuilder)
                        linesAdded++

                        // 🆕 Advanced Pro-Level Triggers
                        val gson = com.google.gson.Gson()
                        val cacheKey = "DEEP_${stock.symbol}"
                        val cachedAnalysis = analysisCacheDao?.getAnalysisCache(cacheKey)
                        val analysis: com.apexinvest.app.api.models.DeepAnalysisResponse? = cachedAnalysis?.let { 
                            gson.fromJson(it.dataJson, com.apexinvest.app.api.models.DeepAnalysisResponse::class.java) 
                        }

                        analysis?.let { res ->
                            // 1. Volume Spike Check (> 2x Avg)
                            val candlesJson = detail.candlesJson
                            if (candlesJson != null) {
                                try {
                                    val type = object : com.google.gson.reflect.TypeToken<List<com.apexinvest.app.api.models.CandlePointDto>>() {}.type
                                    val candles: List<com.apexinvest.app.api.models.CandlePointDto> = gson.fromJson(candlesJson, type)
                                    val volumes: List<Double> = candles.map { it.volume.toDouble() }
                                    val avgVol: Double = if (volumes.isNotEmpty()) volumes.average() else 0.0
                                    
                                    if (detail.dayHigh > 0 && detail.dayLow > 0) {
                                        val currentVol: Double = candles.lastOrNull()?.volume?.toDouble() ?: 0.0
                                        if (avgVol > 0.0 && currentVol > (avgVol * 2.0)) {
                                            notificationService?.sendVolumeSpikeAlert(stock.symbol, currentVol / avgVol)
                                        }
                                    }
                                } catch (e: Exception) { Log.e("MarketCheck", "Vol check failed for ${stock.symbol}", e) }
                            }

                            // 2. Momentum Shift (RSI/MACD)
                            val histData = res.historicalChartData
                            val latestHist = histData.lastOrNull()
                            latestHist?.let { hist ->
                                when {
                                    (hist.rsi14 ?: 0.0) > 70.0 -> notificationService?.sendMomentumAlert(stock.symbol, "RSI", "Overbought (High Risk)")
                                    (hist.rsi14 ?: 0.0) < 30.0 -> notificationService?.sendMomentumAlert(stock.symbol, "RSI", "Oversold (Opportunity)")
                                }
                                val macdVal = hist.macd
                                val macdSignalVal = hist.macdSignal
                                if (macdVal != null && macdSignalVal != null) {
                                    if (macdVal > macdSignalVal && (macdVal - macdSignalVal) < 0.1) {
                                        notificationService?.sendMomentumAlert(stock.symbol, "MACD", "Bullish Crossover")
                                    }
                                }
                            }

                            // 3. Monte Carlo Forecast Breach
                            val forecast = res.monteCarloForecast
                            forecast.lastOrNull()?.let { mc ->
                                when {
                                    price >= mc.bullCase90th -> notificationService?.sendForecastAlert(stock.symbol, "Bull", mc.bullCase90th)
                                    price <= mc.bearCase10th -> notificationService?.sendForecastAlert(stock.symbol, "Bear", mc.bearCase10th)
                                }
                            }

                            // 4. Fundamental Health Change
                            if (res.financialHealthScore.contains("Distress", true)) {
                                notificationService?.sendHealthAlert(stock.symbol, "Healthy/Neutral", "Distress")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MarketCheck", "Error ${stock.symbol}", e)
                }
            }

            if (linesAdded > 0) {
                val summaryBuilder = SpannableStringBuilder()

                // Up Summary
                val upIconStart = summaryBuilder.length
                summaryBuilder.append("  ")
                val upSpan = createIconSpan(context, R.drawable.ic_trending_up, colorUp)
                if (upSpan != null) summaryBuilder.setSpan(upSpan, upIconStart, upIconStart + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                val upTextStart = summaryBuilder.length
                summaryBuilder.append(" $upCount Up   •   ")
                summaryBuilder.setSpan(ForegroundColorSpan(colorUp), upTextStart, upTextStart + 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                // Down Summary
                val downIconStart = summaryBuilder.length
                summaryBuilder.append("  ")
                val downSpan = createIconSpan(context, R.drawable.ic_trending_down, colorDown)
                if (downSpan != null) summaryBuilder.setSpan(downSpan, downIconStart, downIconStart + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                val downTextStart = summaryBuilder.length
                summaryBuilder.append(" $downCount Down")
                summaryBuilder.setSpan(ForegroundColorSpan(colorDown), downTextStart, summaryBuilder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                inboxStyle.setSummaryText(summaryBuilder)

                val title = "Watchlist Update"
                showPremiumNotification(context, title, inboxStyle, watchlist.size)
                
                // Save summary to DB
                val avgChange = totalChange / watchlist.size
                notificationRepo?.insertNotification(
                    NotificationEntity(
                        title = title,
                        message = "$upCount Up, $downCount Down. Avg Change: ${String.format(Locale.US, "%.2f%%", avgChange)}",
                        type = "WatchlistUpdate"
                    )
                )

                // 🆕 New: Portfolio Milestone check (dummy logic for demo)
                if (avgChange > 2.0) {
                    notificationService?.sendMilestoneAlert("surged by over 2% today!")
                } else if (avgChange < -2.0) {
                    notificationService?.sendMilestoneAlert("is facing some market headwinds today.")
                }
            }

        } catch (e: Exception) {
            Log.e("MarketCheck", "Check failed", e)
        }
    }

    private fun createIconSpan(context: Context, drawableResId: Int, colorInt: Int): ImageSpan? {
        val drawable = ContextCompat.getDrawable(context, drawableResId)?.mutate() ?: return null
        drawable.setTint(colorInt)

        val size = (context.resources.displayMetrics.density * 16).toInt()
        drawable.setBounds(0, 0, size, size)

        return ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM)
    }

    private fun showPremiumNotification(
        context: Context,
        @Suppress("SameParameterValue") title: String,
        inboxStyle: NotificationCompat.InboxStyle,
        totalStocks: Int
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "apex_watchlist_alerts"

        val channel = NotificationChannel(channelId, "Market Updates", NotificationManager.IMPORTANCE_HIGH).apply {
            enableLights(true)
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
        val mainIntent = Intent(
            Intent.ACTION_VIEW,
            "apexinvest://watchlist".toUri()
        ).apply {
            setPackage(context.packageName)
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        inboxStyle.setBigContentTitle("$title ($totalStocks)")

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText("Tap to view your Watchlist")
            .setStyle(inboxStyle)
            .setSmallIcon(R.drawable.ic_trending_up)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            .setColor("#673AB7".toColorInt())
            .setContentIntent(mainPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}