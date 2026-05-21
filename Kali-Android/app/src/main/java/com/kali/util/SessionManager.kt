package com.kali.util

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("kali_prefs", Context.MODE_PRIVATE)

    fun saveSession(token: String, role: String, name: String) {
        prefs.edit().putString("token", token).putString("role", role).putString("name", name).apply()
    }

    fun getToken(): String? = prefs.getString("token", null)
    fun getRole(): String? = prefs.getString("role", null)
    fun getName(): String? = prefs.getString("name", null)
    fun isLoggedIn() = getToken() != null

    fun clear() = prefs.edit().clear().apply()
}
