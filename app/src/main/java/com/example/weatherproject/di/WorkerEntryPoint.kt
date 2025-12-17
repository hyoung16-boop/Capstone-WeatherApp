package com.example.weatherproject.di

import com.example.weatherproject.data.repository.WeatherRepository
import com.example.weatherproject.util.LocationProvider
import com.example.weatherproject.util.PreferenceManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkerEntryPoint {
    fun weatherRepository(): WeatherRepository
    fun preferenceManager(): PreferenceManager
    fun locationProvider(): LocationProvider
}
