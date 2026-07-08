package com.apexinvest.app

import android.app.Application
import com.apexinvest.app.data.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ApexApplication : Application() {
    lateinit var container: AppContainer
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Pre-fetch Yahoo credentials
        appScope.launch {
            com.apexinvest.app.api.util.YahooAuthManager.getCrumb()
        }
    }
}