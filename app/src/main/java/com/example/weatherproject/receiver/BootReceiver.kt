package com.example.weatherproject.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.weatherproject.di.AlarmReceiverEntryPoint
import com.example.weatherproject.util.AlarmScheduler
import com.example.weatherproject.util.SmartAlertScheduler
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted. Rescheduling alarms...")

            // 1. 스마트 알림 재예약
            // 스마트 알림 설정 상태를 확인해야 하지만, 기본적으로 Worker는 WorkManager가 알아서 복구할 수도 있습니다.
            // 하지만 확실하게 하기 위해 재예약 로직을 호출하거나, WorkManager의 정책을 따릅니다.
            // WorkManager는 기본적으로 재부팅 후에도 작업이 유지되므로 별도 처리가 필요 없을 수 있으나,
            // SmartAlertScheduler 로직에 따라 다시 걸어주는 것이 안전할 수 있습니다.
            // 여기서는 AlarmEntity 기반의 사용자 알람 복구에 집중하겠습니다.

            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.IO)

            scope.launch {
                try {
                    val hiltEntryPoint = EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        AlarmReceiverEntryPoint::class.java
                    )
                    val alarmDao = hiltEntryPoint.alarmDao()
                    val alarms = alarmDao.getAllAlarmsSnapshot() // Flow가 아닌 List를 반환하는 메서드 필요

                    for (alarm in alarms) {
                        if (alarm.isEnabled) {
                            AlarmScheduler.schedule(context, alarm)
                            Log.d("BootReceiver", "Rescheduled alarm: ${alarm.id}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling alarms", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
