package com.example.weatherproject.data

import androidx.compose.runtime.Immutable

@Immutable
data class WeatherState(
    val isLoading: Boolean = true,
    val currentWeather: CurrentWeather = CurrentWeather(),
    val weatherDetails: WeatherDetails = WeatherDetails(),
    val hourlyForecast: List<HourlyForecast> = emptyList(),
    val weeklyForecast: List<WeeklyForecast> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String = "위치 정보 없음",
    val lastUpdated: String = "업데이트 정보 없음"
)

@Immutable
data class CurrentWeather(
    val iconUrl: String = "",
    val temperature: String = "--°",
    val description: String = "날씨 정보를 불러오는 중...",
    val maxTemp: String = "--°",
    val minTemp: String = "--°",
    val feelsLike: String = "--°"
)

@Immutable
data class WeatherDetails(
    val feelsLike: String = "--°",
    val humidity: String = "--%",
    val precipitation: String = "-- mm",
    val wind: String = "-- m/s",
    val pm10: String = "정보없음",
    val pressure: String = "-- hPa",
    val visibility: String = "-- km",
    val uvIndex: String = "--"
)

// ⭐️ 이 부분이 빠져있었습니다!
@Immutable
data class HourlyForecast(
    val time: String = "",
    val iconUrl: String = "",
    val temperature: String = "--°"
)

@Immutable
data class WeeklyForecast(
    val day: String = "",
    val iconAm: String = "",
    val iconPm: String = "",
    val skyAm: String = "",
    val skyPm: String = "",
    val maxTemp: String = "--°",
    val minTemp: String = "--°"
)
