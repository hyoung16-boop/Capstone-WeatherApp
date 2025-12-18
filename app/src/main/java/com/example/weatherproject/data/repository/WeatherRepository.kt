package com.example.weatherproject.data.repository

import com.example.weatherproject.data.WeatherState
import com.example.weatherproject.network.CctvResponse

/**
 * ViewModel과 데이터 소스(네트워크, 캐시 등) 사이의 중재자 역할을 하는 인터페이스입니다.
 * ViewModel은 이 인터페이스에만 의존하게 됩니다.
 */
interface WeatherRepository {

    /**
     * 지정된 위치의 날씨 정보를 가져옵니다.
     * 내부적으로 네트워크 요청, 데이터 변환, 캐시 저장을 모두 처리합니다.
     * @param lat 위도
     * @param lon 경도
     * @param tempAdjustment 사용자가 설정한 체감온도 보정값
     * @return 성공 시 WeatherState가 담긴 Result 객체, 실패 시 에러가 담긴 Result 객체
     */
    suspend fun getWeatherData(
        lat: Double,
        lon: Double,
        tempAdjustment: Int
    ): Result<WeatherState>

    /**
     * 로컬에 캐시된 마지막 날씨 정보를 가져옵니다.
     * @return 저장된 WeatherState가 있으면 반환, 없으면 null 반환
     */
    suspend fun getCachedWeather(): WeatherState?

    /**
     * 주변 CCTV 정보를 가져옵니다.
     */
    suspend fun getNearbyCctv(lat: Double, lng: Double): Result<CctvResponse>
}
