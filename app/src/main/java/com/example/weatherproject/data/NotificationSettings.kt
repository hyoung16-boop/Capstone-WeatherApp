package com.example.weatherproject.data

import androidx.compose.runtime.Immutable

/**
 * 알림 설정 상태를 나타내는 데이터 클래스.
 * @param isMasterEnabled 전체 알림 설정
 * @param isUserAlarmEnabled 사용자 지정 알림 설정
 * @param isSmartAlarmEnabled 스마트 알림(비/눈 예보) 설정
 */
@Immutable
data class NotificationSettings(
    val isMasterEnabled: Boolean = true,
    val isUserAlarmEnabled: Boolean = true,
    val isSmartAlarmEnabled: Boolean = true
)
