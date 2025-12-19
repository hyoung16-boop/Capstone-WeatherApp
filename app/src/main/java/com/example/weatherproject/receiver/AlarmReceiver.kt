package com.example.weatherproject.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.weatherproject.di.AlarmReceiverEntryPoint
import com.example.weatherproject.util.AlarmScheduler
import com.example.weatherproject.worker.TestWorker
import com.example.weatherproject.worker.WeatherUpdateWorker
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        if (alarmId == -1) {
            Log.e("AlarmReceiver", "Invalid alarmId received")
            return
        }

        Log.d("AlarmReceiver", "Alarm received with ID: $alarmId. Enqueuing worker.")

        // 1. Worker에게 작업 요청
        val workRequest = OneTimeWorkRequestBuilder<WeatherUpdateWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)

        // 2. 다음 반복 알람 재예약 (DB 접근이 필요하므로 background에서 처리)
        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                // Hilt EntryPoint를 통해 안전하게 DAO 인스턴스를 가져옴
                val hiltEntryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    AlarmReceiverEntryPoint::class.java
                )
                val alarmDao = hiltEntryPoint.alarmDao()

                val alarm = alarmDao.getAlarmById(alarmId)
                if (alarm != null) {
                    if (alarm.selectedDate != null) {
                        // 일회성 알람: 울린 후 비활성화 (OFF) 처리
                        val disabledAlarm = alarm.copy(isEnabled = false)
                        alarmDao.updateAlarm(disabledAlarm)
                        Log.d("AlarmReceiver", "One-time alarm disabled: $alarmId")
                    } else {
                        // 반복 알람: 다음 알람 스케줄링
                        AlarmScheduler.schedule(context, alarm)
                        Log.d("AlarmReceiver", "Rescheduled next alarm for repeating alarm: $alarmId")
                    }
                }
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error rescheduling alarm: $alarmId", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
