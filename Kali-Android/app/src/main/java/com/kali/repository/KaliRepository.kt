package com.kali.repository

import android.content.Context
import com.kali.model.*
import com.kali.network.KaliApiService
import com.kali.util.SessionManager

class KaliRepository(private val context: Context, private val api: KaliApiService) {

    private val db = KaliDatabase.get(context)
    private val session = SessionManager(context)

    suspend fun triggerSOS(lat: Double?, lng: Double?): Long {
        return db.alertDao().insert(AlertEntity(latitude = lat, longitude = lng))
    }

    suspend fun addLocation(alertLocalId: Int, lat: Double, lng: Double) {
        db.locationDao().insert(
            LocationEntity(alertLocalId = alertLocalId, latitude = lat, longitude = lng)
        )
    }

    suspend fun addMedia(alertLocalId: Int, filePath: String, mediaType: String) {
        db.mediaDao().insert(
            MediaEntity(alertLocalId = alertLocalId, filePath = filePath, mediaType = mediaType)
        )
    }

    fun getToken() = "Bearer ${session.getToken()}"
}
