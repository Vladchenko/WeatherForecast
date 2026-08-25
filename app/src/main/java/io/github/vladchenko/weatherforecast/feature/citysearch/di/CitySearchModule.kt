package io.github.vladchenko.weatherforecast.feature.citysearch.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.di.DiConstants.WEATHER_RETROFIT_NAME
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.data.database.WeatherForecastDatabase
import io.github.vladchenko.weatherforecast.data.util.ResponseProcessor
import io.github.vladchenko.weatherforecast.feature.citysearch.data.api.CitySearchApiService
import io.github.vladchenko.weatherforecast.feature.citysearch.data.mapper.CitySearchDtoMapper
import io.github.vladchenko.weatherforecast.feature.citysearch.data.mapper.CitySearchEntityMapper
import io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.CitySearchRepositoryImpl
import io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasource.local.CitySearchDAO
import io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasource.local.CitySearchLocalDataSource
import io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasource.remote.CitySearchRemoteDataSource
import io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasourceimpl.CitySearchLocalDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasourceimpl.CitySearchRemoteDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.CitySearchInteractor
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.CitySearchRepository
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Dagger module for providing dependencies related to the City Search feature.
 *
 * This module defines how components such as data sources, mappers, repository,
 * interactor, and view model factory are created and injected within the application.
 * All bindings are scoped to the SingletonComponent to ensure single instances
 * across the app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class CitySearchModule {

    @Provides
    @Singleton

    fun provideCitySearchDAO(database: WeatherForecastDatabase): CitySearchDAO {
        return database.getCitySearchInstance()
    }

    @Singleton
    @Provides
    fun provideCityApiService(@Named(WEATHER_RETROFIT_NAME) retrofit: Retrofit): CitySearchApiService {
        return retrofit.create(CitySearchApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideCitySearchDtoMapper(): CitySearchDtoMapper {
        return CitySearchDtoMapper()
    }

    @Singleton
    @Provides
    fun provideCitySearchEntityMapper(): CitySearchEntityMapper {
        return CitySearchEntityMapper()
    }

    @Singleton
    @Provides
    fun provideCitySearchLocalDataSource(
        loggingService: LoggingService,
        dao: CitySearchDAO
    ): CitySearchLocalDataSource {
        return CitySearchLocalDataSourceImpl(dao, loggingService)
    }

    @Singleton
    @Provides
    fun provideCitySearchRemoteDataSource(
        citySearchApiService: CitySearchApiService,
        responseProcessor: ResponseProcessor,
        loggingService: LoggingService
    ): CitySearchRemoteDataSource {
        return CitySearchRemoteDataSourceImpl(
            citySearchApiService,
            loggingService,
            responseProcessor
        )
    }

    @Singleton
    @Provides
    fun provideCitySearchRepository(
        dtoMapper: CitySearchDtoMapper,
        entityMapper: CitySearchEntityMapper,
        coroutineDispatchers: CoroutineDispatchers,
        citySearchLocalDataSource: CitySearchLocalDataSource,
        citySearchRemoteDataSource: CitySearchRemoteDataSource
    ): CitySearchRepository {
        return CitySearchRepositoryImpl(
            dtoMapper,
            entityMapper,
            coroutineDispatchers,
            citySearchLocalDataSource,
            citySearchRemoteDataSource,
        )
    }

    @Singleton
    @Provides
    fun provideCitySearchInteractor(citySearchRepository: CitySearchRepository): CitySearchInteractor {
        return CitySearchInteractor(citySearchRepository)
    }
}