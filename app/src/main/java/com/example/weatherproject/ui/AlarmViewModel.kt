package com.example.weatherproject.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherproject.data.NotificationSettings
import com.example.weatherproject.data.local.AlarmDao
import com.example.weatherproject.data.local.AlarmEntity
import com.example.weatherproject.data.repository.SettingsRepository
import com.example.weatherproject.util.AlarmScheduler
import com.example.weatherproject.util.SmartAlertScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val alarmDao: AlarmDao,
    private val settingsRepository: SettingsRepository,
    private val application: Application
) : ViewModel() {

    // 설정 repository로부터 세팅값을 Flow로 관찰
    val settings: StateFlow<NotificationSettings> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotificationSettings()
        )

    val alarmList: StateFlow<List<AlarmEntity>> = alarmDao.getAllAlarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        // 스마트 알림 설정값이 변경될 때마다 스케줄링/취소 작업을 수행
        settings.map { it.isSmartAlarmEnabled }
            .distinctUntilChanged()
            .onEach { isEnabled ->
                if (isEnabled) {
                    SmartAlertScheduler.schedule(application)
                } else {
                    SmartAlertScheduler.cancel(application)
                }
            }
            .launchIn(viewModelScope)
    }

    fun saveMasterSwitch(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveMasterSwitch(enabled)
        }
    }
    fun saveUserAlarmSwitch(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveUserAlarmSwitch(enabled)
        }
    }
    fun saveSmartAlarmSwitch(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveSmartAlarmSwitch(enabled)
        }
    }


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
            AlarmScheduler.schedule(application, savedAlarm)
        }
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
            alarmDao.updateAlarm(updatedAlarm)
            
            if (updatedAlarm.isEnabled) {
                AlarmScheduler.schedule(application, updatedAlarm)
            } else {
                AlarmScheduler.cancel(application, updatedAlarm)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            alarmDao.deleteAlarm(alarm)
            AlarmScheduler.cancel(application, alarm)
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
            AlarmScheduler.schedule(application, updatedAlarm)
        }
    }
}
