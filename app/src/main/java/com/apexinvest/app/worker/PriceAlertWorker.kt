package com.apexinvest.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apexinvest.app.ApexApplication
import com.apexinvest.app.util.MarketCheck

class PriceAlertWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository by lazy {
        (applicationContext as ApexApplication).container.portfolioRepository
    }

    override suspend fun doWork(): Result {
        // 1. Get the Shared Preferences (Same name as in MainActivity)
        val prefs = applicationContext.getSharedPreferences("apex_auth", Context.MODE_PRIVATE)

        // 2. Check if notifications are enabled
        val isEnabled = prefs.getBoolean("notifications_enabled", true)

        if (!isEnabled) {
            // Logic: User turned off notifications. We skip the market check.
            // We return success() so the worker isn't retried, but it effectively does nothing.
            return Result.success()
        }

        // 3. If enabled, proceed with Market Check
        try {
            MarketCheck.checkAndNotify(applicationContext, repository, isTestMode = false)
        } catch (e: Exception) {
            // Optional: Log error
            return Result.retry()
        }

        return Result.success()
    }
}