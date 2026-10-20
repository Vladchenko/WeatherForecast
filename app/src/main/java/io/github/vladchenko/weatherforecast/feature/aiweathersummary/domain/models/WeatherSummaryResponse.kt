package io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.models

/**
 * Response with AI weather forecast summary.
 *
 * @param llm ai model name.
 * @param city city name.
 * @param latitude latitude.
 * @param longitude longitude
 * @param summary AI-generated weather forecast.
 * @param timeStamp timestamp of the cached forecast.
 */
data class WeatherSummaryResponse(
    val llm: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val summary: String,
    val timeStamp: Long? = null
)