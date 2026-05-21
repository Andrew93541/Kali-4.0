package com.kali.service

import android.content.Context
import androidx.work.*
import com.kali.model.KaliDatabase
import com.kali.network.AlertRequest
import com.kali.network.LocationRequest
import com.kali.network.RetrofitClient
import com.kali.util.SessionManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    private val db = KaliDatabase.get(appContext)
    private val api = RetrofitClient.api
    private val token = "Bearer ${SessionManager(appContext).getToken()}"

    override suspend fun doWork(): Result {
        return try {
            syncAlerts()
            syncLocations()
            syncMedia()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun syncAlerts() {
        db.alertDao().getPending().forEach { alert ->
            val resp = api.createAlert(token, AlertRequest(alert.latitude, alert.longitude))
            if (resp.isSuccessful) {
                resp.body()?.let { db.alertDao().markSynced(alert.localId, it.alert_id) }
            }
        }
    }

    private suspend fun syncLocations() {
        val allAlerts = db.alertDao().getAll()
        db.locationDao().getPending().forEach { loc ->
            val serverId = allAlerts.find { it.localId == loc.alertLocalId }?.serverId ?: return@forEach
            val resp = api.addLocation(token, LocationRequest(serverId, loc.latitude, loc.longitude))
            if (resp.isSuccessful) db.locationDao().markSynced(loc.id)
        }
    }

    private suspend fun syncMedia() {
        val allAlerts = db.alertDao().getAll()
        db.mediaDao().getPending().forEach { media ->
            val serverId = allAlerts.find { it.localId == media.alertLocalId }?.serverId ?: return@forEach
            val file = File(media.filePath)
            if (!file.exists()) return@forEach
            val part = MultipartBody.Part.createFormData(
                "file", file.name,
                file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            )
            val alertIdBody = serverId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val typeBody = media.mediaType.toRequestBody("text/plain".toMediaTypeOrNull())
            val resp = api.uploadMedia(token, alertIdBody, typeBody, part)
            if (resp.isSuccessful) db.mediaDao().markSynced(media.id)
        }
    }

    companion object {
        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork("kali_sync", ExistingWorkPolicy.KEEP, request)
        }
    }
}
