package com.example.weatherproject.util

/**
 * 날씨 상태나 옷의 속성을 나타내는 태그
 */
enum class WeatherTag {
    // 온도 관련
    VERY_HOT,  // 28도 이상
    HOT,       // 23~27도
    WARM,      // 17~22도
    COOL,      // 12~16도
    CHILLY,    // 9~11도
    COLD,      // 5~8도
    VERY_COLD, // 4도 이하

    // 특수 조건
    RAINY,
    SNOWY,
    WINDY,
    UV_STRONG // 자외선 강함
}

/**
 * 옷의 분류
 */
enum class Category {
    TOP,
    BOTTOM,
    OUTERWEAR,
    ACCESSORY
}

/**
 * 옷 아이템의 데이터 구조
 * @param name 옷 이름 (예: "반팔 티셔츠")
 * @param category 옷 분류 (예: TOP)
 * @param tags 이 옷이 적합한 날씨 태그 목록
 */
data class ClothingItem(
    val nameResId: Int,
    val category: Category,
    val tags: Set<WeatherTag>
)
