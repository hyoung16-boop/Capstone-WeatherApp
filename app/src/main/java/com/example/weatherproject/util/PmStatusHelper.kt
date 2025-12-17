package com.example.weatherproject.util

object PmStatusHelper {

    /**
     * 서버에서 받은 미세먼지 상태 텍스트를 정리하여 반환합니다.
     * @param pm10Value 서버로부터 받은 미세먼지 상태 (e.g., "좋음", "나쁨", "정보없음", null)
     * @return UI에 표시할 최종 상태 문자열
     */
    fun getStatus(pm10Value: String): String {
        // 서버가 "정보없음"을 주거나, 어떤 이유로든 값이 비어있으면 "정보없음"으로 통일
        if (pm10Value.contains("정보없음") || pm10Value.isBlank()) {
            return "정보없음"
        }
        
        // "좋음", "보통", "나쁨" 등 서버가 보내준 값을 그대로 반환
        return pm10Value
    }
}
