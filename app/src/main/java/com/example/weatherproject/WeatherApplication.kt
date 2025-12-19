package com.example.weatherproject

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.weatherproject.worker.SmartAlertWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class WeatherApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        setupWorkManager()
    }

    private fun setupWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 3시간마다 실행되는 주기적 작업
        val smartAlertRequest = PeriodicWorkRequestBuilder<SmartAlertWorker>(
            3, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        // 앱이 실행될 때마다 작업을 예약 (이미 존재하면 유지: KEEP)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SmartAlertWork",
            ExistingPeriodicWorkPolicy.KEEP, 
            smartAlertRequest
        )
    }
}
