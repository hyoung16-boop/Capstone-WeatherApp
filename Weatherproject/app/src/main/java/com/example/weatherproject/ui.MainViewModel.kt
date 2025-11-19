package com.example.weatherproject.ui

import androidx.lifecycle.ViewModel
import com.example.weatherproject.data.CurrentWeather
import com.example.weatherproject.data.HourlyForecast
import com.example.weatherproject.data.WeatherDetails
import com.example.weatherproject.data.WeatherState
import com.example.weatherproject.data.WeeklyForecast
import com.example.weatherproject.util.FeelsLikeTempCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherState())
    val uiState: StateFlow<WeatherState> = _uiState

    init {
        loadFakeData()
    }

    private fun loadFakeData() {

        // 1. Define base numeric values from fake data
        val fakeTemp = 18.0
        val fakeHumidity = 65.0
        val fakeWindKmh = 12.0

        // 2. Calculate feels-like temperature
        val calculatedFeelsLike = FeelsLikeTempCalculator.calculate(
            Ta = fakeTemp,
            RH = fakeHumidity,
            windSpeedKmh = fakeWindKmh
        )

        // OpenWeather's 'real' icon addresses (for testing)
        val iconSunny = "http://openweathermap.org/img/wn/01d@2x.png"
        val iconPartlyCloudy = "http://openweathermap.org/img/wn/02d@2x.png"
        val iconCloudy = "http://openweathermap.org/img/wn/03d@2x.png"
        val iconRain = "http://openweathermap.org/img/wn/10d@2x.png"

        _uiState.value = WeatherState(
            currentAddress = "서울, 대한민국 (가짜)",
            currentWeather = CurrentWeather(
                iconUrl = iconPartlyCloudy,
                temperature = fakeTemp, // Use numeric value
                description = "Partly Cloudy",
                maxTemp = "최고: 22°",
                minTemp = "최저: 14°",
                feelsLike = calculatedFeelsLike // Use calculated value
            ),
            weatherDetails = WeatherDetails(
                feelsLike = calculatedFeelsLike, // Use calculated value
                humidity = fakeHumidity, // Use numeric value
                precipitation = "0 mm",
                wind = fakeWindKmh, // Use numeric value
                pm10 = "45 µg/m³",
                pm25 = "18 µg/m³",
                pressure = "1013 hPa",
                visibility = "10 km",
                uvIndex = "5"
            ),
            hourlyForecast = listOf(
                HourlyForecast("지금", iconPartlyCloudy, "18°", "0mm", "보통"),
                HourlyForecast("14:00", iconSunny, "20°", "0mm", "보통"),
                HourlyForecast("15:00", iconSunny, "21°", "0mm", "보통"),
                HourlyForecast("16:00", iconSunny, "22°", "0mm", "보통"),
                HourlyForecast("17:00", iconCloudy, "21°", "0mm", "보통"),
                HourlyForecast("18:00", iconCloudy, "19°", "10mm", "보통"),
                HourlyForecast("19:00", iconRain, "17°", "20mm", "좋음")
            ),
            weeklyForecast = listOf(
                WeeklyForecast(date = "11/17 (월)", iconSunny, "미세먼지 보통", "0mm", "15°", "23°"),
                WeeklyForecast(date = "11/18 (화)", iconPartlyCloudy, "미세먼지 나쁨", "0mm", "16°", "24°"),
                WeeklyForecast(date = "11/19 (수)", iconCloudy, "미세먼지 나쁨", "0mm", "14°", "22°"),
                WeeklyForecast(date = "11/20 (목)", iconRain, "미세먼지 좋음", "25mm", "13°", "20°"),
                WeeklyForecast(date = "11/21 (금)", iconRain, "미세먼지 좋음", "40mm", "12°", "19°"),
                WeeklyForecast(date = "11/22 (토)", iconSunny, "미세먼지 보통", "0mm", "13°", "22°"),
                WeeklyForecast(date = "11/23 (일)", iconPartlyCloudy, "미세먼지 보통", "0mm", "15°", "24°")
            )
        )
    }
}