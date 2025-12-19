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
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _saveResult = kotlinx.coroutines.flow.MutableSharedFlow<SaveResult>()
    val saveResult = _saveResult.asSharedFlow()

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
            val duplicateType = checkDuplicate(hour, minute, days, date)
            
            if (duplicateType != null) {
                _saveResult.emit(SaveResult.Duplicate(duplicateType))
                return@launch
            }

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
            _saveResult.emit(SaveResult.Success("알림이 저장되었습니다."))
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
            val duplicateType = checkDuplicate(hour, minute, days, date, excludeId = id)

            if (duplicateType != null) {
                _saveResult.emit(SaveResult.Duplicate(duplicateType))
                return@launch
            }

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
            _saveResult.emit(SaveResult.Success("알림이 수정되었습니다."))
        }
    }

    private suspend fun checkDuplicate(hour: Int, minute: Int, newDays: List<String>, newDate: Long?, excludeId: Int? = null): String? {
        val sameTimeAlarms = alarmDao.getAlarmsByTime(hour, minute)
        
        for (existing in sameTimeAlarms) {
            if (excludeId != null && existing.id == excludeId) continue

            // 1. 둘 다 요일 반복인 경우: 하나라도 겹치는 요일이 있으면 중복
            if (existing.days.isNotEmpty() && newDays.isNotEmpty()) {
                val overlap = existing.days.intersect(newDays.toSet())
                if (overlap.isNotEmpty()) {
                    return "이미 해당 시간에 ${overlap.joinToString(", ")}요일 알림이 존재합니다."
                }
            }

            // 2. 둘 다 특정 날짜인 경우
            if (existing.selectedDate != null && newDate != null) {
                if (existing.selectedDate == newDate) {
                    return "이미 해당 날짜와 시간에 알림이 존재합니다."
                }
            }

            // 3. 기존 알림은 요일 반복, 새 알림은 특정 날짜
            if (existing.days.isNotEmpty() && newDate != null) {
                val newDayOfWeek = getDayOfWeekString(newDate)
                if (existing.days.contains(newDayOfWeek)) {
                    return "해당 날짜(${newDayOfWeek}요일)에 이미 반복 알림이 설정되어 있습니다."
                }
            }

            // 4. 기존 알림은 특정 날짜, 새 알림은 요일 반복
            if (existing.selectedDate != null && newDays.isNotEmpty()) {
                val existingDayOfWeek = getDayOfWeekString(existing.selectedDate)
                if (newDays.contains(existingDayOfWeek)) {
                    return "설정하려는 요일(${existingDayOfWeek}요일)에 이미 특정 날짜 알림이 존재합니다."
                }
            }
            
            // 5. 둘 다 반복 없음 (매일) - 사실 days가 비어있고 date가 null이면 매일 알림으로 간주할 수 있음(로직에 따라 다름)
            // 현재 로직상 days가 empty이고 selectedDate가 null인 경우가 있다면 처리
             if (existing.days.isEmpty() && existing.selectedDate == null && newDays.isEmpty() && newDate == null) {
                 return "이미 해당 시간에 알림이 존재합니다."
             }
        }
        return null
    }

    private fun getDayOfWeekString(timeMillis: Long): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timeMillis
        return when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.SUNDAY -> "일"
            java.util.Calendar.MONDAY -> "월"
            java.util.Calendar.TUESDAY -> "화"
            java.util.Calendar.WEDNESDAY -> "수"
            java.util.Calendar.THURSDAY -> "목"
            java.util.Calendar.FRIDAY -> "금"
            java.util.Calendar.SATURDAY -> "토"
            else -> ""
        }
    }
}

sealed class SaveResult {
    data class Success(val message: String) : SaveResult()
    data class Duplicate(val message: String) : SaveResult()
}
