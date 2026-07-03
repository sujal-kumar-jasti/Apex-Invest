package com.apexinvest.app.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apexinvest.app.ApexApplication
import com.apexinvest.app.util.MarketCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MarketUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("MarketUpdateWorker", "Worker triggered successfully")

            val application = applicationContext as ApexApplication
            val repository = application.container.portfolioRepository
            MarketCheck.checkAndNotify(applicationContext, repository)

            Log.d("MarketUpdateWorker", "Worker finished successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e("MarketUpdateWorker", "Worker failed to execute", e)
            Result.retry()
        }
    }
}