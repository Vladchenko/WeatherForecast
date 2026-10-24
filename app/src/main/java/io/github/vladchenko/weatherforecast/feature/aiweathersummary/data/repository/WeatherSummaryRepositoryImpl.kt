package io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.repository

import io.github.vladchenko.weatherforecast.core.data.model.DataError
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.data.MarkdownParser
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.local.AISummaryLocalDataSource
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.remote.AISummaryRemoteDataSource
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.remote.ApiHttpException
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.mapper.DataErrorToAiSummaryErrorMapper
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryRequest
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.WeatherSummaryRepository
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.models.WeatherSummary
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.models.WeatherSummaryResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.UnknownHostException

/**
 * Implementation of [WeatherSummaryRepository] for fetching AI weather forecast results.
 *
 * First attempts to fetch data from the remote AI service. On error, attempts
 * to retrieve saved data from the local database.
 *
 * @param markdownParser parser for converting markdown to segments.
 * @param localDataSource data source from local database.
 * @param remoteDataSource data source from remote API.
 * @param coroutineDispatchers Coroutines dispatchers for managing execution threads.
 * @param errorMapper mapper for converting data-layer errors to domain-level errors.
 */
class WeatherSummaryRepositoryImpl(
    private val markdownParser: MarkdownParser,
    private val localDataSource: AISummaryLocalDataSource,
    private val remoteDataSource: AISummaryRemoteDataSource,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val errorMapper: DataErrorToAiSummaryErrorMapper,
) : WeatherSummaryRepository {

    override suspend fun fetchWeatherSummary(
        city: String,
        latitude: Double,
        longitude: Double,
        request: AISummaryRequest
    ): WeatherSummary = withContext(coroutineDispatchers.io) {
        var apiErrorMessage: String?
        
        try {
            val response = remoteDataSource.fetchWeatherSummary(request)
            
            // Check for API error
            response.error?.let { apiError ->
                return@withContext WeatherSummary.Error(
                    error = errorMapper.map(DataError.ServerError(0, apiError.message))
                )
            }
            
            val summaryText = response.choices.firstOrNull()?.message?.content
                ?: return@withContext WeatherSummary.Error(
                    error = errorMapper.map(DataError.ResponseNoBodyError)
                )

            // Cache the response
            localDataSource.saveWeatherSummary(
                city = city,
                llm = response.model,
                latitude = latitude,
                longitude = longitude,
                response = summaryText,
                timestamp = System.currentTimeMillis(),
                request = request.messages.joinToString("\n") { "${it.role}: ${it.content}" }
            )

            WeatherSummary.Remote(
                data = WeatherSummaryResponse(
                    city = city,
                    latitude = latitude,
                    longitude = longitude,
                    llm = response.model,
                    summary = markdownParser.parse(summaryText),
                )
            )
        } catch (ex: Exception) {
            if (ex is CancellationException) throw ex

            // Extract API error message if available
            apiErrorMessage = when (ex) {
                is ApiHttpException -> ex.message
                is HttpException -> ex.message()
                is UnknownHostException -> "Unable to connect to openrouter.ai. Check your internet connection."
                else -> null
            }

            // Try to get cached data
            try {
                val localSummary = localDataSource.fetchWeatherSummary(city, latitude, longitude)
                
                if (localSummary != null) {
                    return@withContext WeatherSummary.Local(
                        data = WeatherSummaryResponse(
                            city = city,
                            latitude = latitude,
                            longitude = longitude,
                            llm = localSummary.llm,
                            summary = markdownParser.parse(localSummary.summary),
                            timeStamp = localSummary.timestamp
                        ),
                        networkError = ex.message.toString()
                    )
                }
            } catch (ex: Exception) {
                // Check for Room schema validation error
                if (ex.message?.contains("Room cannot verify the data integrity") == true) {
                    return@withContext WeatherSummary.Error(
                        error = errorMapper.map(
                            DataError.DatabaseError("Room schema validation failed", ex)
                        )
                    )
                }
                // Database error, ignore and fall through
            }
            
            // No cached data and network failed
            return@withContext when (ex) {
                is ApiHttpException -> WeatherSummary.Error(
                    error = errorMapper.map(
                        DataError.ServerError(ex.statusCode, ex.message)
                    )
                )
                is HttpException -> WeatherSummary.Error(
                    error = errorMapper.map(
                        DataError.ServerError(ex.code(), ex.message())
                    )
                )
                else -> WeatherSummary.Error(
                    error = errorMapper.map(
                        DataError.NetworkError(ex)
                    )
                )
            }
        }
    }
}