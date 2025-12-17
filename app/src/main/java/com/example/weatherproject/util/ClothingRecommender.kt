package com.example.weatherproject.util

object ClothingRecommender {

    fun getRecommendation(
        feelsLikeTemp: Int,
        isRainy: Boolean = false,
        isWindy: Boolean = false
    ): List<String> {

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

        // 20도 미만일 때만 아우터를 추가합니다.
        if (feelsLikeTemp < 20) {
            recommendedItems.addAll(itemsByCategory[Category.OUTERWEAR] ?: emptyList())
        }

        return recommendedItems.map { it.name }.distinct()
    }
}
