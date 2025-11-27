package com.example.weatherproject.data

data class CctvInfo(
    val id: String,
    val roadName: String,
    val distance: String, // 예: "0.5km"
    val thumbnailUrl: String,
    val videoUrl: String // 실제 재생 시 사용할 URL
)
