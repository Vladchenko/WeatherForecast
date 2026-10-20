package io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.remote

import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryRequest
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryResponse

/**
 * Interface for working with remote storage of AI weather forecast results.
 * Provides a method for fetching AI analysis from the remote API.
 */
interface AISummaryRemoteDataSource {

    /**
     * Requests AI weather forecast analysis for the specified city.
     *
     * @param request request with city and coordinate parameters.
     * @return response from the AI model.
     */
    suspend fun fetchWeatherSummary(request: AISummaryRequest): AISummaryResponse
}