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
            val lastWeatherState = preferenceManager.getWeatherState()
            val lat = lastWeatherState?.latitude
            val lon = lastWeatherState?.longitude
            if (lat == null || lon == null) {
                Log.e("WeatherUpdateWorker", "Latitude or Longitude is null. Cannot fetch weather.")
                return Result.failure()
            }

            val tempAdjustment = preferenceManager.getTempAdjustment()
            val weatherDataResult = weatherRepository.getWeatherData(lat, lon, tempAdjustment)

            if (weatherDataResult.isFailure) {
                Log.e("WeatherUpdateWorker", "Failed to get weather data from repository.")
                return Result.failure()
            }

            val weatherState = weatherDataResult.getOrThrow()
            val weather = weatherState.currentWeather
            val hourlyForecast = weatherState.hourlyForecast

            // --- 2. 알림 내용 생성 ---
            val clothingRecommendation = ClothingRecommender.getRecommendation(weather.feelsLike.replace("°", "").toIntOrNull() ?: 20).first
            val rainForecast = hourlyForecast.take(3).find { it.pty != "0" }
            val rainText = if (rainForecast != null) "• 3시간 내에 비/눈 소식이 있어요. ☔️" else null

            val pm10Value = weatherState.weatherDetails.pm10
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
