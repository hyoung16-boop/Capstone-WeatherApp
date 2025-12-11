package com.example.weatherproject.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val days: List<String>, // 요일 반복일 때 사용 (비어있으면 selectedDate 사용)
    val selectedDate: Long?, // 날짜 지정일 때 사용 (null이면 요일 반복)
    val isEnabled: Boolean
)