package com.example.weatherproject.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherproject.data.repository.WeatherRepository
import com.example.weatherproject.util.LocationProvider
import com.example.weatherproject.util.NotificationHelper
import com.example.weatherproject.util.PreferenceManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@HiltWorker
class SmartAlertWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepository,
    private val prefManager: PreferenceManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            // 마지막으로 저장된 날씨 정보에서 위도, 경도 가져오기
            val lastWeather = prefManager.getWeatherState()
            val lat = lastWeather?.latitude
            val lon = lastWeather?.longitude

            if (lat == null || lon == null) {
                // 위치 정보가 없으면 작업을 중단하고 실패 처리
                return Result.failure()
            }
            
            // 날씨 데이터 가져오기
            val weatherDataResult = weatherRepository.getWeatherData(lat, lon, 0)
            if (weatherDataResult.isFailure) {
                return Result.failure()
            }

            val hourlyForecasts = weatherDataResult.getOrThrow().hourlyForecast
            
            // pty(강수형태) 코드가 "0"(없음)이 아니면 비나 눈이 오는 것으로 간주
            val willRainOrSnow = hourlyForecasts.take(3).any { it.pty != "0" }

            if (willRainOrSnow) {
                // 이전에 알림을 보냈는지, 보냈다면 너무 자주 보내지 않는지 확인하는 로직 추가 (생략)
                NotificationHelper.showNotification(
                    context = applicationContext,
                    title = "스마트 날씨 알림",
                    content = "3시간 내에 비 또는 눈 소식이 있습니다. 우산을 챙기세요!"
                )
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}