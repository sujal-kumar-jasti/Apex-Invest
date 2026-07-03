package com.apexinvest.app

import android.app.Application
import com.apexinvest.app.data.AppContainer
<<<<<<< HEAD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ApexApplication : Application() {
    lateinit var container: AppContainer
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
=======

class ApexApplication : Application() {
    lateinit var container: AppContainer
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
<<<<<<< HEAD

        // 🚀 Pre-fetch Yahoo crumb/cookies in background
        appScope.launch {
            com.apexinvest.app.api.util.YahooAuthManager.getCrumb()
        }
=======
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
    }
}