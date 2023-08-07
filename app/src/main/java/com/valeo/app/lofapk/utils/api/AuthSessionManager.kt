package com.valeo.app.lofapk.utils.api

import android.content.Context
import android.content.SharedPreferences
import com.valeo.app.lofapk.R

class AuthSessionManager(context: Context) {

    private var prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val EXPIRED_TOKEN = "expires_in"
    }

    /**
     * Function to save auth token
     */
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun saveExpiresIn(millis: Long) {
        val editor = prefs.edit()
        editor.putLong(EXPIRED_TOKEN, millis)
        editor.apply()
    }

    /**
     * Function to fetch auth token
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun fetchExpiresIn(): Long? {
        return prefs.getLong(EXPIRED_TOKEN, 0)
    }
}