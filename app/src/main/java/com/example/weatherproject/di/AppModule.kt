package com.example.weatherproject.di

import android.app.Application
import android.content.Context
import com.example.weatherproject.data.local.AppDatabase
import com.example.weatherproject.data.repository.WeatherRepository
import com.example.weatherproject.data.repository.WeatherRepositoryImpl
import com.example.weatherproject.network.WeatherApiService
import com.example.weatherproject.util.LocationProvider
import com.example.weatherproject.util.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://www.weapi.shop/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherApiService(retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
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
        weatherDao: com.example.weatherproject.data.local.WeatherDao,
        preferenceManager: PreferenceManager
    ): WeatherRepository {
        return WeatherRepositoryImpl(weatherApi, weatherDao, preferenceManager)
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

    @Provides
    @Singleton
    fun provideNotificationHelper(): com.example.weatherproject.util.NotificationHelper {
        return com.example.weatherproject.util.NotificationHelper
    }
}
