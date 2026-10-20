package io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.local

import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.CachedWeatherSummary

/**
 * Interface for working with local storage of AI weather forecast results.
 * Provides methods for saving and retrieving AI analysis.
 */
interface AISummaryLocalDataSource {

    /**
     * Fetches the saved AI response for the specified city.
     *
     * @param city city name.
     * @param latitude latitude.
     * @param longitude longitude.
     * @return AI response text or `null` if record not found.
     */
    suspend fun fetchWeatherSummary(
        city: String,
        latitude: Double,
        longitude: Double
    ): CachedWeatherSummary?

    /**
     * Saves the AI model interaction result for the specified city.
     *
     * @param llm LLM model identifier.
     * @param city city name.
     * @param latitude latitude.
     * @param longitude longitude.
     * @param request request text sent to the model.
     * @param response model response (may be `null` on error).
     * @param timestamp request timestamp (default: current time).
     */
    suspend fun saveWeatherSummary(
        llm: String,
        city: String,
        latitude: Double,
        longitude: Double,
        request: String,
        response: String?,
        timestamp: Long = System.currentTimeMillis()
    )
}