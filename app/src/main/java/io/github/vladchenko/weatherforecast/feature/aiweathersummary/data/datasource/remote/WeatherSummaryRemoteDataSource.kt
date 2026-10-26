package io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.remote

import io.github.vladchenko.weatherforecast.core.data.model.DataResult
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryRequest
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryResponse

/**
 * Interface for working with remote storage of AI weather forecast results.
 * Provides a method for fetching AI analysis from the remote API.
 */
interface WeatherSummaryRemoteDataSource {

    /**
     * Requests AI weather forecast analysis for the specified city.
     *
     * @param city name to fetch the weather forecast for.
     * @param request request to llm.
     * @return response from the AI model, or a [DataResult.Error] on failure.
     */
    suspend fun fetchWeatherSummary(city: String, request: AISummaryRequest): DataResult<AISummaryResponse>
}