package com.example.weatherproject.network

import com.google.gson.annotations.SerializedName

data class CurrentWeatherResponse(
    @SerializedName("위치좌표")
    val location: LocationCoord,
    @SerializedName("날씨")
    val weather: CurrentWeatherData
)

data class LocationCoord(
    val nx: Int,
    val ny: Int
)

// ⭐️ 타입을 Double로 수정
data class CurrentWeatherData(
    @SerializedName("기온(°C)")
    val temp: Double?,  // String → Double
    @SerializedName("1시간 강수량(mm)")
    val rainfall: Double?,  // String → Double
    @SerializedName("습도(%)")
    val humidity: Double?,  // String → Double
    @SerializedName("풍속(m/s)")
    val windSpeed: Double?,  // String → Double
    @SerializedName("일 최저기온(°C)")
    val minTemp: Double?,  // String → Double
    @SerializedName("일 최고기온(°C)")
    val maxTemp: Double?,  // String → Double
    @SerializedName("하늘상태")
    val skyCondition: String?,
    @SerializedName("강수형태")
    val precipitationType: String?,
    @SerializedName("미세먼지")  // ⭐️ 키 이름 수정
    val pm10: String?,
    @SerializedName("초미세먼지")  // ⭐️ 키 이름 수정
    val pm25: String?
)

data class HourlyForecastResponse(
    @SerializedName("위치좌표")
    val location: LocationCoord,
    @SerializedName("날씨")
    val weather: List<HourlyWeatherItem>
)

data class HourlyWeatherItem(
    val date: String,
    val time: String,
    val temp: Double?,
    val sky: String,
    val pty: String,
    @SerializedName("rain_amount")
    val rainAmount: String,
    val pop: Double  // Int → Double
)

data class WeeklyForecastResponse(
    @SerializedName("위치좌표")
    val location: LocationCoord,
    @SerializedName("날씨")
    val weather: List<WeeklyWeatherItem>
)

data class WeeklyWeatherItem(
    val date: String,
    @SerializedName("min_temp")
    val minTemp: Double?,
    @SerializedName("max_temp")
    val maxTemp: Double?,
    @SerializedName("sky_am")
    val skyAm: String,
    @SerializedName("sky_pm")
    val skyPm: String,
    val pop: Int
)