package com.example.weatherproject.util

import com.example.weatherproject.data.Recommendation
import com.example.weatherproject.data.RecommendationRule

object RecommendationService {

    // Placeholder rules. These can be expanded and refined later.
    private val recommendationRules = listOf(
        RecommendationRule(
            tempRange = 28.0..Double.MAX_VALUE,
            recommendation = Recommendation(
                clothing = "민소매, 반팔, 반바지, 린넨 옷",
                life = "야외 활동은 자제하고, 물을 충분히 섭취하세요."
            )
        ),
        RecommendationRule(
            tempRange = 23.0..27.9,
            recommendation = Recommendation(
                clothing = "반팔, 얇은 셔츠, 반바지, 면바지",
                life = "가벼운 야외 활동을 즐기기 좋은 날씨입니다."
            )
        ),
        RecommendationRule(
            tempRange = 17.0..22.9,
            recommendation = Recommendation(
                clothing = "맨투맨, 가디건, 긴팔, 슬랙스",
                life = "산책이나 피크닉 등 나들이하기 좋은 날입니다."
            )
        ),
        RecommendationRule(
            tempRange = 10.0..16.9,
            recommendation = Recommendation(
                clothing = "자켓, 트렌치코트, 니트, 청바지",
                life = "일교차가 클 수 있으니 겉옷을 꼭 챙기세요."
            )
        ),
        RecommendationRule(
            tempRange = 5.0..9.9,
            recommendation = Recommendation(
                clothing = "코트, 가죽자켓, 두꺼운 니트",
                life = "쌀쌀한 날씨, 감기 조심하세요."
            )
        ),
        RecommendationRule(
            tempRange = Double.MIN_VALUE..4.9,
            recommendation = Recommendation(
                clothing = "패딩, 두꺼운 코트, 목도리, 기모 제품",
                life = "실내 활동 위주로 계획하고, 동파에 유의하세요."
            )
        )
    )

    private val defaultRecommendation = Recommendation(
        clothing = "적절한 옷차림을 준비하세요.",
        life = "즐거운 하루 보내세요."
    )

    /**
     * Finds and returns a recommendation based on the feels-like temperature.
     * @param feelsLikeTemp The feels-like temperature.
     * @return The matching Recommendation, or a default one if no rule matches.
     */
    fun getRecommendations(feelsLikeTemp: Double): Recommendation {
        return recommendationRules.firstOrNull { feelsLikeTemp in it.tempRange }?.recommendation
            ?: defaultRecommendation
    }
}
