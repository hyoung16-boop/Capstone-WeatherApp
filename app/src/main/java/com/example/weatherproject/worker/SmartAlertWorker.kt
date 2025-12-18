package com.example.weatherproject.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherproject.data.repository.WeatherRepository
import com.example.weatherproject.util.NotificationHelper
import com.example.weatherproject.util.PreferenceManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SmartAlertWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepository,
    private val preferenceManager: PreferenceManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            var lat: Double?
            var lon: Double?

            // 1. 먼저 Preferences에서 마지막 위치 정보 가져오기 시도
            val prefWeather = preferenceManager.getWeatherState()
            if (prefWeather?.latitude != null && prefWeather.longitude != null) {
                lat = prefWeather.latitude
                lon = prefWeather.longitude
            } else {
                // 2. 실패 시 Room DB 캐시에서 가져오기 시도
                val cachedWeather = weatherRepository.getCachedWeather()
                if (cachedWeather?.latitude != null && cachedWeather.longitude != null) {
                    lat = cachedWeather.latitude
                    lon = cachedWeather.longitude
                    preferenceManager.saveWeatherState(cachedWeather) // 다음을 위해 Preferences에 저장
                } else {
                    // 두 방법 모두 실패 시 작업 종료
                    return Result.failure()
                }
            }

            // 날씨 데이터 가져오기
            val weatherDataResult = weatherRepository.getWeatherData(lat, lon, 0)
            if (weatherDataResult.isFailure) {
                return Result.failure()
            }
            
            val freshWeatherState = weatherDataResult.getOrThrow()
            // 워커가 최신 데이터를 사용하도록 Preference를 업데이트
            preferenceManager.saveWeatherState(freshWeatherState)

            val hourlyForecasts = freshWeatherState.hourlyForecast
            
            val threeHourForecast = hourlyForecasts.take(3)
            
            // PTY 코드: 1=비, 2=비/눈, 3=눈, 4=소나기
            val willRain = threeHourForecast.any { it.pty == "1" || it.pty == "4" }
            val willSnow = threeHourForecast.any { it.pty == "2" || it.pty == "3" }

            if (willRain || willSnow) {
                val lastAlertTime = preferenceManager.getLastAlertTime()
                val currentTime = System.currentTimeMillis()
                val minInterval = 6 * 60 * 60 * 1000 // 6시간

                if (currentTime - lastAlertTime >= minInterval) {
                    val message = when {
                        willRain && willSnow -> "3시간 내에 비 또는 눈 소식이 있습니다. 우산과 따뜻한 옷차림을 준비하세요! ☔❄️"
                        willRain -> "3시간 내에 비 소식이 있습니다. 우산을 챙기세요! ☔"
                        willSnow -> "3시간 내에 눈 소식이 있습니다. 미끄럼 주의하시고 따뜻하게 입으세요! ❄️"
                        else -> "기상 변화가 예상됩니다. 날씨를 확인해주세요."
                    }

                    NotificationHelper.showNotification(
                        context = applicationContext,
                        title = "스마트 날씨 알림",
                        content = message
                    )
                    preferenceManager.saveLastAlertTime(currentTime)
                    Log.d("SmartAlertWorker", "Alert sent: $message")
                } else {
                    Log.d("SmartAlertWorker", "Alert skipped. Too soon since last alert.")
                }
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}