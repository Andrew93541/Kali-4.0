package com.kali.ui.sos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.os.VibrationEffect
import android.telephony.SmsManager
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kali.R
import com.kali.databinding.ActivitySosBinding
import com.kali.network.EmergencyContact
import com.kali.network.RetrofitClient
import com.kali.ui.auth.LoginActivity
import com.kali.ui.auth.ProfileActivity
import com.kali.util.SessionManager
import com.kali.service.SyncWorker
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sqrt

class SosActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivitySosBinding
    private val viewModel: SosViewModel by viewModels()
    private lateinit var fusedLocation: FusedLocationProviderClient
    private lateinit var session: SessionManager

    // Countdown / Active SOS Timers
    private var countDownTimer: CountDownTimer? = null
    private var sosTimerHandler: Handler? = null
    private var sosStartTime: Long = 0L
    private var isCountingDown = false

    // Shake Trigger Sensors
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastShakeTime: Long = 0L
    private val SHAKE_THRESHOLD = 12.0f // acceleration above gravity (m/s^2)

    // Secure Escape Utility: Simulated Fake Call
    private var ringtone: Ringtone? = null
    private var fakeCallTimerHandler: Handler? = null
    private var fakeCallStartTime: Long = 0L

    // Scream Guard variables
    private var audioRecord: AudioRecord? = null
    private var isAudioThreadRunning = false
    private var audioThread: Thread? = null
    private val SAMPLE_RATE = 8000
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

    // Cached emergency contacts for SMS failover
    private var cachedContacts = listOf<EmergencyContact>()

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        val fineLocationGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] ?: (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        val recordAudioGranted = perms[Manifest.permission.RECORD_AUDIO] ?: (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
        
        if (recordAudioGranted && binding.switchScreamGuard.tag == "requesting") {
            binding.switchScreamGuard.tag = null
            binding.switchScreamGuard.isChecked = true
            startScreamGuard()
            return@registerForActivityResult
        }

        if (fineLocationGranted && recordAudioGranted) {
            startCountdown()
        } else {
            Toast.makeText(this, "Location and Audio permissions are required for SOS.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)

        // Sensors
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Core Buttons
        binding.btnSos.setOnClickListener { checkPermissionsAndStartCountdown() }
        binding.btnCancel.setOnClickListener {
            countDownTimer?.cancel()
            isCountingDown = false
            binding.tvCountdown.visibility = View.GONE
            binding.btnSos.isEnabled = true
            viewModel.cancelSOS()
        }

        binding.btnLogout.setOnClickListener {
            session.clear()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Fake Call Secure Escape Buttons
        binding.btnFakeCall.setOnClickListener { triggerFakeCall() }
        binding.btnAnswerCall.setOnClickListener { answerFakeCall() }
        binding.btnDeclineCall.setOnClickListener { endFakeCall() }
        binding.btnEndCall.setOnClickListener { endFakeCall() }

        // Scream Guard Switch
        binding.switchScreamGuard.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    startScreamGuard()
                } else {
                    binding.switchScreamGuard.tag = "requesting"
                    binding.switchScreamGuard.isChecked = false
                    permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                }
            } else {
                binding.switchScreamGuard.tag = null
                stopScreamGuard()
            }
        }

        // Observe ViewModel
        viewModel.sosActive.observe(this) { active ->
            if (active) {
                onSosActivated()
            } else {
                onSosCancelled()
            }
        }

        viewModel.status.observe(this) { binding.tvStatus.text = it }

        // Load location and contacts
        loadCurrentLocation()
        loadEmergencyContacts()
        SyncWorker.enqueue(this)
    }

    // ── Sensors: Shake silent SOS triggers ───────────────────────────────────────────

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gForce = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
        if (gForce > SHAKE_THRESHOLD) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime > 3000) { // 3s debounce interval
                lastShakeTime = now
                Toast.makeText(this, "Emergency shake detected! Triggering SOS countdown...", Toast.LENGTH_SHORT).show()
                checkPermissionsAndStartCountdown()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // ── Permissions ──────────────────────────────────────────────────────────────

    private fun checkPermissionsAndStartCountdown() {
        if (isCountingDown) return
        val needed = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.SEND_SMS
        )
        val missing = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) startCountdown() else permissionLauncher.launch(missing.toTypedArray())
    }

    // ── Countdown 3-2-1 ──────────────────────────────────────────────────────────

    private fun startCountdown() {
        isCountingDown = true
        binding.tvCountdown.visibility = View.VISIBLE
        binding.btnSos.isEnabled = false
        binding.btnCancel.isEnabled = true

        countDownTimer = object : CountDownTimer(3500, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = (millisUntilFinished / 1000).toInt() + 1
                binding.tvCountdown.text = if (sec > 0) "$sec" else "SOS!"
            }

            override fun onFinish() {
                binding.tvCountdown.text = "SOS!"
                binding.tvCountdown.postDelayed({
                    binding.tvCountdown.visibility = View.GONE
                    isCountingDown = false
                    activateSOS()
                }, 400)
            }
        }.start()
    }

    // ── SOS Trigger ──────────────────────────────────────────────────────────────

    private fun activateSOS() {
        var lat: Double? = null
        var lng: Double? = null
        try {
            fusedLocation.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    lat = loc.latitude
                    lng = loc.longitude
                }
                viewModel.triggerSOS(lat, lng)
                if (!isNetworkAvailable()) {
                    sendEmergencySMS(lat, lng)
                }
            }.addOnFailureListener {
                viewModel.triggerSOS(null, null)
                if (!isNetworkAvailable()) {
                    sendEmergencySMS(null, null)
                }
            }
        } catch (e: SecurityException) {
            viewModel.triggerSOS(null, null)
            if (!isNetworkAvailable()) {
                sendEmergencySMS(null, null)
            }
        }
    }

    // ── SOS Active UI ─────────────────────────────────────────────────────────────

    private fun onSosActivated() {
        binding.btnSos.isEnabled = false
        binding.btnSos.alpha = 0.4f
        binding.btnCancel.isEnabled = true
        binding.layoutSosActive.visibility = View.VISIBLE

        // Start duration timer
        sosStartTime = System.currentTimeMillis()
        sosTimerHandler = Handler(Looper.getMainLooper())
        val timerRunnable = object : Runnable {
            override fun run() {
                val elapsed = (System.currentTimeMillis() - sosStartTime) / 1000
                val mins = elapsed / 60
                val secs = elapsed % 60
                binding.tvSosDuration.text = "SOS Active: %02d:%02d".format(mins, secs)
                sosTimerHandler?.postDelayed(this, 1000)
            }
        }
        sosTimerHandler?.post(timerRunnable)

        // Show emergency contacts card now that SOS is active
        binding.layoutContacts.visibility = View.VISIBLE
    }

    private fun onSosCancelled() {
        binding.btnSos.isEnabled = true
        binding.btnSos.alpha = 1f
        binding.btnCancel.isEnabled = false
        binding.layoutSosActive.visibility = View.GONE
        sosTimerHandler?.removeCallbacksAndMessages(null)
        sosTimerHandler = null
    }

    // ── Location & Address ────────────────────────────────────────────────────────

    private fun loadCurrentLocation() {
        binding.tvAddress.text = "Acquiring GPS…"
        try {
            fusedLocation.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    binding.viewGpsDot.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.kali_green)
                    reverseGeocode(loc.latitude, loc.longitude)
                } else {
                    binding.tvAddress.text = "GPS signal not found"
                }
            }
        } catch (e: SecurityException) {
            binding.tvAddress.text = "Location permission needed"
        }
    }

    private fun reverseGeocode(lat: Double, lng: Double) {
        lifecycleScope.launch {
            try {
                val geocoder = Geocoder(this@SosActivity, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val addr = addresses[0]
                    val line = buildString {
                        addr.subLocality?.let { append("$it, ") }
                        addr.locality?.let { append("$it") }
                        if (isEmpty()) append("%.4f, %.4f".format(lat, lng))
                    }
                    binding.tvAddress.text = line
                } else {
                    binding.tvAddress.text = "%.4f, %.4f".format(lat, lng)
                }
            } catch (e: Exception) {
                binding.tvAddress.text = "%.4f, %.4f".format(lat, lng)
            }
        }
    }

    // ── Emergency Contacts ────────────────────────────────────────────────────────

    private fun loadEmergencyContacts() {
        lifecycleScope.launch {
            val token = "Bearer ${session.getToken() ?: return@launch}"
            val response = runCatching { RetrofitClient.api.getContacts(token) }.getOrNull()
            if (response?.isSuccessful == true) {
                val contacts = response.body() ?: emptyList()
                if (contacts.isNotEmpty()) {
                    cachedContacts = contacts
                    populateContactsUI(contacts)
                    binding.layoutContacts.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun populateContactsUI(contacts: List<EmergencyContact>) {
        binding.llContactsContainer.removeAllViews()
        contacts.forEach { contact ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.bottomMargin = (10 * resources.displayMetrics.density).toInt()
                layoutParams = params
            }

            val nameView = TextView(this).apply {
                val p = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                layoutParams = p
                text = "${contact.name}\n${contact.relation ?: ""}"
                setTextColor(ContextCompat.getColor(this@SosActivity, R.color.kali_text))
                textSize = 13f
            }

            val callBtn = MaterialButton(this).apply {
                val p = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    (40 * resources.displayMetrics.density).toInt()
                )
                layoutParams = p
                text = "📞 Call"
                textSize = 12f
                setTextColor(ContextCompat.getColor(this@SosActivity, R.color.white))
                backgroundTintList = ContextCompat.getColorStateList(this@SosActivity, R.color.kali_pink)
                cornerRadius = (20 * resources.displayMetrics.density).toInt()
                insetTop = 0
                insetBottom = 0
                setOnClickListener {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
                    startActivity(intent)
                }
            }

            row.addView(nameView)
            row.addView(callBtn)
            binding.llContactsContainer.addView(row)
        }
    }

    // ── Secure Escape Utility: Simulated Fake Call Logic ──────────────────────────

    private fun triggerFakeCall() {
        binding.layoutFakeCallOverlay.visibility = View.VISIBLE
        binding.llCallActions.visibility = View.VISIBLE
        binding.llActiveCallHud.visibility = View.GONE
        binding.llEndCall.visibility = View.GONE

        // Realistic dummy details
        binding.tvCallerName.text = "MOM"
        binding.tvCallerNumber.text = "Mobile +91 98765 43210"

        // Play phone call ringtone
        try {
            val ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(applicationContext, ringUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun answerFakeCall() {
        ringtone?.stop()

        binding.llCallActions.visibility = View.GONE
        binding.llActiveCallHud.visibility = View.VISIBLE
        binding.llEndCall.visibility = View.VISIBLE

        // Start call active stopwatch timer
        fakeCallStartTime = System.currentTimeMillis()
        fakeCallTimerHandler = Handler(Looper.getMainLooper())
        val callTimerRunnable = object : Runnable {
            override fun run() {
                val elapsed = (System.currentTimeMillis() - fakeCallStartTime) / 1000
                val mins = elapsed / 60
                val secs = elapsed % 60
                binding.tvCallTimer.text = "%02d:%02d".format(mins, secs)
                fakeCallTimerHandler?.postDelayed(this, 1000)
            }
        }
        fakeCallTimerHandler?.post(callTimerRunnable)
    }

    private fun endFakeCall() {
        ringtone?.stop()
        fakeCallTimerHandler?.removeCallbacksAndMessages(null)
        fakeCallTimerHandler = null
        binding.layoutFakeCallOverlay.visibility = View.GONE
    }

    // ── Advanced Features: Hands-Free Scream Guard decibel checking ────────────────

    private fun startScreamGuard() {
        if (isAudioThreadRunning) return
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        if (bufferSize <= 0) return

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                return
            }

            audioRecord?.startRecording()
            isAudioThreadRunning = true
            
            var aboveThresholdDuration = 0L
            val thresholdDb = 85.0
            val checkIntervalMs = 100L

            audioThread = Thread {
                val buffer = ShortArray(bufferSize)
                while (isAudioThreadRunning) {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readSize > 0) {
                        var sum = 0.0
                        for (i in 0 until readSize) {
                            sum += buffer[i] * buffer[i]
                        }
                        val rms = sqrt(sum / readSize)
                        val db = if (rms > 0) 20 * kotlin.math.log10(rms / 32767.0) + 90 else 0.0

                        if (db >= thresholdDb) {
                            aboveThresholdDuration += checkIntervalMs
                            if (aboveThresholdDuration >= 1500) { // 1.5 seconds
                                Handler(Looper.getMainLooper()).post {
                                    if (!isCountingDown && viewModel.sosActive.value != true) {
                                        Toast.makeText(
                                            this@SosActivity,
                                            "Scream Guard: Distress signal detected (${"%.1f".format(db)} dB)!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        
                                        val vibrator = getSystemService(Vibrator::class.java)
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                                        } else {
                                            @Suppress("DEPRECATION")
                                            vibrator.vibrate(500)
                                        }
                                        checkPermissionsAndStartCountdown()
                                    }
                                }
                                aboveThresholdDuration = 0L
                            }
                        } else {
                            aboveThresholdDuration = 0L
                        }
                    }
                    try {
                        Thread.sleep(checkIntervalMs)
                    } catch (e: InterruptedException) {
                        break
                    }
                }
            }.apply { start() }

        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopScreamGuard() {
        isAudioThreadRunning = false
        audioThread?.interrupt()
        audioThread = null
        try {
            if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.stop()
            }
            audioRecord?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioRecord = null
    }

    // ── Advanced Features: Offline SMS failover dispatcher ──────────────────────

    private fun sendEmergencySMS(lat: Double?, lng: Double?) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "SMS permission not granted. Cannot send emergency SMS.", Toast.LENGTH_SHORT).show()
            return
        }
        if (cachedContacts.isEmpty()) {
            Toast.makeText(this, "No emergency contacts configured to receive SMS.", Toast.LENGTH_SHORT).show()
            return
        }

        val address = binding.tvAddress.text.toString()
        val locationLink = if (lat != null && lng != null) "https://maps.google.com/?q=$lat,$lng" else "Location coordinates unavailable"
        val message = "EMERGENCY: KALI Safety SOS triggered! I need help. Last known location: $address\nMap: $locationLink"

        try {
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            
            for (contact in cachedContacts) {
                val phone = contact.phone
                if (!phone.isNullOrBlank()) {
                    smsManager.sendTextMessage(phone, null, message, null, null)
                }
            }
            Toast.makeText(this, "Emergency SMS failover dispatched to contacts.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send emergency SMS: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        @Suppress("DEPRECATION")
        val networkInfo = connectivityManager.activeNetworkInfo
        @Suppress("DEPRECATION")
        return networkInfo != null && networkInfo.isConnected
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────────

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        if (binding.switchScreamGuard.isChecked) {
            startScreamGuard()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        stopScreamGuard()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        sosTimerHandler?.removeCallbacksAndMessages(null)
        ringtone?.stop()
        fakeCallTimerHandler?.removeCallbacksAndMessages(null)
        stopScreamGuard()
        super.onDestroy()
    }
}

