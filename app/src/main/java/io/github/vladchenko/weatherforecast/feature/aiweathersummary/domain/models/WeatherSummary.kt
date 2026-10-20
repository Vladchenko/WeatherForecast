package io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.models

import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError

/**
 * Result of the AI weather forecast summary request.
 */
sealed interface WeatherSummary {
    
    /**
     * Successful response from the remote AI service.
     *
     * @param data response with AI summary from the remote source.
     */
    data class Remote(val data: WeatherSummaryResponse) : WeatherSummary
    
    /**
     * Response from local cache on network error.
     *
     * @param data saved AI summary.
     * @param networkError network error description.
     */
    data class Local(
        val data: WeatherSummaryResponse,
        val networkError: String
    ) : WeatherSummary
    
    /**
     * Error when fetching the summary.
     *
     * @param error domain-level error describing what went wrong.
     */
    data class Error(val error: ForecastError) : WeatherSummary
}