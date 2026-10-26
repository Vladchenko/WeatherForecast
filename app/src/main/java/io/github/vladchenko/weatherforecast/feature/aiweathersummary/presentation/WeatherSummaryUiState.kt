package io.github.vladchenko.weatherforecast.feature.aiweathersummary.presentation

import io.github.vladchenko.weatherforecast.core.domain.model.MarkdownSegment

/**
 * UI state for displaying the AI weather forecast summary.
 */
sealed interface WeatherSummaryUiState {
    
    /**
     * Data loading state.
     */
    object Loading : WeatherSummaryUiState
    
    /**
     * Successfully fetched response from the remote AI service.
     *
     * @param summary AI summary as parsed markdown segments.
     * @param llm model name.
     */
    data class Remote(val summary: List<MarkdownSegment>, val llm: String) : WeatherSummaryUiState
    
    /**
     * Response from local cache on network error.
     *
     * @param llm model name.
     * @param summary parsed markdown segments of the saved AI summary.
     * @param timeStamp timestamp of the last successful network request.
     * @param networkError network error description.
     */
    data class Local(
        val llm: String,
        val summary: List<MarkdownSegment>,
        val timeStamp: Long?,
        val networkError: String
    ) : WeatherSummaryUiState
    
    /**
     * Error when fetching the AI summary.
     *
     * @param city city name for which the forecast was requested.
     * @param messageId resource ID of the error message.
     */
    data class Error(
        val city: String?,
        val messageId: Int,
    ) : WeatherSummaryUiState
}