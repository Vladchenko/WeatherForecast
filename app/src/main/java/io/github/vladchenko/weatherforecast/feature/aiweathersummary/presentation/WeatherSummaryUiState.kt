package io.github.vladchenko.weatherforecast.feature.aiweathersummary.presentation

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
     * @param summary AI summary text.
     * @param llm model name.
     */
    data class Remote(val summary: String, val llm: String) : WeatherSummaryUiState
    
    /**
     * Response from local cache on network error.
     *
     * @param llm model name.
     * @param summary text of the saved AI summary.
     * @param timeStamp timestamp of the last successful network request.
     * @param networkError network error description.
     */
    data class Local(
        val llm: String,
        val summary: String,
        val timeStamp: Long?,
        val networkError: String
    ) : WeatherSummaryUiState
    
    /**
     * Error when fetching the AI summary.
     *
     * @param city city name for which the forecast was requested.
     * @param messageId resource ID of the error message (may be `null`).
     */
    data class Error(
        val city: String?,
        val messageId: Int?,
    ) : WeatherSummaryUiState
}