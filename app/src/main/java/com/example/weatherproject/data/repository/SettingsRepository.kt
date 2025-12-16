package com.example.weatherproject.data.repository

import com.example.weatherproject.data.NotificationSettings
import com.example.weatherproject.util.PreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val prefManager: PreferenceManager
) {

    private val _settings = MutableStateFlow(loadSettings())
    val settings: Flow<NotificationSettings> = _settings.asStateFlow()

    private fun loadSettings(): NotificationSettings {
        return NotificationSettings(
            isMasterEnabled = prefManager.getBoolean(KEY_MASTER_ENABLED, true),
            isUserAlarmEnabled = prefManager.getBoolean(KEY_USER_ALARM_ENABLED, true),
            isSmartAlarmEnabled = prefManager.getBoolean(KEY_SMART_ALARM_ENABLED, true)
        )
    }

    fun saveMasterSwitch(enabled: Boolean) {
        prefManager.putBoolean(KEY_MASTER_ENABLED, enabled)
        _settings.value = _settings.value.copy(isMasterEnabled = enabled)
    }

    fun saveUserAlarmSwitch(enabled: Boolean) {
        prefManager.putBoolean(KEY_USER_ALARM_ENABLED, enabled)
        _settings.value = _settings.value.copy(isUserAlarmEnabled = enabled)
    }

    fun saveSmartAlarmSwitch(enabled: Boolean) {
        prefManager.putBoolean(KEY_SMART_ALARM_ENABLED, enabled)
        _settings.value = _settings.value.copy(isSmartAlarmEnabled = enabled)
    }

    companion object {
        private const val KEY_MASTER_ENABLED = "master_enabled"
        private const val KEY_USER_ALARM_ENABLED = "user_alarm_enabled"
        private const val KEY_SMART_ALARM_ENABLED = "smart_alarm_enabled"
    }
}
