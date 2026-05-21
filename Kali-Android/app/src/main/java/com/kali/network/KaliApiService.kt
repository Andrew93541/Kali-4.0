package com.kali.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String, val role: String, val name: String)
data class AlertRequest(val latitude: Double?, val longitude: Double?)
data class AlertResponse(val alert_id: Int, val status: String)
data class LocationRequest(val alert_id: Int, val latitude: Double, val longitude: Double)
data class RegisterRequest(val name: String, val phone: String, val email: String, val password: String, val role: String)
data class GoogleLoginRequest(val email: String, val name: String, val googleId: String?)

interface KaliApiService {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<Unit>

    @POST("auth/google")
    suspend fun googleLogin(@Body body: GoogleLoginRequest): Response<LoginResponse>

    @POST("alerts")
    suspend fun createAlert(
        @Header("Authorization") token: String,
        @Body body: AlertRequest
    ): Response<AlertResponse>

    @POST("locations")
    suspend fun addLocation(
        @Header("Authorization") token: String,
        @Body body: LocationRequest
    ): Response<Unit>

    @Multipart
    @POST("media")
    suspend fun uploadMedia(
        @Header("Authorization") token: String,
        @Part("alert_id") alertId: RequestBody,
        @Part("media_type") mediaType: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<Unit>
}
