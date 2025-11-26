// WeatherState.kt

package com.example.weatherproject.data

// 메인 화면 전체의 상태를 담는 '틀'
data class WeatherState(
    val currentAddress: String = "서울, 대한민국",
    val currentWeather: CurrentWeather = CurrentWeather(),
    val weatherDetails: WeatherDetails = WeatherDetails(),
    val hourlyForecast: List<HourlyForecast> = emptyList(),
    val weeklyForecast: List<WeeklyForecast> = emptyList()
)

// "현재 날씨" 카드 '틀'
data class CurrentWeather(
    val iconUrl: String = "",
    val temperature: String = "18°", // Double -> String
    val description: String = "Partly Cloudy",
    val maxTemp: String = "최고: 22°",
    val minTemp: String = "최저: 14°",
    val feelsLike: String = "체감: 17°" // Double -> String
)

// "시간별 예보" 카드 '틀'
data class HourlyForecast(
    val time: String,
    val iconUrl: String,
    val temperature: String,
    val precipitation: String, // 강수량
    val pm10Status: String     // 미세먼지
)

// "날씨 상세" 카드 '틀'
data class WeatherDetails(
    val feelsLike: String = "17°", // Double -> String
    val humidity: String = "65%", // Double -> String
    val precipitation: String = "0 mm",
    val wind: String = "12 km/h", // Double -> String
    val pm10: String = "45 µg/m³",
    val pm25: String = "18 µg/m³",
    val pressure: String = "1013 hPa",
    val visibility: String = "10 km",
    val uvIndex: String = "5"
)

// "주간 예보" 카드 '틀'
data class WeeklyForecast(
    val date: String,
    val iconUrl: String,
    val pm10Status: String,
    val precipitation: String,
    val minTemp: String,
    val maxTemp: String
)