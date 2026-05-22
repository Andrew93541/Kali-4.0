package com.kali

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.kali.network.RetrofitClient

class KaliApplication : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        RetrofitClient.initialize(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
