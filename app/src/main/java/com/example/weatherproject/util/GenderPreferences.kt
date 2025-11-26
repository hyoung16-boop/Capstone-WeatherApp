package com.example.weatherproject.util

import android.content.Context
import android.content.SharedPreferences

object GenderPreferences {
    private const val PREF_NAME = "weather_app_prefs"
    private const val KEY_IS_GENDER_SET = "is_gender_set"
    private const val KEY_GENDER_OFFSET = "gender_offset"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isGenderSet(context: Context): Boolean {
        return getPrefs(context).contains(KEY_IS_GENDER_SET)
    }

    fun getGenderOffset(context: Context): Double {
        // Default to 0.0 if not set
        return getPrefs(context).getFloat(KEY_GENDER_OFFSET, 0f).toDouble()
    }

    fun setGenderOffset(context: Context, offset: Double) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_IS_GENDER_SET, true)
            putFloat(KEY_GENDER_OFFSET, offset.toFloat())
            apply()
        }
    }
}
