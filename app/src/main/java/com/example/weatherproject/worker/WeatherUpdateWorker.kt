package com.example.weatherproject.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherproject.data.repository.WeatherRepository
import com.example.weatherproject.util.ClothingRecommender
import com.example.weatherproject.util.NotificationHelper
import com.example.weatherproject.util.PreferenceManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WeatherUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepository,
    private val preferenceManager: PreferenceManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("WeatherUpdateWorker", "Work started.")

        try {
            var lat: Double?
            var lon: Double?

            // 1. 먼저 Preferences에서 마지막 위치 정보 가져오기 시도
            val prefWeather = preferenceManager.getWeatherState()
            if (prefWeather?.latitude != null && prefWeather.longitude != null) {
                lat = prefWeather.latitude
                lon = prefWeather.longitude
                Log.d("WeatherUpdateWorker", "Location loaded from Preferences.")
            } else {
                // 2. 실패 시 Room DB 캐시에서 가져오기 시도
                val cachedWeather = weatherRepository.getCachedWeather()
                if (cachedWeather?.latitude != null && cachedWeather.longitude != null) {
                    lat = cachedWeather.latitude
                    lon = cachedWeather.longitude
                    preferenceManager.saveWeatherState(cachedWeather) // 다음을 위해 Preferences에 저장
                    Log.d("WeatherUpdateWorker", "Location restored from Room cache.")
                } else {
                    // 두 방법 모두 실패 시 작업 종료
                    Log.e("WeatherUpdateWorker", "Could not find any location coordinates. Failing work.")
                    return Result.failure()
                }
            }

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
                context,
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
