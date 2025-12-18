package com.example.weatherproject.util

object ClothingRecommender {

    fun getRecommendation(
        feelsLikeTemp: Int,
        isRainy: Boolean = false,
        isWindy: Boolean = false
    ): List<Int> {

        // 1. 현재 날씨 상태를 기반으로 날씨 태그 세트를 생성합니다.
        val currentWeatherTags = mutableSetOf<WeatherTag>()
        when {
            feelsLikeTemp >= 28 -> currentWeatherTags.add(WeatherTag.VERY_HOT)
            feelsLikeTemp >= 23 -> currentWeatherTags.add(WeatherTag.HOT)
            feelsLikeTemp >= 17 -> currentWeatherTags.add(WeatherTag.WARM)
            feelsLikeTemp >= 12 -> currentWeatherTags.add(WeatherTag.COOL)
            feelsLikeTemp >= 9 -> currentWeatherTags.add(WeatherTag.CHILLY)
            feelsLikeTemp >= 5 -> currentWeatherTags.add(WeatherTag.COLD)
            else -> currentWeatherTags.add(WeatherTag.VERY_COLD)
        }
        if (isRainy) currentWeatherTags.add(WeatherTag.RAINY)
        if (isWindy) currentWeatherTags.add(WeatherTag.WINDY)

        // 2. 현재 날씨 태그와 하나라도 일치하는 모든 아이템을 필터링합니다.
        val allMatchingItems = ClothingDatabase.items.filter { item ->
            item.tags.any { it in currentWeatherTags }
        }

        // 3. 카테고리별로 아이템을 그룹화합니다.
        val itemsByCategory = allMatchingItems.groupBy { it.category }

        // 4. 최종 추천 목록을 조합합니다.
        val recommendedItems = mutableListOf<ClothingItem>()
        recommendedItems.addAll(itemsByCategory[Category.TOP] ?: emptyList())
        recommendedItems.addAll(itemsByCategory[Category.BOTTOM] ?: emptyList())
        recommendedItems.addAll(itemsByCategory[Category.ACCESSORY] ?: emptyList())

        // 5. 아우터 추천 로직 개선
        // 기존: if (feelsLikeTemp < 20) 무조건 추가
        // 변경: 날씨 태그에 맞는 아우터가 있다면 자연스럽게 추가됨.
        // 다만, 여름(VERY_HOT, HOT)에는 아우터가 추천되지 않도록 방어 로직 추가 가능
        if (feelsLikeTemp < 26) { // 26도 미만이면 아우터 고려 (얇은 가디건 등)
             recommendedItems.addAll(itemsByCategory[Category.OUTERWEAR] ?: emptyList())
        }

        return recommendedItems.map { it.nameResId }.distinct()
    }
}