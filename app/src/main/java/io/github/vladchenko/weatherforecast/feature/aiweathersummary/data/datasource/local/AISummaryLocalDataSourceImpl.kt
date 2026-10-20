package io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.local

import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.CachedWeatherSummary

/**
 * Implementation of [AISummaryLocalDataSource] for working with the local database.
 * Uses Room DAO for saving and retrieving AI weather forecast results.
 *
 * @param dao Data Access Object for accessing AI summary database.
 */
class AISummaryLocalDataSourceImpl(
    private val dao: AISummaryDAO,
) : AISummaryLocalDataSource {

    /**
     * Fetches the saved AI response for the specified city from the database.
     */
    override suspend fun fetchWeatherSummary(
        city: String,
        latitude: Double,
        longitude: Double
    ): CachedWeatherSummary? {
        return dao.fetchWeatherSummaryWithTimestamp(city, latitude, longitude)
    }

    /**
     * Saves the AI model interaction result to the database.
     * Success status is determined by the presence of a response (`true` if response is not null).
     */
    override suspend fun saveWeatherSummary(
        llm: String,
        city: String,
        latitude: Double,
        longitude: Double,
        request: String,
        response: String?,
        timestamp: Long
    ) {
        dao.saveWeatherSummary(llm, city, latitude, longitude, request, response, timestamp, response != null)
    }
}