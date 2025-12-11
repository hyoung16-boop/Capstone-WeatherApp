package com.example.weatherproject.data.repository

import android.util.Log
import com.example.weatherproject.data.CurrentWeather
import com.example.weatherproject.data.HourlyForecast
import com.example.weatherproject.data.WeatherDetails
import com.example.weatherproject.data.WeatherState
import com.example.weatherproject.data.WeeklyForecast
import com.example.weatherproject.network.WeatherApiService
import com.example.weatherproject.network.CurrentWeatherResponse
import com.example.weatherproject.network.HourlyForecastResponse
import com.example.weatherproject.network.WeeklyForecastResponse
import com.example.weatherproject.util.FeelsLikeTempCalculator
import com.example.weatherproject.util.GpsTransfer
import com.example.weatherproject.util.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * WeatherRepository의 실제 구현체.
 * 네트워크, 로컬 캐시 등 구체적인 데이터 소스를 다룹니다.
 */
class WeatherRepositoryImpl(
    private val weatherApi: WeatherApiService,
    private val preferenceManager: PreferenceManager
) : WeatherRepository {

    override suspend fun getWeatherData(
        lat: Double,
        lon: Double,
        tempAdjustment: Int
    ): Result<WeatherState> = withContext(Dispatchers.IO) {
        try {
            // 1. GPS 좌표 → 격자 좌표 변환
            val (nx, ny) = GpsTransfer.convertToGrid(lat, lon)
            Log.d("WeatherRepository", "GPS($lat, $lon) → Grid($nx, $ny)")

            // 2. API 병렬 호출 (네트워크 효율을 위해 실제로는 순차 호출되지만,
            // Coroutine의 suspend 함수 덕분에 메인 스레드를 막지 않음)
            val currentResponse = weatherApi.getCurrentWeather(nx, ny)
            val hourlyResponse = weatherApi.getHourlyForecast(nx, ny)
            val weeklyResponse = weatherApi.getWeeklyForecast(nx, ny)

            // 3. API 응답을 UI에서 사용할 WeatherState 객체로 변환
            val weatherState = mapResponseToWeatherState(
                currentData = currentResponse,
                hourlyData = hourlyResponse,
                weeklyData = weeklyResponse,
                tempAdjustment = tempAdjustment
            )

            // 4. 성공 시, 새로운 날씨 정보를 캐시에 저장
            preferenceManager.saveWeatherState(weatherState)
            
            // 5. 성공 결과를 Result 객체에 담아 반환
            Result.success(weatherState)

        } catch (e: Exception) {
            Log.e("WeatherRepository", "API 호출 실패: ${e.message}", e)
            // 6. 실패 시, 에러를 Result 객체에 담아 반환
            Result.failure(e)
        }
    }

    override suspend fun getCachedWeather(): WeatherState? {
        return preferenceManager.getWeatherState()
    }
    
    // 이 함수는 ViewModel에서 Repository로 이동되었습니다.
    private fun mapResponseToWeatherState(
        currentData: CurrentWeatherResponse?,
        hourlyData: HourlyForecastResponse?,
        weeklyData: WeeklyForecastResponse?,
        tempAdjustment: Int
    ): WeatherState {
        val weather = currentData?.weather

        // 체감온도 계산
        val temp = weather?.temp ?: 0.0
        val humidity = weather?.humidity ?: 0.0
        val windSpeedMs = weather?.windSpeed ?: 0.0
        val windSpeedKmh = windSpeedMs * 3.6

        val calculatedFeelsLike = FeelsLikeTempCalculator.calculate(temp, humidity, windSpeedKmh)
        val finalFeelsLike = calculatedFeelsLike + tempAdjustment
        val feelsLikeString = "${finalFeelsLike.toInt()}°"

        // 현재 날씨 변환
        val currentWeather = CurrentWeather(
            iconUrl = getWeatherIconUrl(weather?.skyCondition ?: "맑음", weather?.precipitationType ?: "없음"),
            temperature = "${weather?.temp?.toInt() ?: 0}°",
            description = weather?.skyCondition ?: "정보 없음",
            maxTemp = "${weather?.maxTemp?.toInt() ?: 0}°",
            minTemp = "${weather?.minTemp?.toInt() ?: 0}°",
            feelsLike = feelsLikeString
        )

        // 상세 날씨 변환
        val weatherDetails = WeatherDetails(
            feelsLike = feelsLikeString,
            humidity = "${weather?.humidity?.toInt() ?: 0}%",
            precipitation = "${weather?.rainfall ?: 0.0} mm",
            wind = "${weather?.windSpeed ?: 0.0} m/s",
            pm10 = weather?.pm10 ?: "정보없음",
            pressure = "1013 hPa",
            visibility = "10 km",
            uvIndex = "5"
        )

        // 시간별 예보 변환
        val hourlyForecast = hourlyData?.weather?.take(24)?.map { item ->
            HourlyForecast(
                time = formatTime(item.time),
                iconUrl = getWeatherIconUrl(item.sky, item.pty),
                temperature = "${item.temp?.toInt() ?: 0}°"
            )
        } ?: emptyList()

        // 주간 예보 변환
        val weeklyForecast = weeklyData?.weather?.map { item ->
            WeeklyForecast(
                day = formatDate(item.date),
                iconUrl = getWeatherIconUrl(item.skyAm, "없음"),
                maxTemp = "${item.maxTemp?.toInt() ?: 0}°",
                minTemp = "${item.minTemp?.toInt() ?: 0}°"
            )
        } ?: emptyList()

        // 최종 WeatherState 객체 생성
        val lastUpdatedTimestamp = SimpleDateFormat("MM월 dd일 HH:mm", Locale.KOREAN).format(Date())
        return WeatherState(
            isLoading = false,
            currentWeather = currentWeather,
            weatherDetails = weatherDetails,
            hourlyForecast = hourlyForecast,
            weeklyForecast = weeklyForecast,
            lastUpdated = "업데이트: $lastUpdatedTimestamp"
        )
    }

    // 아래 헬퍼 함수들도 ViewModel에서 Repository로 이동되었습니다.
    private fun getWeatherIconUrl(sky: String, pty: String): String {
        return when {
            pty.contains("비") || pty.contains("소나기") -> "https://openweathermap.org/img/wn/10d@2x.png"
            pty.contains("눈") -> "https://openweathermap.org/img/wn/13d@2x.png"
            sky.contains("맑음") -> "https://openweathermap.org/img/wn/01d@2x.png"
            sky.contains("구름조금") || sky.contains("구름많음") -> "https://openweathermap.org/img/wn/02d@2x.png"
            sky.contains("흐림") -> "https://openweathermap.org/img/wn/03d@2x.png"
            else -> "https://openweathermap.org/img/wn/01d@2x.png"
        }
    }

    private fun formatTime(time: String): String {
        return if (time.length == 4) {
            "${time.substring(0, 2)}:${time.substring(2, 4)}"
        } else {
            time
        }
    }

    private fun formatDate(date: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.KOREAN)
            val parsedDate = sdf.parse(date)
            val outputFormat = SimpleDateFormat("MM/dd (E)", Locale.KOREAN)
            outputFormat.format(parsedDate ?: date)
        } catch (e: Exception) {
            date
        }
    }
}
