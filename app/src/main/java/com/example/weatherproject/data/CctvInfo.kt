package com.example.weatherproject.data

data class CctvInfo(
    val cctvName: String,
    val cctvUrl: String,
    val type: String,
    val roadName: String,
    val distance: String,
    val latitude: String = "",   // 추가
    val longitude: String = ""   // 추가
)