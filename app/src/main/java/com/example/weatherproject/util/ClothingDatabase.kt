package com.example.weatherproject.util

import com.example.weatherproject.R

object ClothingDatabase {

    val items = listOf(
        // 상의 (TOP)
        ClothingItem(R.string.sleeveless, Category.TOP, setOf(WeatherTag.VERY_HOT)),
        ClothingItem(R.string.tshirt_short, Category.TOP, setOf(WeatherTag.VERY_HOT, WeatherTag.HOT)),
        ClothingItem(R.string.shirt_thin, Category.TOP, setOf(WeatherTag.HOT, WeatherTag.WARM)),
        ClothingItem(R.string.tshirt_long, Category.TOP, setOf(WeatherTag.HOT, WeatherTag.WARM, WeatherTag.COOL)),
        ClothingItem(R.string.sweatshirt, Category.TOP, setOf(WeatherTag.WARM, WeatherTag.COOL, WeatherTag.CHILLY)),
        ClothingItem(R.string.knitwear, Category.TOP, setOf(WeatherTag.COOL, WeatherTag.CHILLY, WeatherTag.COLD)),
        ClothingItem(R.string.hoodie_fleece, Category.TOP, setOf(WeatherTag.CHILLY, WeatherTag.COLD)),
        ClothingItem(R.string.thermal_underwear, Category.TOP, setOf(WeatherTag.COLD, WeatherTag.VERY_COLD)),

        // 하의 (BOTTOM)
        ClothingItem(R.string.shorts, Category.BOTTOM, setOf(WeatherTag.VERY_HOT, WeatherTag.HOT)),
        ClothingItem(R.string.pants_cotton, Category.BOTTOM, setOf(WeatherTag.HOT, WeatherTag.WARM, WeatherTag.COOL)),
        ClothingItem(R.string.jeans, Category.BOTTOM, setOf(WeatherTag.WARM, WeatherTag.COOL, WeatherTag.CHILLY)),
        ClothingItem(R.string.slacks, Category.BOTTOM, setOf(WeatherTag.WARM, WeatherTag.COOL)),
        ClothingItem(R.string.pants_fleece, Category.BOTTOM, setOf(WeatherTag.COLD, WeatherTag.VERY_COLD)),

        // 아우터 (OUTERWEAR)
        ClothingItem(R.string.cardigan_thin, Category.OUTERWEAR, setOf(WeatherTag.WARM)),
        ClothingItem(R.string.jacket, Category.OUTERWEAR, setOf(WeatherTag.COOL, WeatherTag.WINDY)),
        ClothingItem(R.string.field_jacket, Category.OUTERWEAR, setOf(WeatherTag.COOL, WeatherTag.CHILLY, WeatherTag.WINDY)),
        ClothingItem(R.string.trench_coat, Category.OUTERWEAR, setOf(WeatherTag.COOL, WeatherTag.CHILLY)),
        ClothingItem(R.string.coat, Category.OUTERWEAR, setOf(WeatherTag.COLD)),
        ClothingItem(R.string.padding_light, Category.OUTERWEAR, setOf(WeatherTag.COLD, WeatherTag.VERY_COLD)),
        ClothingItem(R.string.padding_heavy, Category.OUTERWEAR, setOf(WeatherTag.VERY_COLD, WeatherTag.SNOWY)),
        ClothingItem(R.string.windbreaker, Category.OUTERWEAR, setOf(WeatherTag.WARM, WeatherTag.COOL, WeatherTag.WINDY)),
        
        // 액세서리 (ACCESSORY)
        ClothingItem(R.string.sandals, Category.ACCESSORY, setOf(WeatherTag.VERY_HOT)),
        ClothingItem(R.string.hat, Category.ACCESSORY, setOf(WeatherTag.UV_STRONG)),
        ClothingItem(R.string.scarf, Category.ACCESSORY, setOf(WeatherTag.VERY_COLD)),
        ClothingItem(R.string.gloves, Category.ACCESSORY, setOf(WeatherTag.VERY_COLD)),
        ClothingItem(R.string.umbrella, Category.ACCESSORY, setOf(WeatherTag.RAINY)),
        ClothingItem(R.string.boots, Category.ACCESSORY, setOf(WeatherTag.RAINY, WeatherTag.SNOWY, WeatherTag.COLD))
    )
}