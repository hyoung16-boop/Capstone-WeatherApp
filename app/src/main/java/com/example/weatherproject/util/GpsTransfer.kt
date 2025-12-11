package com.example.weatherproject.util

import kotlin.math.*

/**
 * GPS 위도/경도 → 기상청 격자 좌표(nx, ny) 변환
 */
object GpsTransfer {

    private const val RE = 6371.00877 // 지구 반경(km)
    private const val GRID = 5.0      // 격자 간격(km)
    private const val SLAT1 = 30.0    // 투영 위도1(degree)
    private const val SLAT2 = 60.0    // 투영 위도2(degree)
    private const val OLON = 126.0    // 기준점 경도(degree)
    private const val OLAT = 38.0     // 기준점 위도(degree)
    private const val XO = 43.0       // 기준점 X좌표(GRID)
    private const val YO = 136.0      // 기준점 Y좌표(GRID)

    /**
     * GPS 좌표를 기상청 격자 좌표로 변환
     * @param lat 위도 (latitude)
     * @param lon 경도 (longitude)
     * @return Pair<nx, ny> 격자 좌표
     */
    fun convertToGrid(lat: Double, lon: Double): Pair<Int, Int> {
        val DEGRAD = PI / 180.0

        val re = RE / GRID
        val slat1 = SLAT1 * DEGRAD
        val slat2 = SLAT2 * DEGRAD
        val olon = OLON * DEGRAD
        val olat = OLAT * DEGRAD

        var sn = tan(PI * 0.25 + slat2 * 0.5) / tan(PI * 0.25 + slat1 * 0.5)
        sn = ln(cos(slat1) / cos(slat2)) / ln(sn)
        var sf = tan(PI * 0.25 + slat1 * 0.5)
        sf = sf.pow(sn) * cos(slat1) / sn
        var ro = tan(PI * 0.25 + olat * 0.5)
        ro = re * sf / ro.pow(sn)

        var ra = tan(PI * 0.25 + lat * DEGRAD * 0.5)
        ra = re * sf / ra.pow(sn)
        var theta = lon * DEGRAD - olon
        if (theta > PI) theta -= 2.0 * PI
        if (theta < -PI) theta += 2.0 * PI
        theta *= sn

        val nx = (ra * sin(theta) + XO + 0.5).toInt()
        val ny = (ro - ra * cos(theta) + YO + 0.5).toInt()

        return Pair(nx, ny)
    }
}