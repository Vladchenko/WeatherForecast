package io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.models

import io.github.vladchenko.weatherforecast.core.domain.model.MarkdownSegment

/**
 * Response with AI weather forecast summary.
 *
 * @param llm ai model name.
 * @param city city name.
 * @param latitude latitude.
 * @param longitude longitude
 * @param summary AI-generated weather forecast as parsed markdown segments.
 * @param timeStamp timestamp of the cached forecast.
 */
data class WeatherSummaryResponse(
    val llm: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val summary: List<MarkdownSegment>,
    val timeStamp: Long? = null
)