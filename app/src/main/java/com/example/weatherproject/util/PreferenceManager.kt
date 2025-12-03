package com.example.weatherproject.util

import android.content.Context
import android.content.SharedPreferences
import com.example.weatherproject.data.WeatherState
import com.google.gson.Gson

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("weather_app_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_TEMP_ADJUSTMENT = "temp_adjustment"
        private const val KEY_IS_SETUP_COMPLETE = "is_setup_complete"
        private const val KEY_LAST_WEATHER_STATE = "last_weather_state"
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

    // 마지막 날씨 정보 저장
    fun saveWeatherState(weatherState: WeatherState) {
        val json = gson.toJson(weatherState)
        prefs.edit().putString(KEY_LAST_WEATHER_STATE, json).apply()
    }

    // 마지막 날씨 정보 불러오기
    fun getWeatherState(): WeatherState? {
        val json = prefs.getString(KEY_LAST_WEATHER_STATE, null)
        return gson.fromJson(json, WeatherState::class.java)
    }
}
