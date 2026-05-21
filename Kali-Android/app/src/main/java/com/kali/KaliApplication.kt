package com.kali

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

class KaliApplication : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
