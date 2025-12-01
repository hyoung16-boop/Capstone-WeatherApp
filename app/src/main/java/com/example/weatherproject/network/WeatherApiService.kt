package com.example.weatherproject.network

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    /**
     * 현재 날씨 조회
     */
    @GET("/api/weather/current")
    suspend fun getCurrentWeather(
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): CurrentWeatherResponse

    /**
     * 시간별 예보 조회
     */
    @GET("/api/weather/forecast")
    suspend fun getHourlyForecast(
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): HourlyForecastResponse

    /**
     * 주간 예보 조회
     */
    @GET("/api/weather/week")
    suspend fun getWeeklyForecast(
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): WeeklyForecastResponse

    /**
     * 🆕 가까운 CCTV 조회
     */
    @GET("/get_cctv")
    suspend fun getNearbyCctv(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): CctvResponse
}