package com.example.hoteru.viewModel

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("HoteruAppPrefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_EMAIL = "user_email"
    }

    fun saveSession(email: String) {
        prefs.edit().putString(USER_EMAIL, email).apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
