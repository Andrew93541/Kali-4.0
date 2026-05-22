package com.kali.ui.sos

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.kali.R
import com.kali.databinding.ActivityTrackingBinding
import com.kali.network.Alert
import com.kali.network.HistoricalLocation
import com.kali.network.RetrofitClient
import com.kali.network.TelemetryStats
import com.kali.ui.auth.LoginActivity
import com.kali.util.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline

class TrackingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrackingBinding
    private val session by lazy { SessionManager(this) }
    private var trackingJob: Job? = null
    private var activeAlert: Alert? = null
    private var lastUserInteractedTime = 0L
    private var isFirstLoad = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize OSMdroid Configuration
        val sharedPrefs = getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        Configuration.getInstance().load(applicationContext, sharedPrefs)

        binding = ActivityTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val role = intent.getStringExtra("ROLE") ?: session.getRole() ?: "Guardian"
        binding.tvDashboardTitle.text = if (role == "Admin") "POLICE TACTICAL RADAR" else "FAMILY SAFETY DASHBOARD"

        // Set up MapView settings
        binding.mapView.apply {
            setMultiTouchControls(true)
            controller.setZoom(17.5)
            
            // Listen for user touches to pause auto-centering
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                    lastUserInteractedTime = System.currentTimeMillis()
                }
                false
            }
        }

        // Exit / Logout Dashboard Action
        binding.btnExitDashboard.setOnClickListener {
            stopTracking()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Call target emergency contact
        binding.btnCallTarget.setOnClickListener {
            // Pull phone number of user under active alert, or standard emergency line
            val phoneNumber = activeAlert?.phone ?: "+919876543210" // Fallback demo number
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        }

        // Resolve emergency alert action
        binding.btnResolveAlert.setOnClickListener {
            activeAlert?.let { alert ->
                lifecycleScope.launch {
                    binding.btnResolveAlert.isEnabled = false
                    binding.btnResolveAlert.text = "RESOLVING..."
                    val token = session.getToken() ?: ""
                    val response = runCatching {
                        RetrofitClient.api.resolveAlert("Bearer $token", alert.id)
                    }.getOrNull()

                    binding.btnResolveAlert.isEnabled = true
                    binding.btnResolveAlert.text = "RESOLVE EMERGENCY"

                    if (response?.isSuccessful == true) {
                        Toast.makeText(this@TrackingActivity, "Emergency alert resolved successfully.", Toast.LENGTH_SHORT).show()
                        activeAlert = null
                        binding.mapView.overlays.clear()
                        binding.mapView.invalidate()
                        resetTelemetryUI()
                    } else {
                        Toast.makeText(this@TrackingActivity, "Failed to resolve emergency.", Toast.LENGTH_SHORT).show()
                    }
                }
            } ?: run {
                Toast.makeText(this, "No active alert to resolve.", Toast.LENGTH_SHORT).show()
            }
        }

        // Only show Resolve button to Police/Admin
        binding.btnResolveAlert.visibility = if (role == "Admin") View.VISIBLE else View.GONE

        // Start real-time native tracking update loop
        startTracking()
    }

    private fun startTracking() {
        trackingJob = lifecycleScope.launch {
            val token = session.getToken() ?: ""
            while (isActive) {
                try {
                    val alertsResponse = runCatching { RetrofitClient.api.getAlerts("Bearer $token") }.getOrNull()
                    if (alertsResponse?.isSuccessful == true) {
                        val alerts = alertsResponse.body() ?: emptyList()
                        // Grab the first unresolved active alert
                        val alert = alerts.firstOrNull { it.status == "active" }
                        
                        if (alert != null) {
                            activeAlert = alert
                            binding.tvTargetName.text = alert.name ?: "Active SOS User"
                            binding.viewStatusLight.backgroundTintList = ContextCompat.getColorStateList(this@TrackingActivity, R.color.kali_red)

                            // Fetch coordinate trail and telemetry calculations
                            val locsResponse = runCatching { RetrofitClient.api.getLocations("Bearer $token", alert.id) }.getOrNull()
                            val statsResponse = runCatching { RetrofitClient.api.getLocationStats("Bearer $token", alert.id) }.getOrNull()

                            if (locsResponse?.isSuccessful == true && statsResponse?.isSuccessful == true) {
                                val locations = locsResponse.body() ?: emptyList()
                                val stats = statsResponse.body()

                                if (locations.isNotEmpty() && stats != null) {
                                    updateMapAndTelemetry(locations, stats)
                                }
                            }
                        } else {
                            // No active alerts
                            activeAlert = null
                            binding.tvTargetName.text = "Searching live feeds..."
                            binding.viewStatusLight.backgroundTintList = ContextCompat.getColorStateList(this@TrackingActivity, R.color.kali_green)
                            binding.mapView.overlays.clear()
                            binding.mapView.invalidate()
                            resetTelemetryUI()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("KALI_TRACKING", "Error in tracking loop", e)
                }
                delay(2000) // Poll every 2 seconds for high-fidelity native updates
            }
        }
    }

    private fun updateMapAndTelemetry(locations: List<HistoricalLocation>, stats: TelemetryStats) {
        binding.mapView.overlays.clear()

        // 1. Draw Breadcrumb Trail (Polyline)
        val geoPoints = locations.map { GeoPoint(it.latitude, it.longitude) }
        val polyline = Polyline(binding.mapView).apply {
            setPoints(geoPoints)
            outlinePaint.color = ContextCompat.getColor(this@TrackingActivity, R.color.kali_pink)
            outlinePaint.strokeWidth = 6f
        }
        binding.mapView.overlays.add(polyline)

        // 2. Draw Geofence Safety Circle (Centered at first point of active SOS)
        val firstPoint = geoPoints.first()
        val geofenceCircle = Polygon(binding.mapView).apply {
            points = Polygon.pointsAsCircle(firstPoint, 500.0) // 500m geofence radius
            fillPaint.color = 0x14FF2D8A.toInt() // 8% translucent pink
            outlinePaint.color = ContextCompat.getColor(this@TrackingActivity, R.color.kali_pink)
            outlinePaint.strokeWidth = 2f
        }
        binding.mapView.overlays.add(geofenceCircle)

        // 3. Draw Pulsing Marker at latest coordinate
        val latestPoint = geoPoints.last()
        val marker = Marker(binding.mapView).apply {
            position = latestPoint
            icon = ContextCompat.getDrawable(this@TrackingActivity, R.drawable.bg_pulsing_marker)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        }
        binding.mapView.overlays.add(marker)

        // 4. Smart Auto-Pan (Only auto-center if user hasn't dragged map recently)
        if (isFirstLoad) {
            binding.mapView.controller.setCenter(latestPoint)
            isFirstLoad = false
        } else {
            val quietTimeElapsed = System.currentTimeMillis() - lastUserInteractedTime
            if (quietTimeElapsed > 5000) { // 5 seconds of inactivity
                binding.mapView.controller.animateTo(latestPoint)
            }
        }

        binding.mapView.invalidate()

        // 5. Update Telemetry HUD Labels
        binding.tvTelemetrySpeed.text = "${stats.avgSpeed} km/h"
        binding.tvTelemetryDistance.text = "${stats.totalDistance} km"
        binding.tvTelemetryBearing.text = "${getCompassDirection(stats.lastBearing)} (${stats.lastBearing.toInt()}°)"
        binding.tvTelemetryEta.text = "${calculateEta(stats.totalDistance)} min"
    }

    private fun resetTelemetryUI() {
        binding.tvTelemetrySpeed.text = "0.0 km/h"
        binding.tvTelemetryDistance.text = "0.00 km"
        binding.tvTelemetryBearing.text = "-- (0°)"
        binding.tvTelemetryEta.text = "-- min"
        isFirstLoad = true
    }

    private fun getCompassDirection(bearing: Double): String {
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")
        val index = (((bearing + 22.5) % 360) / 45).toInt()
        return directions[index]
    }

    private fun calculateEta(distanceKm: Double): String {
        if (distanceKm <= 0) return "--"
        // Emergency vehicle moving at average speed of 40 km/h
        val minutes = (distanceKm / 40.0 * 60).toInt()
        return if (minutes < 1) "1" else "$minutes"
    }

    private fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        stopTracking()
        binding.mapView.onPause()
        super.onDestroy()
    }
}
