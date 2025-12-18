package com.example.weatherproject.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherproject.di.WorkerEntryPoint
import com.example.weatherproject.util.ClothingRecommender
import com.example.weatherproject.util.NotificationHelper
import com.example.weatherproject.util.PmStatusHelper
import com.example.weatherproject.util.WeatherSummarizer
import dagger.hilt.android.EntryPointAccessors

/**
 * 백그라운드에서 주기적으로(또는 알람 시간에 맞춰) 실행되어 날씨를 업데이트하고 알림을 보내는 워커입니다.
 *
 * 주요 역할:
 * 1. 현재 위치를 파악합니다 (저장된 위치 또는 실시간 GPS).
 * 2. 최신 날씨 데이터를 서버에서 가져옵니다.
 * 3. 날씨 정보, 미세먼지 상태, 옷차림 추천 등을 포함한 종합 브리핑 알림을 생성하여 사용자에게 발송합니다.
 *
 * Hilt의 `EntryPointAccessors`를 사용하여 워커 내부에서 의존성을 주입받습니다.
 */
class WeatherUpdateWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    /**
     * 워커의 실행 진입점입니다.
     */
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

            // 옷차림 추천 가져오기
            val clothingItemIds = ClothingRecommender.getRecommendation(weather.feelsLike.replace("°", "").toIntOrNull() ?: 20)
            val clothingItems = clothingItemIds.map { applicationContext.getString(it) }
            val clothingRecommendation = "추천 옷차림: " + clothingItems.joinToString(", ")
            
            // 강수 예보 분석 (향후 3시간)
            val threeHourForecast = hourlyForecast.take(3)
            val willRain = threeHourForecast.any { it.pty == "1" || it.pty == "4" }
            val willSnow = threeHourForecast.any { it.pty == "2" || it.pty == "3" }
            
            val rainText = when {
                willRain && willSnow -> "• 3시간 내에 비 또는 눈 소식이 있어요. ☔❄️"
                willRain -> "• 3시간 내에 비 소식이 있어요. 우산을 챙기세요 ☔"
                willSnow -> "• 3시간 내에 눈 소식이 있어요. 미끄럼 주의하세요 ❄️"
                else -> null
            }

            val pm10Value = weatherDetails.pm10
            val pm10Status = PmStatusHelper.getStatus(pm10Value)
            val pm10Text = "😷 미세먼지: $pm10Status"

            val mainWeatherSummary = WeatherSummarizer.getSummary(weather, weatherDetails, hourlyForecast)

            // 알림 내용 구성
            val notificationContent = buildString {
                // 1. 현재 날씨 팩트 정보 (기온, 상태, 체감)
                append("🌡️ ${weather.temperature} (체감 ${weather.feelsLike})\n")
                append("SKY: ${weather.description}\n")
                append("$pm10Text\n\n")

                // 2. 날씨 요약 (조언)
                append("$mainWeatherSummary\n")
                
                // 3. 강수 예보 (있을 경우만)
                rainText?.let { append("$it\n") }

                // 4. 옷차림 추천
                append("\n$clothingRecommendation")
            }

            // --- 3. 알림 표시 ---
            Log.d("WeatherUpdateWorker", "Generated Notification Content:\n$notificationContent")
            
            NotificationHelper.showNotification(
                appContext,
                "현재 날씨 브리핑", // 제목 변경
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