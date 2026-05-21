package com.kali.ui.sos

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kali.databinding.ActivitySosBinding
import com.kali.service.SyncWorker

class SosActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySosBinding
    private val viewModel: SosViewModel by viewModels()
    private lateinit var fusedLocation: FusedLocationProviderClient

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        if (perms.values.all { it }) activateSOS()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocation = LocationServices.getFusedLocationProviderClient(this)

        binding.btnSos.setOnClickListener { checkPermissionsAndSOS() }
        binding.btnCancel.setOnClickListener { viewModel.cancelSOS() }

        viewModel.sosActive.observe(this) { active ->
            binding.btnSos.isEnabled = !active
            binding.btnCancel.isEnabled = active
            binding.btnSos.alpha = if (active) 0.4f else 1f
        }
        viewModel.status.observe(this) { binding.tvStatus.text = it }

        SyncWorker.enqueue(this)
    }

    private fun checkPermissionsAndSOS() {
        val needed = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO)
        val missing = needed.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isEmpty()) activateSOS() else permissionLauncher.launch(missing.toTypedArray())
    }

    private fun activateSOS() {
        try {
            fusedLocation.lastLocation.addOnSuccessListener { loc: Location? ->
                viewModel.triggerSOS(loc?.latitude, loc?.longitude)
            }
        } catch (e: SecurityException) {
            viewModel.triggerSOS(null, null)
        }
    }
}
