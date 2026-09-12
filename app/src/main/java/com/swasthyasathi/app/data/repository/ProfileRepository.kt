package com.swasthyasathi.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.swasthyasathi.app.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("swasthya_sathi_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _profileFlow = MutableStateFlow(loadProfile())
    val profileFlow: StateFlow<UserProfile> = _profileFlow.asStateFlow()

    private fun loadProfile(): UserProfile {
        val json = prefs.getString("user_profile", null)
        return if (json != null) {
            try {
                gson.fromJson(json, UserProfile::class.java)
            } catch (e: Exception) {
                UserProfile()
            }
        } else {
            UserProfile()
        }
    }

    fun saveProfile(profile: UserProfile) {
        prefs.edit().putString("user_profile", gson.toJson(profile)).apply()
        _profileFlow.value = profile
    }

    fun resetProfile(): UserProfile {
        val defaultProfile = UserProfile()
        saveProfile(defaultProfile)
        return defaultProfile
    }
}
