package com.kali.network

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var cachedIp: String = "192.168.0.151" // Pre-filled with host Wi-Fi IP for direct physical phone connection
    private var cachedPort: String = "3000"
    
    private var apiInstance: KaliApiService? = null

    // Configure a production-grade resilient OkHttpClient
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences("server_settings", Context.MODE_PRIVATE)
        if (!prefs.contains("server_ip")) {
            prefs.edit().putString("server_ip", "192.168.0.151").apply()
        }
        val ip = prefs.getString("server_ip", "192.168.0.151") ?: "192.168.0.151"
        val port = prefs.getString("server_port", "3000") ?: "3000"
        
        updateBaseUrl(ip, port)
    }

    fun updateBaseUrl(ip: String, port: String) {
        cachedIp = ip
        cachedPort = port
        val url = "http://$ip:$port/api/"
        apiInstance = Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KaliApiService::class.java)
    }

    val api: KaliApiService
        get() {
            if (apiInstance == null) {
                // Fallback default
                updateBaseUrl("192.168.0.151", "3000")
            }
            return apiInstance!!
        }
}

