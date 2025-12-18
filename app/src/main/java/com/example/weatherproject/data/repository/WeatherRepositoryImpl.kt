package com.example.weatherproject.data.repository

import android.util.Log
import com.example.weatherproject.data.CurrentWeather
import com.example.weatherproject.data.HourlyForecast
import com.example.weatherproject.data.WeatherDetails
import com.example.weatherproject.data.WeatherState
import com.example.weatherproject.data.WeeklyForecast
import com.example.weatherproject.data.local.WeatherCacheEntity
import com.example.weatherproject.data.local.WeatherDao
import com.example.weatherproject.network.WeatherApiService
import com.example.weatherproject.network.CctvResponse
import com.example.weatherproject.network.CurrentWeatherResponse
import com.example.weatherproject.network.HourlyForecastResponse
import com.example.weatherproject.network.WeeklyForecastResponse
import com.example.weatherproject.util.FeelsLikeTempCalculator
import com.example.weatherproject.util.GpsTransfer
import com.example.weatherproject.common.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.weatherproject.util.PreferenceManager
import java.util.Calendar

/**
 * WeatherRepository의 실제 구현체.
 * 네트워크, 로컬 DB 등 구체적인 데이터 소스를 다룹니다.
 */
class WeatherRepositoryImpl(
    private val weatherApi: WeatherApiService,
    private val weatherDao: WeatherDao,
    private val preferenceManager: PreferenceManager
) : WeatherRepository {

    private val gson = Gson() // 데이터 변환을 위한 Gson 인스턴스

    override suspend fun getWeatherData(
        lat: Double,
        lon: Double,
        tempAdjustment: Int
    ): Result<WeatherState> = withContext(Dispatchers.IO) {
        try {
            val (nx, ny) = GpsTransfer.convertToGrid(lat, lon)

            val currentDeferred = async { weatherApi.getCurrentWeather(nx, ny) }
            val hourlyDeferred = async { weatherApi.getHourlyForecast(nx, ny) }
            val weeklyDeferred = async { weatherApi.getWeeklyForecast(nx, ny) }

            // 현재 날씨는 필수 (실패 시 전체 실패)
            val currentResponse = currentDeferred.await()
            
            // 예보 데이터는 선택 (실패 시 로그 남기고 null 처리)
            val hourlyResponse = try {
                hourlyDeferred.await()
            } catch (e: Exception) {
                Log.e("WeatherRepository", "Hourly forecast API failed", e)
                null
            }

            val weeklyResponse = try {
                weeklyDeferred.await()
            } catch (e: Exception) {
                Log.e("WeatherRepository", "Weekly forecast API failed", e)
                null
            }

            // 어제 날씨 비교 로직
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
            val todayDate = dateFormat.format(calendar.time)
            
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayDate = dateFormat.format(calendar.time)

            val currentTemp = currentResponse.weather.temp?.toInt() ?: 0
            
            // 오늘 기온 저장
            preferenceManager.saveDailyTemp(todayDate, currentTemp)
            
            // 어제 기온 불러오기 및 비교
            val yesterdayTemp = preferenceManager.getDailyTemp(yesterdayDate)
            val comparisonText = if (yesterdayTemp != null) {
                val diff = currentTemp - yesterdayTemp
                when {
                    diff > 0 -> "어제보다 ${diff}° 높아요 🔺"
                    diff < 0 -> "어제보다 ${Math.abs(diff)}° 낮아요 🔻"
                    else -> "어제와 기온이 같아요"
                }
            } else {
                null
            }

            val weatherState = mapResponseToWeatherState(
                currentData = currentResponse,
                hourlyData = hourlyResponse,
                weeklyData = weeklyResponse,
                tempAdjustment = tempAdjustment,
                lat = lat,
                lon = lon,
                comparisonText = comparisonText
            )

            // UI 모델(WeatherState) -> DB 모델(WeatherCacheEntity)로 변환하여 저장
            weatherDao.upsertWeatherCache(weatherState.toEntity())

            Result.success(weatherState)

        } catch (e: Exception) {
            Log.e("WeatherRepository", "API 호출 실패: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getCachedWeather(): WeatherState? {
        // DB 모델(WeatherCacheEntity) -> UI 모델(WeatherState)로 변환
        return weatherDao.getWeatherCache()?.toWeatherState()
    }

    override suspend fun getNearbyCctv(lat: Double, lng: Double): Result<CctvResponse> = withContext(Dispatchers.IO) {
        try {
            val response = weatherApi.getNearbyCctv(lat, lng)
            Result.success(response)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "CCTV API 호출 실패: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun WeatherState.toEntity(): WeatherCacheEntity {
        return WeatherCacheEntity(
            // currentWeather
            current_iconUrl = this.currentWeather.iconUrl,
            current_temperature = this.currentWeather.temperature,
            current_description = this.currentWeather.description,
            current_maxTemp = this.currentWeather.maxTemp,
            current_minTemp = this.currentWeather.minTemp,
            current_feelsLike = this.currentWeather.feelsLike,
            // weatherDetails
            details_feelsLike = this.weatherDetails.feelsLike,
            details_humidity = this.weatherDetails.humidity,
            details_precipitation = this.weatherDetails.precipitation,
            details_wind = this.weatherDetails.wind,
            details_pm10 = this.weatherDetails.pm10,
            details_pressure = this.weatherDetails.pressure,
            details_visibility = this.weatherDetails.visibility,
            details_uvIndex = this.weatherDetails.uvIndex,
            // forecast (JSON으로 변환)
            hourlyForecastJson = gson.toJson(this.hourlyForecast),
            weeklyForecastJson = gson.toJson(this.weeklyForecast),
            // metadata
            latitude = this.latitude ?: 0.0,
            longitude = this.longitude ?: 0.0,
            address = this.address,
            lastUpdated = this.lastUpdated
        )
    }

    private fun WeatherCacheEntity.toWeatherState(): WeatherState {
        val hourlyType = object : TypeToken<List<HourlyForecast>>() {}.type
        val weeklyType = object : TypeToken<List<WeeklyForecast>>() {}.type

        return WeatherState(
            isLoading = false,
            currentWeather = CurrentWeather(
                iconUrl = this.current_iconUrl,
                temperature = this.current_temperature,
                description = this.current_description,
                maxTemp = this.current_maxTemp,
                minTemp = this.current_minTemp,
                feelsLike = this.current_feelsLike
            ),
            weatherDetails = WeatherDetails(
                feelsLike = this.details_feelsLike,
                humidity = this.details_humidity,
                precipitation = this.details_precipitation,
                wind = this.details_wind,
                pm10 = this.details_pm10,
                pressure = this.details_pressure,
                visibility = this.details_visibility,
                uvIndex = this.details_uvIndex
            ),
            hourlyForecast = gson.fromJson(this.hourlyForecastJson, hourlyType),
            weeklyForecast = gson.fromJson(this.weeklyForecastJson, weeklyType),
            latitude = this.latitude,
            longitude = this.longitude,
            address = this.address,
            lastUpdated = this.lastUpdated,
            yesterdayComparisonText = null
        )
    }

    private fun mapResponseToWeatherState(
        currentData: CurrentWeatherResponse?,
        hourlyData: HourlyForecastResponse?,
        weeklyData: WeeklyForecastResponse?,
        tempAdjustment: Int,
        lat: Double,
        lon: Double,
        comparisonText: String?
    ): WeatherState {
        val weather = currentData?.weather
        val temp = weather?.temp ?: 0.0
        val humidity = weather?.humidity ?: 0.0
        val windSpeedMs = weather?.windSpeed ?: 0.0
        val windSpeedKmh = windSpeedMs * 3.6
        val calculatedFeelsLike = FeelsLikeTempCalculator.calculate(temp, humidity, windSpeedKmh)
        val finalFeelsLike = calculatedFeelsLike + tempAdjustment
        val feelsLikeString = "${finalFeelsLike.toInt()}°"

        val currentWeather = CurrentWeather(
            iconUrl = getWeatherIconUrl(weather?.skyCondition ?: "맑음", weather?.precipitationType ?: "없음"),
            temperature = "${weather?.temp?.toInt() ?: 0}°",
            description = weather?.skyCondition ?: "정보 없음",
            maxTemp = "${weather?.maxTemp?.toInt() ?: 0}°",
            minTemp = "${weather?.minTemp?.toInt() ?: 0}°",
            feelsLike = feelsLikeString
        )

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

        val hourlyForecast = hourlyData?.weather?.take(24)?.map { item ->
            HourlyForecast(
                time = formatTime(item.time),
                iconUrl = getWeatherIconUrl(item.sky, item.pty),
                temperature = "${item.temp?.toInt() ?: 0}°",
                pty = item.pty
            )
        } ?: emptyList()

        val weeklyForecast = weeklyData?.weather?.map { item ->
            WeeklyForecast(
                day = formatDate(item.date),
                skyAm = item.skyAm,
                skyPm = item.skyPm,
                iconAm = getWeatherIconUrl(item.skyAm, "없음"),
                iconPm = getWeatherIconUrl(item.skyPm, "없음"),
                maxTemp = "${item.maxTemp?.toInt() ?: 0}°",
                minTemp = "${item.minTemp?.toInt() ?: 0}°"
            )
        } ?: emptyList()

        val lastUpdatedTimestamp = SimpleDateFormat("MM월 dd일 HH:mm", Locale.KOREAN).format(Date())
        return WeatherState(
            isLoading = false,
            currentWeather = currentWeather,
            weatherDetails = weatherDetails,
            hourlyForecast = hourlyForecast,
            weeklyForecast = weeklyForecast,
            latitude = lat,
            longitude = lon,
            lastUpdated = "업데이트: $lastUpdatedTimestamp",
            yesterdayComparisonText = comparisonText
        )
    }

    private fun getWeatherIconUrl(sky: String, pty: String): String {
        val iconId = when {
            pty.contains("비") || pty.contains("소나기") -> "10d"
            pty.contains("눈") -> "13d"
            sky.contains("맑음") -> "01d"
            sky.contains("구름조금") || sky.contains("구름많음") -> "02d"
            sky.contains("흐림") -> "03d"
            else -> "01d" // 기본값
        }
        return "${Constants.WEATHER_ICON_BASE_URL}${iconId}@2x.png"
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
