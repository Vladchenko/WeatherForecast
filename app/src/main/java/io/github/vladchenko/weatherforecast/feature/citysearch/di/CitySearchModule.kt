package io.github.vladchenko.weatherforecast.feature.citysearch.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.di.DiConstants.WEATHER_RETROFIT_NAME
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
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
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.viewmodel.CitySearchViewModelFactory
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.RecentCitiesInteractor
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import kotlinx.serialization.InternalSerializationApi
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
    @InternalSerializationApi
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
    @InternalSerializationApi
    fun provideCitySearchDtoMapper(): CitySearchDtoMapper {
        return CitySearchDtoMapper()
    }

    @Singleton
    @Provides
    @InternalSerializationApi
    fun provideCitySearchEntityMapper(): CitySearchEntityMapper {
        return CitySearchEntityMapper()
    }

    @InternalSerializationApi
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
    @InternalSerializationApi
    fun provideCitySearchRepository(
        loggingService: LoggingService,
        dtoMapper: CitySearchDtoMapper,
        entityMapper: CitySearchEntityMapper,
        coroutineDispatchers: CoroutineDispatchers,
        citySearchLocalDataSource: CitySearchLocalDataSource,
        citySearchRemoteDataSource: CitySearchRemoteDataSource
    ): CitySearchRepository {
        return CitySearchRepositoryImpl(
            loggingService,
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

    @Singleton
    @Provides
    fun provideCitySearchViewModelFactory(
        loggingService: LoggingService,
        statusRenderer: StatusRenderer,
        resourceManager: ResourceManager,
        connectivityObserver: ConnectivityObserver,
        citySearchInteractor: CitySearchInteractor,
        recentCitiesInteractor: RecentCitiesInteractor
    ): CitySearchViewModelFactory {
        return CitySearchViewModelFactory(
            loggingService,
            statusRenderer,
            resourceManager,
            connectivityObserver,
            citySearchInteractor,
            recentCitiesInteractor
        )
    }

}