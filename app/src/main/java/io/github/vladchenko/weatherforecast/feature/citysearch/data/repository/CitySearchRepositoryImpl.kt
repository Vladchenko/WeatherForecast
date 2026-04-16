package io.github.vladchenko.weatherforecast.feature.citysearch.data.repository

import io.github.vladchenko.weatherforecast.core.data.model.DataResult
import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.citysearch.data.mapper.CitySearchDtoMapper
import io.github.vladchenko.weatherforecast.feature.citysearch.data.mapper.CitySearchEntityMapper
import io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasource.local.CitySearchLocalDataSource
import io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasource.remote.CitySearchRemoteDataSource
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.CitySearchRepository
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CitySearch
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
class CitySearchRepositoryImpl(
    private val loggingService: LoggingService,
    private val dtoMapper: CitySearchDtoMapper,
    private val entityMapper: CitySearchEntityMapper,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val localDataSource: CitySearchLocalDataSource,
    private val remoteDataSource: CitySearchRemoteDataSource,
) : CitySearchRepository {

    override suspend fun loadCitiesNames(token: String): LoadResult<CitySearch> =
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

    private suspend fun loadFromCacheOrError(token: String): LoadResult<CitySearch> {
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