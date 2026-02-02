package com.apexinvest.app

import android.app.Application
import com.apexinvest.app.data.AppContainer

class ApexApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}