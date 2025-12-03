package com.example.weatherproject.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.weatherproject.data.local.AlarmEntity
import com.example.weatherproject.receiver.AlarmReceiver
import java.util.Calendar

object AlarmScheduler {

    fun schedule(context: Context, alarm: AlarmEntity) {
        if (!alarm.isEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextTriggerTime = getNextTriggerTimeMillis(alarm)
        
        // 스케줄링이 불가능한 경우 (예: 과거의 일회성 알람)
        if (nextTriggerTime < System.currentTimeMillis()) {
            Log.w("AlarmScheduler", "Skipping past or invalid alarm: ${alarm.id} at ${Calendar.getInstance().apply { timeInMillis = nextTriggerTime }.time}")
            return
        }

        try {
            // 기존에 설정된 알람이 있다면 취소하고 새로 설정
            alarmManager.cancel(pendingIntent)
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTriggerTime,
                pendingIntent
            )
            Log.d("AlarmScheduler", "Alarm ${alarm.id} scheduled for ${Calendar.getInstance().apply { timeInMillis = nextTriggerTime }.time}")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Failed to schedule alarm due to security exception.", e)
        }
    }

    private fun getNextTriggerTimeMillis(alarm: AlarmEntity): Long {
        val now = Calendar.getInstance()

        // 1. 일회성 알람 처리
        if (alarm.selectedDate != null) {
            return Calendar.getInstance().apply {
                timeInMillis = alarm.selectedDate
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        val alarmTimeToday = Calendar.getInstance().apply{
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 2. 요일 반복 알람 처리
        if (alarm.days.isNotEmpty()) {
            val dayMap = mapOf("일" to Calendar.SUNDAY, "월" to Calendar.MONDAY, "화" to Calendar.TUESDAY, "수" to Calendar.WEDNESDAY, "목" to Calendar.THURSDAY, "금" to Calendar.FRIDAY, "토" to Calendar.SATURDAY)
            val selectedDaysOfWeek = alarm.days.mapNotNull { dayMap[it] }.toSet()

            // 오늘 시간이 아직 안지났고, 오늘이 선택된 요일이라면 오늘 울리도록 설정
            if (alarmTimeToday.after(now) && now.get(Calendar.DAY_OF_WEEK) in selectedDaysOfWeek) {
                return alarmTimeToday.timeInMillis
            }

            // 오늘 시간이 지났거나, 오늘이 선택된 요일이 아니라면 다음 요일을 찾음
            for (i in 1..7) {
                val nextDay = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, i) }
                if (nextDay.get(Calendar.DAY_OF_WEEK) in selectedDaysOfWeek) {
                    nextDay.set(Calendar.HOUR_OF_DAY, alarm.hour)
                    nextDay.set(Calendar.MINUTE, alarm.minute)
                    nextDay.set(Calendar.SECOND, 0)
                    nextDay.set(Calendar.MILLISECOND, 0)
                    return nextDay.timeInMillis
                }
            }
        }
        
        // 3. 반복 요일이 없는 '매일' 알람 처리
        return if (alarmTimeToday.after(now)) {
            alarmTimeToday.timeInMillis // 오늘 아직 시간이 안 지났으면 오늘
        } else {
            alarmTimeToday.add(Calendar.DAY_OF_MONTH, 1)
            alarmTimeToday.timeInMillis // 오늘 시간이 지났으면 내일
        }
    }

    fun cancel(context: Context, alarm: AlarmEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}