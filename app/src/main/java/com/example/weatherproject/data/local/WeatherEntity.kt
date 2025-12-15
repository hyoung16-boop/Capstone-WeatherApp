package com.example.weatherproject.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val id: Int = 1,

    // currentWeather
    val current_iconUrl: String,
    val current_temperature: String,
    val current_description: String,
    val current_maxTemp: String,
    val current_minTemp: String,
    val current_feelsLike: String,

    // weatherDetails
    val details_feelsLike: String,
    val details_humidity: String,
    val details_precipitation: String,
    val details_wind: String,
    val details_pm10: String,
    val details_pressure: String,
    val details_visibility: String,
    val details_uvIndex: String,

    // forecast (JSON String)
    val hourlyForecastJson: String,
    val weeklyForecastJson: String,

    // metadata
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val lastUpdated: String
)
