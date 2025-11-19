package com.example.weatherproject.util

import java.time.LocalDate
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt

object FeelsLikeTempCalculator {

    /**
     * Calculates the feels-like temperature based on current weather conditions and month.
     *
     * @param Ta Air temperature in Celsius (°C).
     * @param RH Relative humidity in percent (%).
     * @param windSpeedKmh Wind speed in kilometers per hour (km/h).
     * @return The calculated feels-like temperature in Celsius (°C).
     */
    fun calculate(Ta: Double, RH: Double, windSpeedKmh: Double): Double {
        val currentMonth = LocalDate.now().monthValue

        return if (currentMonth in 5..9) {
            // Summer (May to September)
            calculateSummerFeelsLike(Ta, RH)
        } else {
            // Winter (October to April)
            val windSpeedMs = windSpeedKmh * 1000 / 3600 // Convert km/h to m/s
            if (Ta <= 10 && windSpeedMs >= 1.3) {
                calculateWinterFeelsLike(Ta, windSpeedKmh)
            } else {
                Ta // If conditions for winter formula are not met, return air temperature.
            }
        }
    }

    /**
     * Calculates summer feels-like temperature.
     * Formula: -0.2442 + 0.55399*Tw + 0.45535*Ta - 0.0022*Tw^2 + 0.00278*Tw*Ta + 3.0
     */
    private fun calculateSummerFeelsLike(Ta: Double, RH: Double): Double {
        val Tw = calculateWetBulbTemp(Ta, RH)
        val feelsLike = -0.2442 + (0.55399 * Tw) + (0.45535 * Ta) - (0.0022 * Tw.pow(2)) + (0.00278 * Tw * Ta) + 3.0
        return feelsLike
    }

    /**
     * Calculates wet-bulb temperature (Tw) using Stull's formula.
     * Tw = Ta*ATAN[0.151977(RH+8.313659)^1/2] + ATAN(Ta+RH) - ATAN(RH-1.67633) + 0.00391838*RH^1.5*ATAN(0.023101*RH) - 4.686035
     */
    private fun calculateWetBulbTemp(Ta: Double, RH: Double): Double {
        val term1 = Ta * atan(0.151977 * (RH + 8.313659).pow(0.5))
        val term2 = atan(Ta + RH)
        val term3 = atan(RH - 1.67633)
        val term4 = 0.00391838 * RH.pow(1.5) * atan(0.023101 * RH)
        val term5 = 4.686035
        return term1 + term2 - term3 + term4 - term5
    }

    /**
     * Calculates winter feels-like temperature (wind chill).
     * Formula: 13.12 + 0.6215*Ta - 11.37*V^0.16 + 0.3965*V^0.16*Ta
     * @param V Wind speed in km/h.
     */
    private fun calculateWinterFeelsLike(Ta: Double, V: Double): Double {
        val vPow = V.pow(0.16)
        val feelsLike = 13.12 + (0.6215 * Ta) - (11.37 * vPow) + (0.3965 * vPow * Ta)
        return feelsLike
    }
}
