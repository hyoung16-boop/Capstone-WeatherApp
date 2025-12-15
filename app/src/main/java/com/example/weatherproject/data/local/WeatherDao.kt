package com.example.weatherproject.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDao {

    /**
     * 날씨 캐시 정보를 삽입하거나 교체합니다.
     * OnConflictStrategy.REPLACE는 ID가 1인 기존 데이터가 있으면 새 데이터로 덮어쓰도록 합니다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeatherCache(weatherCache: WeatherCacheEntity)

    /**
     * 저장된 날씨 캐시 정보를 가져옵니다.
     * ID가 1인 데이터를 조회합니다.
     */
    @Query("SELECT * FROM weather_cache WHERE id = 1")
    suspend fun getWeatherCache(): WeatherCacheEntity?

    /**
     * 모든 날씨 캐시를 삭제합니다. (선택적)
     */
    @Query("DELETE FROM weather_cache")
    suspend fun clearCache()
}
