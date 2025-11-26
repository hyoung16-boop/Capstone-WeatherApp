package com.example.weatherproject.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("weather_app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TEMP_ADJUSTMENT = "temp_adjustment"
        private const val KEY_IS_SETUP_COMPLETE = "is_setup_complete"
    }

    // 보정값 저장 (-3 ~ +3)
    fun setTempAdjustment(value: Int) {
        prefs.edit().putInt(KEY_TEMP_ADJUSTMENT, value).apply()
        prefs.edit().putBoolean(KEY_IS_SETUP_COMPLETE, true).apply()
    }

    // 보정값 가져오기 (기본값 0)
    fun getTempAdjustment(): Int {
        return prefs.getInt(KEY_TEMP_ADJUSTMENT, 0)
    }

    // 최초 설정 완료 여부 확인
    fun isSetupComplete(): Boolean {
        return prefs.getBoolean(KEY_IS_SETUP_COMPLETE, false)
    }
}
