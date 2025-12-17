package com.example.weatherproject.util

object ClothingDatabase {

    val items = listOf(
        // 상의 (TOP)
        ClothingItem("민소매", Category.TOP, setOf(WeatherTag.VERY_HOT)),
        ClothingItem("반팔 티셔츠", Category.TOP, setOf(WeatherTag.VERY_HOT, WeatherTag.HOT)),
        ClothingItem("얇은 셔츠", Category.TOP, setOf(WeatherTag.HOT, WeatherTag.WARM)),
        ClothingItem("긴팔 티셔츠", Category.TOP, setOf(WeatherTag.HOT, WeatherTag.WARM, WeatherTag.COOL)),
        ClothingItem("맨투맨", Category.TOP, setOf(WeatherTag.WARM, WeatherTag.COOL, WeatherTag.CHILLY)),
        ClothingItem("니트", Category.TOP, setOf(WeatherTag.COOL, WeatherTag.CHILLY, WeatherTag.COLD)),
        ClothingItem("기모 후드티", Category.TOP, setOf(WeatherTag.CHILLY, WeatherTag.COLD)),
        ClothingItem("히트텍", Category.TOP, setOf(WeatherTag.COLD, WeatherTag.VERY_COLD)),

        // 하의 (BOTTOM)
        ClothingItem("반바지", Category.BOTTOM, setOf(WeatherTag.VERY_HOT, WeatherTag.HOT)),
        ClothingItem("면바지", Category.BOTTOM, setOf(WeatherTag.HOT, WeatherTag.WARM, WeatherTag.COOL)),
        ClothingItem("청바지", Category.BOTTOM, setOf(WeatherTag.WARM, WeatherTag.COOL, WeatherTag.CHILLY)),
        ClothingItem("슬랙스", Category.BOTTOM, setOf(WeatherTag.WARM, WeatherTag.COOL)),
        ClothingItem("기모 바지", Category.BOTTOM, setOf(WeatherTag.COLD, WeatherTag.VERY_COLD)),

        // 아우터 (OUTERWEAR)
        ClothingItem("얇은 가디건", Category.OUTERWEAR, setOf(WeatherTag.WARM)),
        ClothingItem("자켓", Category.OUTERWEAR, setOf(WeatherTag.COOL, WeatherTag.WINDY)),
        ClothingItem("야상", Category.OUTERWEAR, setOf(WeatherTag.COOL, WeatherTag.CHILLY, WeatherTag.WINDY)),
        ClothingItem("트렌치코트", Category.OUTERWEAR, setOf(WeatherTag.COOL, WeatherTag.CHILLY)),
        ClothingItem("코트", Category.OUTERWEAR, setOf(WeatherTag.COLD)),
        ClothingItem("경량 패딩", Category.OUTERWEAR, setOf(WeatherTag.COLD, WeatherTag.VERY_COLD)),
        ClothingItem("두꺼운 패딩", Category.OUTERWEAR, setOf(WeatherTag.VERY_COLD, WeatherTag.SNOWY)),
        ClothingItem("바람막이", Category.OUTERWEAR, setOf(WeatherTag.WARM, WeatherTag.COOL, WeatherTag.WINDY)),
        
        // 액세서리 (ACCESSORY)
        ClothingItem("샌들", Category.ACCESSORY, setOf(WeatherTag.VERY_HOT)),
        ClothingItem("모자", Category.ACCESSORY, setOf(WeatherTag.UV_STRONG)),
        ClothingItem("목도리", Category.ACCESSORY, setOf(WeatherTag.VERY_COLD)),
        ClothingItem("장갑", Category.ACCESSORY, setOf(WeatherTag.VERY_COLD)),
        ClothingItem("우산", Category.ACCESSORY, setOf(WeatherTag.RAINY)),
        ClothingItem("부츠", Category.ACCESSORY, setOf(WeatherTag.RAINY, WeatherTag.SNOWY, WeatherTag.COLD))
    )
}
