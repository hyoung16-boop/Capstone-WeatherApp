package com.example.weatherproject.data

/**
 * Represents clothing and lifestyle recommendations.
 * @param clothing Recommendation for clothing.
 * @param life Recommendation for lifestyle/activities.
 */
data class Recommendation(
    val clothing: String,
    val life: String
)

/**
 * Defines a rule that maps a temperature range to a specific recommendation.
 * @param tempRange The temperature range for which this rule applies.
 * @param recommendation The Recommendation to be given for this temperature range.
 */
data class RecommendationRule(
    val tempRange: ClosedFloatingPointRange<Double>,
    val recommendation: Recommendation
)
