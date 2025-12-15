package com.example.weatherproject.di

import android.app.Application
import android.content.Context
import com.example.weatherproject.data.local.AppDatabase
import com.example.weatherproject.data.repository.WeatherRepository
import com.example.weatherproject.data.repository.WeatherRepositoryImpl
import com.example.weatherproject.network.RetrofitClient
import com.example.weatherproject.network.WeatherApiService
import com.example.weatherproject.util.LocationProvider
import com.example.weatherproject.util.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWeatherApiService(): WeatherApiService {
        return RetrofitClient.weatherApi
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideWeatherDao(appDatabase: AppDatabase) = appDatabase.weatherDao()

    @Provides
    @Singleton
    fun provideAlarmDao(appDatabase: AppDatabase) = appDatabase.alarmDao()

    @Provides
    @Singleton
    fun provideWeatherRepository(
        weatherApi: WeatherApiService,
        weatherDao: com.example.weatherproject.data.local.WeatherDao
    ): WeatherRepository {
        return WeatherRepositoryImpl(weatherApi, weatherDao)
    }

    @Provides
    @Singleton
    fun providePreferenceManager(@ApplicationContext context: Context): PreferenceManager {
        return PreferenceManager(context)
    }
    
    @Provides
    @Singleton
    fun provideLocationProvider(application: Application): LocationProvider {
        return LocationProvider(application)
    }
}
