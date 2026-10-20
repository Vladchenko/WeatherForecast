package io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain

import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryRequest
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.models.WeatherSummary

/**
 * Repository interface for fetching AI weather forecast summaries.
 *
 * Defines the contract for requesting AI weather forecast analysis for a specified city.
 */
interface WeatherSummaryRepository {

    /**
     * Requests AI weather forecast analysis for the specified city.
     *
     * @param city city name.
     * @param latitude latitude.
     * @param longitude longitude.
     * @param request request to the AI model.
     * @return result as [WeatherSummary] (Remote, Local, or Error).
     */
    suspend fun fetchWeatherSummary(
        city: String,
        latitude: Double,
        longitude: Double,
        request: AISummaryRequest
    ): WeatherSummary
}