package com.kali.ui.sos

import android.app.Application
import android.content.Intent
import androidx.lifecycle.*
import com.kali.network.RetrofitClient
import com.kali.repository.KaliRepository
import com.kali.service.LocationService
import com.kali.service.RecordingService
import kotlinx.coroutines.launch

class SosViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = KaliRepository(app, RetrofitClient.api)

    private val _sosActive = MutableLiveData(false)
    val sosActive: LiveData<Boolean> = _sosActive

    private val _status = MutableLiveData("Ready")
    val status: LiveData<String> = _status

    var activeAlertLocalId: Int = -1

    fun triggerSOS(lat: Double?, lng: Double?) {
        viewModelScope.launch {
            _status.postValue("SOS Activated – Recording…")
            activeAlertLocalId = repo.triggerSOS(lat, lng).toInt()
            _sosActive.postValue(true)
            startServices(activeAlertLocalId)
        }
    }

    fun cancelSOS() {
        _sosActive.postValue(false)
        _status.postValue("Ready")
        stopServices()
    }

    private fun startServices(alertLocalId: Int) {
        val ctx = getApplication<Application>()
        ctx.startService(Intent(ctx, LocationService::class.java).putExtra("alertLocalId", alertLocalId))
        ctx.startService(Intent(ctx, RecordingService::class.java).putExtra("alertLocalId", alertLocalId))
    }

    private fun stopServices() {
        val ctx = getApplication<Application>()
        ctx.stopService(Intent(ctx, LocationService::class.java))
        ctx.stopService(Intent(ctx, RecordingService::class.java))
    }
}
