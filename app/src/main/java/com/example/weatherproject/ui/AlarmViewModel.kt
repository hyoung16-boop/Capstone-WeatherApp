package com.example.weatherproject.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherproject.data.local.AlarmEntity
import com.example.weatherproject.data.local.AppDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.weatherproject.util.AlarmScheduler

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmDao = AppDatabase.getDatabase(application).alarmDao()

    // DB의 데이터를 Flow로 관찰하여 UI 상태로 변환
    val alarmList: StateFlow<List<AlarmEntity>> = alarmDao.getAllAlarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addAlarm(hour: Int, minute: Int, days: List<String>, date: Long?) {
        viewModelScope.launch {
            val newAlarm = AlarmEntity(
                hour = hour,
                minute = minute,
                days = days,
                selectedDate = date,
                isEnabled = true
            )
            val id = alarmDao.insertAlarm(newAlarm)
            val savedAlarm = newAlarm.copy(id = id.toInt())
            AlarmScheduler.schedule(getApplication(), savedAlarm)
        }
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
            alarmDao.updateAlarm(updatedAlarm)
            
            if (updatedAlarm.isEnabled) {
                AlarmScheduler.schedule(getApplication(), updatedAlarm)
            } else {
                AlarmScheduler.cancel(getApplication(), updatedAlarm)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            alarmDao.deleteAlarm(alarm)
            AlarmScheduler.cancel(getApplication(), alarm)
        }
    }

    suspend fun getAlarmById(id: Int): AlarmEntity? {
        return alarmDao.getAlarmById(id)
    }

    fun updateAlarmInfo(id: Int, hour: Int, minute: Int, days: List<String>, date: Long?) {
        viewModelScope.launch {
            val updatedAlarm = AlarmEntity(
                id = id,
                hour = hour,
                minute = minute,
                days = days,
                selectedDate = date,
                isEnabled = true
            )
            alarmDao.updateAlarm(updatedAlarm)
            AlarmScheduler.schedule(getApplication(), updatedAlarm)
        }
    }
}
