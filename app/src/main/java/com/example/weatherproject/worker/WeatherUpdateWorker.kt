package com.example.weatherproject.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherproject.network.RetrofitClient
import com.example.weatherproject.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("WeatherUpdateWorker", "Work started.")
        
        try {
            val preferenceManager = PreferenceManager(context)
            val lastWeatherState = preferenceManager.getWeatherState()

            // 위치 정보가 없으면 작업을 중단
            val lat = lastWeatherState?.latitude
            val lon = lastWeatherState?.longitude
            if (lat == null || lon == null) {
                Log.e("WeatherUpdateWorker", "Latitude or Longitude is null. Cannot fetch weather.")
                return Result.failure()
            }

            // --- 1. 최신 날씨 정보 가져오기 ---
            val (nx, ny) = GpsTransfer.convertToGrid(lat, lon)
            val currentResponse = withContext(Dispatchers.IO) { RetrofitClient.weatherApi.getCurrentWeather(nx, ny) }
            val hourlyResponse = withContext(Dispatchers.IO) { RetrofitClient.weatherApi.getHourlyForecast(nx, ny) }
            val weather = currentResponse.weather

            if (weather == null) {
                Log.e("WeatherUpdateWorker", "Failed to get current weather from API.")
                return Result.failure()
            }
            
            // --- 2. 알림 내용 생성 ---
            val tempAdjustment = preferenceManager.getTempAdjustment()

            // 2-1. 체감온도 및 옷차림 추천
            val temp = weather.temp ?: 0.0
            val humidity = weather.humidity ?: 0.0
            val windSpeedMs = weather.windSpeed ?: 0.0
            val windSpeedKmh = windSpeedMs * 3.6

            val calculatedFeelsLike = FeelsLikeTempCalculator.calculate(temp, humidity, windSpeedKmh)
            val finalFeelsLike = calculatedFeelsLike + tempAdjustment
            val clothingRecommendation = ClothingRecommender.getRecommendation(finalFeelsLike.toInt(), tempAdjustment).first

            // 2-2. 3시간 내 강수 예보
            val rainForecast = hourlyResponse.weather?.take(3)?.find { it.pty.contains("비") || it.pty.contains("소나기") }
            val rainText = if (rainForecast != null) "• 3시간 내에 비 소식이 있을 수 있어요. ☔️" else null
            
            // 2-3. 미세먼지
            val pm10Value = weather.pm10
            val pm10Status = if (pm10Value != null) {
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
            
            // 2-4. 최종 알림 콘텐츠 조합
            val notificationContent = buildString {
                append("현재 기온은 ${temp.toInt()}°이며, 하늘은 ${weather.skyCondition ?: "정보 없음"} 상태입니다.\n")
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
            Log.d("WeatherUpdateWorker", "Smart Notification shown successfully.")
            
            // --- 4. 새 데이터로 캐시 업데이트 (선택적이지만 좋은 습관) ---
            // 이 부분은 MainViewModel의 updateUiWithServerData와 유사한 로직이 필요.
            // 단순화를 위해 여기서는 생략하고, 알림 표시에 집중.

            return Result.success()

        } catch (e: Exception) {
            Log.e("WeatherUpdateWorker", "Error during doWork", e)
            return Result.failure()
        }
    }
}
