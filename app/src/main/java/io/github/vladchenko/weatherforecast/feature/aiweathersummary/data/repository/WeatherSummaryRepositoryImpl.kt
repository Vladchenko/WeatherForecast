package io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.repository

import io.github.vladchenko.weatherforecast.core.data.mapper.MarkdownParser
import io.github.vladchenko.weatherforecast.core.data.model.DataError
import io.github.vladchenko.weatherforecast.core.data.model.DataResult
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.local.AISummaryLocalDataSource
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.remote.WeatherSummaryRemoteDataSource
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.mapper.DataErrorToSummaryErrorMapper
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryRequest
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryResponse
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.WeatherSummaryRepository
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.models.WeatherSummary
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.models.WeatherSummaryResponse
import kotlinx.coroutines.withContext

/**
 * Implementation of [WeatherSummaryRepository] for fetching AI weather forecast results.
 *
 * First attempts to fetch data from the remote AI service. On error, attempts
 * to retrieve saved data from the local database.
 *
 * @param markdownParser parser for converting markdown to segments.
 * @param localDataSource data source from local database.
 * @param errorMapper mapper for converting data-layer errors to domain-level errors.
 * @param coroutineDispatchers coroutines dispatchers for managing execution threads.
 * @param remoteDataSource data source from remote API.
 */
class WeatherSummaryRepositoryImpl(
    private val markdownParser: MarkdownParser,
    private val localDataSource: AISummaryLocalDataSource,
    private val errorMapper: DataErrorToSummaryErrorMapper,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val remoteDataSource: WeatherSummaryRemoteDataSource,
) : WeatherSummaryRepository {

    override suspend fun fetchWeatherSummary(
        city: String,
        latitude: Double,
        longitude: Double,
        request: AISummaryRequest
    ): WeatherSummary = withContext(coroutineDispatchers.io) {
        when (val result = remoteDataSource.fetchWeatherSummary(city, request)) {
            is DataResult.Success -> handleRemoteSuccess(
                result.data,
                city,
                latitude,
                longitude,
                request
            )

            is DataResult.Error -> handleRemoteError(result.error, city, latitude, longitude)
        }
    }

    private suspend fun handleRemoteError(
        error: DataError,
        city: String,
        latitude: Double,
        longitude: Double
    ): WeatherSummary {
        val localSummary = try {
            localDataSource.fetchWeatherSummary(city, latitude, longitude)
        } catch (_: Exception) {
            null
        }

        if (localSummary != null) {
            return WeatherSummary.Local(
                data = WeatherSummaryResponse(
                    city = city,
                    latitude = latitude,
                    longitude = longitude,
                    llm = localSummary.llm,
                    // It's an overhead to use Dispatchers.Default here
                    summary = markdownParser.parse(localSummary.summary),
                    timeStamp = localSummary.timestamp
                ),
                networkError = errorMapper.map(error)
            )
        }

        return WeatherSummary.Error(
            error = errorMapper.map(error, cacheAvailable = false)
        )
    }

    private suspend fun handleRemoteSuccess(
        response: AISummaryResponse,
        city: String,
        latitude: Double,
        longitude: Double,
        request: AISummaryRequest
    ): WeatherSummary {
        response.error?.let {
            return handleApiErrorWithCacheFallback(city, latitude, longitude)
        }

        val summaryText = response.choices.firstOrNull()?.message?.content
            ?: return handleEmptyResponseWithCacheFallback(city, latitude, longitude)

        return saveAndReturnRemoteSummary(response, city, latitude, longitude, request, summaryText)
    }

    private suspend fun handleApiErrorWithCacheFallback(
        city: String,
        latitude: Double,
        longitude: Double
    ): WeatherSummary {
        val localSummary = try {
            localDataSource.fetchWeatherSummary(city, latitude, longitude)
        } catch (_: Exception) {
            null
        }

        if (localSummary != null) {
            return WeatherSummary.Local(
                data = WeatherSummaryResponse(
                    city = city,
                    latitude = latitude,
                    longitude = longitude,
                    llm = localSummary.llm,
                    summary = markdownParser.parse(localSummary.summary),
                    timeStamp = localSummary.timestamp
                ),
                networkError = errorMapper.map(DataError.ServerError(0))
            )
        }

        return WeatherSummary.Error(
            error = errorMapper.map(DataError.ServerError(0))
        )
    }

    private suspend fun handleEmptyResponseWithCacheFallback(
        city: String,
        latitude: Double,
        longitude: Double
    ): WeatherSummary {
        val localSummary = try {
            localDataSource.fetchWeatherSummary(city, latitude, longitude)
        } catch (_: Exception) {
            null
        }

        if (localSummary != null) {
            return WeatherSummary.Local(
                data = WeatherSummaryResponse(
                    city = city,
                    latitude = latitude,
                    longitude = longitude,
                    llm = localSummary.llm,
                    summary = markdownParser.parse(localSummary.summary),
                    timeStamp = localSummary.timestamp
                ),
                networkError = errorMapper.map(DataError.ResponseNoBodyError)
            )
        }

        return WeatherSummary.Error(
            error = errorMapper.map(DataError.ResponseNoBodyError)
        )
    }

    private suspend fun saveAndReturnRemoteSummary(
        response: AISummaryResponse,
        city: String,
        latitude: Double,
        longitude: Double,
        request: AISummaryRequest,
        summaryText: String
    ): WeatherSummary {
        localDataSource.saveWeatherSummary(
            city = city,
            llm = response.model,
            latitude = latitude,
            longitude = longitude,
            response = summaryText,
            timestamp = System.currentTimeMillis(),
            request = request.messages.joinToString("\n") { "${it.role}: ${it.content}" }
        )

        return WeatherSummary.Remote(
            data = WeatherSummaryResponse(
                city = city,
                latitude = latitude,
                longitude = longitude,
                llm = response.model,
                summary = markdownParser.parse(summaryText),
            )
        )
    }

}