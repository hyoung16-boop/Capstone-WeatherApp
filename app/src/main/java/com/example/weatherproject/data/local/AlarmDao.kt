package com.example.weatherproject.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms")
    suspend fun getAllAlarmsSnapshot(): List<AlarmEntity>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): AlarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    // 중복 검사를 위해 같은 시간의 알람 조회
    @Query("SELECT * FROM alarms WHERE hour = :hour AND minute = :minute")
    suspend fun getAlarmsByTime(hour: Int, minute: Int): List<AlarmEntity>
}
