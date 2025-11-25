package com.example.weatherproject.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.weatherproject.util.NotificationHelper
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val isOneTime = intent.getBooleanExtra("IS_ONE_TIME", false)
        val days = intent.getStringArrayListExtra("ALARM_DAYS")

        if (!isOneTime && days != null && days.isNotEmpty()) {
            val calendar = Calendar.getInstance()
            val todayDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val todayString = when (todayDayOfWeek) {
                Calendar.SUNDAY -> "일"
                Calendar.MONDAY -> "월"
                Calendar.TUESDAY -> "화"
                Calendar.WEDNESDAY -> "수"
                Calendar.THURSDAY -> "목"
                Calendar.FRIDAY -> "금"
                Calendar.SATURDAY -> "토"
                else -> ""
            }
            
            if (!days.contains(todayString)) {
                return
            }
        }

        NotificationHelper.showNotification(
            context, 
            "설정된 알람", 
            "설정하신 시간입니다. 지금 날씨를 확인해보세요!"
        )
    }
}
