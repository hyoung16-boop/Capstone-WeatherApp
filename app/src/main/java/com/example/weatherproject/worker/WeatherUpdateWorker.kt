package com.example.weatherproject.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherproject.di.WorkerEntryPoint
import com.example.weatherproject.util.ClothingRecommender
import com.example.weatherproject.util.NotificationHelper
import dagger.hilt.android.EntryPointAccessors

class WeatherUpdateWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("WeatherUpdateWorker", "Work started using EntryPoint.")

        // Hilt의 생성자 주입 대신 EntryPoint를 통해 직접 의존성을 가져옴
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            appContext,
            WorkerEntryPoint::class.java
        )
        val weatherRepository = hiltEntryPoint.weatherRepository()
        val preferenceManager = hiltEntryPoint.preferenceManager()
        val locationProvider = hiltEntryPoint.locationProvider()

        try {
            // 위치 정보 가져오기 (1. Preference -> 2. Room -> 3. LocationProvider)
            val location = preferenceManager.getWeatherState()?.let { state ->
                if (state.latitude != null && state.longitude != null) state.latitude to state.longitude else null
            } ?: weatherRepository.getCachedWeather()?.let { state ->
                if (state.latitude != null && state.longitude != null) {
                    preferenceManager.saveWeatherState(state) // 다음을 위해 Preferences에 저장
                    state.latitude to state.longitude
                } else null
            } ?: locationProvider.getFreshLocation()?.let { loc -> // 새로 만든 함수 호출
                loc.latitude to loc.longitude
            }

            if (location == null) {
                Log.e("WeatherUpdateWorker", "Could not find any location coordinates. Failing work.")
                return Result.failure()
            }
            val (lat, lon) = location

            // --- 1. 최신 날씨 정보 가져오기 ---
            val tempAdjustment = preferenceManager.getTempAdjustment()
            val weatherDataResult = weatherRepository.getWeatherData(lat, lon, tempAdjustment)

            if (weatherDataResult.isFailure) {
                Log.e("WeatherUpdateWorker", "Failed to get fresh weather data from repository.")
                return Result.failure()
            }

            val freshWeatherState = weatherDataResult.getOrThrow()
            // 워커가 최신 데이터를 사용하도록 Preference를 업데이트
            preferenceManager.saveWeatherState(freshWeatherState)

            val weather = freshWeatherState.currentWeather
            val hourlyForecast = freshWeatherState.hourlyForecast
            val weatherDetails = freshWeatherState.weatherDetails

            // --- 2. 알림 내용 생성 ---
            val clothingRecommendation = ClothingRecommender.getRecommendation(weather.feelsLike.replace("°", "").toIntOrNull() ?: 20).first
            val rainForecast = hourlyForecast.take(3).find { it.pty != "0" }
            val rainText = if (rainForecast != null) "• 3시간 내에 비/눈 소식이 있어요. ☔️" else null

            val pm10Value = weatherDetails.pm10
            val pm10Status = if (pm10Value != "정보없음") {
                val rawValue = pm10Value.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
                when {
                    rawValue <= 30 -> "좋음"
                    rawValue <= 80 -> "보통"
                    rawValue <= 150 -> "나쁨"
                    else -> "매우 나쁨"
                }
            } else {
                "정보 없음"
            }
            val pm10Text = "• 미세먼지: $pm10Status"

            val notificationContent = buildString {
                append("현재 기온은 ${weather.temperature}이며, 하늘은 ${weather.description} 상태입니다.\n")
                append("\n$clothingRecommendation\n\n")
                append(pm10Text)
                rainText?.let { append("\n$it") }
            }

            // --- 3. 알림 표시 ---
            NotificationHelper.showNotification(
                appContext,
                "오늘의 날씨 브리핑",
                notificationContent
            )
            Log.d("WeatherUpdateWorker", "Notification shown successfully.")

            return Result.success()

        } catch (e: Exception) {
            Log.e("WeatherUpdateWorker", "Error during doWork", e)
            return Result.failure()
        }
    }
}