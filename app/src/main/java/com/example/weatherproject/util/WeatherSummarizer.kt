package com.example.weatherproject.util

import com.example.weatherproject.data.CurrentWeather
import com.example.weatherproject.data.WeatherDetails
import com.example.weatherproject.data.HourlyForecast
import kotlin.math.abs

object WeatherSummarizer {

    fun getSummary(
        currentWeather: CurrentWeather,
        weatherDetails: WeatherDetails,
        hourlyForecast: List<HourlyForecast>
    ): String {
        // 데이터 파싱
        val actualTemp = currentWeather.temperature.replace(Regex("[^0-9-]"), "").toIntOrNull()
        val feelsLikeTemp = currentWeather.feelsLike.replace(Regex("[^0-9-]"), "").toIntOrNull()
        val maxTemp = currentWeather.maxTemp.replace(Regex("[^0-9-]"), "").toIntOrNull()
        val minTemp = currentWeather.minTemp.replace(Regex("[^0-9-]"), "").toIntOrNull()
        val windSpeed = weatherDetails.wind.replace(Regex("[^0-9.]"), "").toDoubleOrNull()
        val humidity = weatherDetails.humidity.replace(Regex("[^0-9]"), "").toIntOrNull()
        val precipitation = weatherDetails.precipitation.replace(Regex("[^0-9.]"), "").toDoubleOrNull()

        if (actualTemp == null || feelsLikeTemp == null) {
            return "현재 날씨를 분석하고 있어요."
        }

        // 1. 현재 강수 확인
        if (precipitation != null && precipitation > 0) {
            return when {
                feelsLikeTemp < 0 -> "눈이 내리는 매우 추운 날씨예요."
                feelsLikeTemp < 10 -> "비가 내려 쌀쌀하게 느껴져요."
                else -> "비가 오니 우산을 꼭 챙기세요."
            }
        }

        // 2. 단기 강수 예보 확인 (향후 6시간)
        val upcomingPtyCodes = hourlyForecast.take(6).map { it.pty }.filter { it != "0" }.toSet()
        if (upcomingPtyCodes.isNotEmpty()) {
            // pty 코드: 1(비), 2(비/눈), 3(눈), 4(소나기)
            if (upcomingPtyCodes.any { it == "3" || it == "2" }) { // 눈 또는 진눈깨비
                return "곧 눈 소식이 있으니 참고하세요."
            }
            if (upcomingPtyCodes.any { it == "1" || it == "4" }) { // 비 또는 소나기
                return "곧 비 소식이 있으니 우산을 챙기세요."
            }
        }

        // 3. 일교차 확인
        if (maxTemp != null && minTemp != null && (maxTemp - minTemp) > 10) {
            return "일교차가 커서 저녁엔 쌀쌀할 수 있으니 겉옷을 챙기는 게 좋겠어요."
        }

        // 4. 바람 또는 체감온도 차이 확인
        val tempDiff = abs(actualTemp - feelsLikeTemp)
        if (windSpeed != null && windSpeed > 9.0) { // 9m/s 이상
            return "바람이 매우 강하게 불어 체감온도가 더 낮아요."
        }
        if (tempDiff > 3) {
            return if (actualTemp > feelsLikeTemp) {
                "바람 때문에 실제 기온보다 더 춥게 느껴져요."
            } else {
                "습도가 높아 실제 기온보다 더 덥게 느껴져요."
            }
        }

        // 5. 습도 확인 (덥고 습할 때)
        if (humidity != null && humidity > 70 && feelsLikeTemp >= 25) {
            return "덥고 습해서 불쾌지수가 높은 날씨예요."
        }

        // 6. 일반적인 날씨 요약
        return when {
            feelsLikeTemp >= 28 -> "푹푹 찌는 한여름 날씨를 보여요."
            feelsLikeTemp >= 23 -> "활동 시 조금 더울 수 있는 날씨입니다."
            feelsLikeTemp >= 17 -> "나들이하기 좋은 쾌적한 날씨네요!"
            feelsLikeTemp >= 12 -> "선선한 바람이 부는 가을 날씨 같아요."
            feelsLikeTemp >= 5 -> "아침저녁으로 꽤 쌀쌀하게 느껴져요."
            else -> "옷깃을 여미게 되는 추운 날씨입니다."
        }
    }
}
