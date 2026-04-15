package io.github.vladchenko.weatherforecast.data.repository

import io.github.vladchenko.weatherforecast.data.mapper.CitiesSearchDtoMapper
import io.github.vladchenko.weatherforecast.data.mapper.CitiesSearchEntityMapper
import io.github.vladchenko.weatherforecast.data.repository.datasource.CitiesNamesLocalDataSource
import io.github.vladchenko.weatherforecast.data.repository.datasource.CitiesNamesRemoteDataSource
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.domain.citiesnames.CitiesNamesRepository
import io.github.vladchenko.weatherforecast.models.data.DataResult
import io.github.vladchenko.weatherforecast.models.domain.CitiesNames
import io.github.vladchenko.weatherforecast.models.domain.ForecastError
import io.github.vladchenko.weatherforecast.models.domain.LoadResult
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
            when (val response = remoteDataSource.loadCitiesNames(token)) {
                is DataResult.Success -> {
                    val dtos = response.data
                    val entities = dtoMapper.toEntities(dtos)
                    localDataSource.saveCitiesNames(entities)
                    return@withContext LoadResult.Remote(entityMapper.toDomain(entities))
                }
                is DataResult.Error -> {
                    loadFromCacheOrError(token)
                }
            }
        }

    private suspend fun loadFromCacheOrError(token: String): LoadResult<CitiesNames> {
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