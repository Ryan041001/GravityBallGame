package com.example.gravityballgame.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/**
 * 用户会话管理器，处理用户登录状态
 */
class UserSessionManager(context: Context) {
    
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    
    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    fun saveUserLoginSession(userId: Long, username: String) {
        val editor = preferences.edit()
        editor.putLong(KEY_USER_ID, userId)
        editor.putString(KEY_USERNAME, username)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }
    
    fun getUserId(): Long {
        return preferences.getLong(KEY_USER_ID, -1)
    }
    
    fun getUsername(): String? {
        return preferences.getString(KEY_USERNAME, null)
    }
    
    fun isLoggedIn(): Boolean {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun logout() {
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }
}
