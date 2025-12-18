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

/**
 * 기기 재부팅 완료 시(`BOOT_COMPLETED`) 호출되는 브로드캐스트 리시버입니다.
 *
 * 역할:
 * - 안드로이드 시스템은 재부팅 시 등록된 알람(AlarmManager)을 모두 초기화합니다.
 * - 이 리시버는 DB에 저장된 알람 목록을 읽어와서, 활성화된 알람들을 다시 예약(Reschedule)하는 역할을 합니다.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted. Rescheduling alarms...")

            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.IO)

            scope.launch {
                try {
                    val hiltEntryPoint = EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        AlarmReceiverEntryPoint::class.java
                    )
                    val alarmDao = hiltEntryPoint.alarmDao()
                    val alarms = alarmDao.getAllAlarmsSnapshot()

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