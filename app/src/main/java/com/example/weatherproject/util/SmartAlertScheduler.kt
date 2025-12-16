package com.example.weatherproject.util

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.weatherproject.worker.SmartAlertWorker
import java.util.concurrent.TimeUnit

object SmartAlertScheduler {

    private const val UNIQUE_WORK_NAME = "smart_weather_alert"

    fun schedule(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // 3시간마다 반복 실행되는 작업 요청 생성
        val periodicWorkRequest = PeriodicWorkRequestBuilder<SmartAlertWorker>(3, TimeUnit.HOURS)
            .build()

        // 고유한 이름으로 작업을 등록합니다. 동일한 이름의 작업이 이미 있으면 교체합니다.
        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest
        )
    }

    fun cancel(context: Context) {
        val workManager = WorkManager.getInstance(context)
        // 고유한 이름으로 작업을 취소합니다.
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
    }
}
