package com.example.weatherproject.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherproject.data.repository.WeatherRepositoryImpl
import com.example.weatherproject.network.RetrofitClient
import com.example.weatherproject.util.PreferenceManager

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            // 1. Repository 의존성을 생성합니다.
            val weatherApiService = RetrofitClient.weatherApi
            val preferenceManager = PreferenceManager(application)
            val weatherRepository = WeatherRepositoryImpl(weatherApiService, preferenceManager)

            // 2. LocationProvider 의존성을 생성합니다.
            val locationProvider = com.example.weatherproject.util.LocationProvider(application)

            // 3. 모든 의존성을 ViewModel에 전달하여 최종적으로 생성합니다.
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, weatherRepository, locationProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
