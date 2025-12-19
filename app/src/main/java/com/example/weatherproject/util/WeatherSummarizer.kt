package com.example.weatherproject.util

import com.example.weatherproject.data.CurrentWeather
import com.example.weatherproject.data.WeatherDetails
import com.example.weatherproject.data.HourlyForecast
import java.util.Calendar
import kotlin.math.abs

object WeatherSummarizer {

    fun getSummary(
        currentWeather: CurrentWeather,
        weatherDetails: WeatherDetails,
        hourlyForecast: List<HourlyForecast>
    ): String {
        val actualTemp = currentWeather.temperature.replace(Regex("[^0-9-]"), "").toIntOrNull() ?: 0
        val feelsLikeTemp = currentWeather.feelsLike.replace(Regex("[^0-9-]"), "").toIntOrNull() ?: 0
        val maxTemp = currentWeather.maxTemp.replace(Regex("[^0-9-]"), "").toIntOrNull()
        val minTemp = currentWeather.minTemp.replace(Regex("[^0-9-]"), "").toIntOrNull()
        val windSpeed = weatherDetails.wind.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
        val humidity = weatherDetails.humidity.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
        val precipitation = weatherDetails.precipitation.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0

        // 1. 강수/눈 예보 (최우선 - 위험하거나 특별한 상황이므로 즉시 반환)
        if (precipitation > 0) {
            return when {
                feelsLikeTemp < 0 -> "눈이 내리는 날씨예요. 미끄러지지 않게 조심하세요."
                feelsLikeTemp < 5 -> "비나 눈이 오고 추워요. 따뜻하게 입으세요."
                else -> "비가 오고 있어요. 우산을 꼭 챙기세요."
            }
        }
        
        val upcomingPtyCodes = hourlyForecast.take(6).map { it.pty }.filter { it != "0" }.toSet()
        if (upcomingPtyCodes.isNotEmpty()) {
            if (upcomingPtyCodes.any { it == "3" || it == "2" }) return "곧 눈이 올 수 있어요."
            if (upcomingPtyCodes.any { it == "1" || it == "4" }) return "곧 비 소식이 있으니 우산을 챙기세요."
        }

        // 2. 계절과 기온 기반 기본 메시지 선정
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        var message = ""
        val isWinter = currentMonth == 12 || currentMonth <= 2
        val isSummer = currentMonth in 6..8
        val dailyRange = if (maxTemp != null && minTemp != null) maxTemp - minTemp else 0
        
        // 하늘 상태 확인
        val isCloudy = currentWeather.description.contains("흐림") || currentWeather.description.contains("구름")
        val isClear = currentWeather.description.contains("맑음")

        message = if (isWinter) {
            when {
                feelsLikeTemp >= 12 -> "겨울이지만 봄처럼 포근한 날씨예요."
                feelsLikeTemp >= 5 -> "겨울치고는 비교적 온화한 편이에요."
                feelsLikeTemp >= -5 -> "공기가 꽤 차가운 날씨예요. 따뜻하게 입으세요."
                else -> "매서운 한파가 찾아왔어요. 보온에 신경 쓰세요."
            }
        } else if (isSummer) {
            when {
                feelsLikeTemp >= 33 -> "가만히 있어도 땀이 나는 폭염이에요."
                feelsLikeTemp >= 28 -> "무더운 여름 날씨입니다. 수분을 자주 섭취하세요."
                feelsLikeTemp >= 23 -> "활동하기 적당하지만 조금 더울 수 있어요."
                else -> "여름치고는 선선해서 활동하기 좋아요."
            }
        } else { // 봄/가을
            when {
                feelsLikeTemp >= 25 -> "초여름처럼 다소 더운 날씨예요."
                feelsLikeTemp >= 18 -> "나들이하기 딱 좋은 쾌적한 날씨예요!"
                feelsLikeTemp >= 10 -> "선선한 바람이 불어 산책하기 좋아요."
                feelsLikeTemp >= 5 -> "아침저녁으로 쌀쌀하니 겉옷을 챙기세요."
                else -> "계절보다 날씨가 많이 춥네요."
            }
        }

        // 3. 하늘 상태/바람/습도 정보를 바탕으로 메시지 보완 (Modifier)
        
        // 겨울철 하늘 상태 (흐림/맑음) 보정
        if (isWinter) {
             if (isCloudy && feelsLikeTemp < 10) {
                 if (message.contains("포근") || message.contains("온화")) {
                     message += " 그래도 흐린 날씨라 약간 쌀쌀할 수 있어요."
                 } else {
                     message = "하늘이 흐려 실제보다 더 쌀쌀하게 느껴지는 날씨예요."
                 }
             } else if (isClear && feelsLikeTemp < 0) {
                 message = "하늘은 맑지만 공기가 매우 차가우니 보온에 신경 쓰세요."
             }
        }

        // 바람 (겨울철 칼바람)
        if (isWinter && windSpeed >= 4.0) {
            if (feelsLikeTemp >= 5) {
                // 포근하다고 했지만 바람이 불면 -> 반전
                if (message.contains("온화") || message.contains("포근")) {
                    message = "기온은 높지만 찬 바람이 불어 체감온도는 낮아요."
                } else {
                    message += " 찬 바람 때문에 더 춥게 느껴질 수 있어요."
                }
            } else {
                // 이미 추운데 바람까지 불면 -> 강조
                // 흐림 멘트 등과 겹치지 않게 '칼바람' 키워드가 없을 때만 추가
                if (!message.contains("바람")) {
                    message += " 칼바람 때문에 체감온도가 뚝 떨어졌어요."
                }
            }
        }
        
        // 습도 (여름철 무더위)
        if (isSummer && humidity >= 70 && feelsLikeTemp >= 25) {
            message = "습도가 높아 불쾌지수가 높고 후텁지근해요."
        }
        
        // 일교차 (봄/가을철에 주로 체크, 겨울엔 중요도 낮춤)
        if (!isSummer && !isWinter && dailyRange >= 10) {
            // 메시지가 너무 길어지지 않게 체크
            if (message.length < 30) {
                message += " 일교차가 크니 감기 조심하세요."
            }
        }
        
        // 강풍 특보 (계절 무관하게 바람이 매우 강할 때)
        if (windSpeed >= 10.0) {
             message = "바람이 매우 강하게 불어요. 시설물 관리에 유의하세요."
        }

        return message
    }
}