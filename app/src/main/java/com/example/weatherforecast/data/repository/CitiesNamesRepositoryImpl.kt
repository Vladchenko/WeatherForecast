package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.mapper.CitiesSearchDtoMapper
import com.example.weatherforecast.data.mapper.CitiesSearchEntityMapper
import com.example.weatherforecast.data.repository.datasource.CitiesNamesLocalDataSource
import com.example.weatherforecast.data.repository.datasource.CitiesNamesRemoteDataSource
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.citiesnames.CitiesNamesRepository
import com.example.weatherforecast.models.domain.CitiesNames
import com.example.weatherforecast.models.domain.ForecastError
import com.example.weatherforecast.models.domain.LoadResult
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi

/**
 * CitiesNamesRepository implementation to retrieve cities names.
 *
 * @property loggingService to log events
 * @property dtoMapper mapper to convert dto to entity
 * @property entityMapper mapper to convert entity to model
 * @property coroutineDispatchers dispatchers for coroutines
 * @property localDataSource to download cities names from database
 * @property remoteDataSource to download cities names remotely
 */
@InternalSerializationApi
class CitiesNamesRepositoryImpl(
    private val loggingService: LoggingService,
    private val dtoMapper: CitiesSearchDtoMapper,
    private val entityMapper: CitiesSearchEntityMapper,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val localDataSource: CitiesNamesLocalDataSource,
    private val remoteDataSource: CitiesNamesRemoteDataSource,
) : CitiesNamesRepository {

    override suspend fun loadCitiesNames(token: String): LoadResult<CitiesNames> =
        withContext(coroutineDispatchers.io) {
            try {
                val response = remoteDataSource.loadCitiesNames(token)
                if (response.isSuccessful && response.body() != null) {
                    val dtos = response.body()!!
                    val entities = dtoMapper.toEntities(dtos)
                    localDataSource.saveCitiesNames(entities)
                    return@withContext LoadResult.Remote(entityMapper.toDomain(entities))
                } else {
                    loadFromCacheOrThrow(token)
                }
            } catch (ex: Exception) {
                loggingService.logError(TAG, "Error loading cities names", ex)
                loadFromCacheOrThrow(token)
            }
        }

    private suspend fun loadFromCacheOrThrow(token: String): LoadResult<CitiesNames> {
        val cachedEntities = localDataSource.loadCitiesNames(token)
        if (cachedEntities.isEmpty()) {
            return LoadResult.Error(
                token, ForecastError.NoDataAvailable("No cities match '$token' and no internet")
            )
        }
        return LoadResult.Local(
            entityMapper.toDomain(cachedEntities),
            ForecastError.NoDataAvailable("Remote request failed")
        )
    }

    override suspend fun deleteAllCitiesNames() =
        withContext(coroutineDispatchers.io) {
            localDataSource.deleteAllCitiesNames()
        }

    companion object {
        const val TAG = "CitiesNamesRepositoryImpl"
    }
}