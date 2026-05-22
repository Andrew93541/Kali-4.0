package com.kali.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

// ── Auth DTOs ─────────────────────────────────────────────────────────────────
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String, val role: String, val name: String, val phone: String?, val userId: Int)
data class AlertRequest(val latitude: Double?, val longitude: Double?)
data class AlertResponse(val alert_id: Int, val status: String)
data class LocationRequest(val alert_id: Int, val latitude: Double, val longitude: Double)
data class RegisterRequest(val name: String, val phone: String, val email: String, val password: String, val role: String)
data class GoogleLoginRequest(val email: String, val name: String, val googleId: String?, val role: String = "User")

// ── Profile DTOs ──────────────────────────────────────────────────────────────
data class ProfileResponse(
    val id: Int, val name: String, val email: String, val phone: String?,
    val role: String, val guardian_id: Int?
)
data class ProfileUpdateRequest(val name: String? = null, val phone: String? = null, val fcm_token: String? = null)

// ── Alert DTOs ────────────────────────────────────────────────────────────────
data class Alert(
    val id: Int,
    val user_id: Int,
    val name: String?,
    val phone: String?,
    val timestamp: String?,
    val status: String?
)

data class TelemetryStats(
    val totalDistance: Double,
    val avgSpeed: Double,
    val maxSpeed: Double,
    val duration: Double,
    val pointCount: Int,
    val lastBearing: Double
)

data class HistoricalLocation(
    val id: Int,
    val alert_id: Int,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String?
)

// ── Contacts DTOs ─────────────────────────────────────────────────────────────
data class EmergencyContact(
    val id: Int,
    val user_id: Int,
    val name: String,
    val phone: String,
    val relation: String?,
    val is_primary: Int
)

data class EmergencyContactRequest(
    val name: String,
    val phone: String,
    val relation: String?,
    val is_primary: Boolean
)

// ── Guardian DTOs ─────────────────────────────────────────────────────────────
data class LinkedUser(val id: Int, val name: String, val email: String, val phone: String?)
data class GuardianUserResponse(val linkedUser: LinkedUser?)
data class GuardianLinkRequest(val userEmail: String? = null, val userPhone: String? = null)
data class GuardianLinkResponse(val success: Boolean, val linkedUser: LinkedUser?)

// ── Resolve DTO ───────────────────────────────────────────────────────────────
data class ResolveRequest(val note: String? = null)

interface KaliApiService {
    // Auth
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<Unit>

    @POST("auth/google")
    suspend fun googleLogin(@Body body: GoogleLoginRequest): Response<LoginResponse>

    @GET("auth/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ProfileResponse>

    @PUT("auth/profile")
    suspend fun updateProfile(@Header("Authorization") token: String, @Body body: ProfileUpdateRequest): Response<ProfileResponse>

    // Alerts
    @POST("alerts")
    suspend fun createAlert(@Header("Authorization") token: String, @Body body: AlertRequest): Response<AlertResponse>

    @GET("alerts")
    suspend fun getAlerts(@Header("Authorization") token: String): Response<List<Alert>>

    @GET("alerts/my")
    suspend fun getMyAlerts(@Header("Authorization") token: String): Response<List<Alert>>

    @POST("alerts/{id}/resolve")
    suspend fun resolveAlert(@Header("Authorization") token: String, @Path("id") alertId: Int, @Body body: ResolveRequest = ResolveRequest()): Response<Unit>

    // Locations
    @POST("locations")
    suspend fun addLocation(@Header("Authorization") token: String, @Body body: LocationRequest): Response<Unit>

    @GET("locations/{alert_id}")
    suspend fun getLocations(@Header("Authorization") token: String, @Path("alert_id") alertId: Int): Response<List<HistoricalLocation>>

    @GET("locations/{alert_id}/stats")
    suspend fun getLocationStats(@Header("Authorization") token: String, @Path("alert_id") alertId: Int): Response<TelemetryStats>

    // Media
    @Multipart
    @POST("media")
    suspend fun uploadMedia(
        @Header("Authorization") token: String,
        @Part("alert_id") alertId: RequestBody,
        @Part("media_type") mediaType: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<Unit>

    // Emergency Contacts
    @GET("contacts")
    suspend fun getContacts(@Header("Authorization") token: String): Response<List<EmergencyContact>>

    @POST("contacts")
    suspend fun addContact(@Header("Authorization") token: String, @Body body: EmergencyContactRequest): Response<EmergencyContact>

    @DELETE("contacts/{id}")
    suspend fun deleteContact(@Header("Authorization") token: String, @Path("id") contactId: Int): Response<Unit>

    // Guardian
    @GET("guardian/my-user")
    suspend fun getMyUser(@Header("Authorization") token: String): Response<GuardianUserResponse>

    @POST("guardian/link")
    suspend fun linkGuardian(@Header("Authorization") token: String, @Body body: GuardianLinkRequest): Response<GuardianLinkResponse>

    @DELETE("guardian/unlink")
    suspend fun unlinkGuardian(@Header("Authorization") token: String): Response<Unit>

    @GET("guardian/active-alert")
    suspend fun getGuardianActiveAlert(@Header("Authorization") token: String): Response<Map<String, Any?>>
}
