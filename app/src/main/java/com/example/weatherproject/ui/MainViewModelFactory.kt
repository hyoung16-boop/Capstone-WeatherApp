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
            // 1. Repository가 필요로 하는 의존성들을 여기서 생성합니다.
            val weatherApiService = RetrofitClient.weatherApi
            val preferenceManager = PreferenceManager(application)

            // 2. Repository 인스턴스를 생성합니다.
            val weatherRepository = WeatherRepositoryImpl(weatherApiService, preferenceManager)

            // 3. 생성한 Repository를 ViewModel에 전달하여 최종적으로 ViewModel을 생성합니다.
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, weatherRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
