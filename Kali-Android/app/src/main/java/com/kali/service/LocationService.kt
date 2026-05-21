package com.kali.service

import android.app.*
import android.content.Intent
import android.location.*
import android.os.*
import androidx.core.app.NotificationCompat
import com.kali.model.KaliDatabase
import com.kali.model.LocationEntity
import kotlinx.coroutines.*

class LocationService : Service(), LocationListener {

    private lateinit var locationManager: LocationManager
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    var activeAlertLocalId: Int = -1

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        startForeground(NOTIF_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        activeAlertLocalId = intent?.getIntExtra("alertLocalId", -1) ?: -1
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 5f, this)
        } catch (e: SecurityException) { stopSelf() }
        return START_STICKY
    }

    override fun onLocationChanged(location: Location) {
        if (activeAlertLocalId == -1) return
        scope.launch {
            KaliDatabase.get(applicationContext).locationDao().insert(
                LocationEntity(alertLocalId = activeAlertLocalId, latitude = location.latitude, longitude = location.longitude)
            )
            SyncWorker.enqueue(applicationContext)
        }
    }

    override fun onDestroy() {
        locationManager.removeUpdates(this)
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun buildNotification(): Notification {
        val channelId = "kali_location"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Location Tracking", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("KALI – SOS Active")
            .setContentText("Tracking your location for safety.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
    }

    companion object { const val NOTIF_ID = 1001 }
}
