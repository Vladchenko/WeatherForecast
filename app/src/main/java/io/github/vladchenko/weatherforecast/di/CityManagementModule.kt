package io.github.vladchenko.weatherforecast.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesConstants.SHARED_PREFERENCES_KEY
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.data.api.CityApiService
import io.github.vladchenko.weatherforecast.data.database.CitySearchDAO
import io.github.vladchenko.weatherforecast.data.database.RecentCitiesDAO
import io.github.vladchenko.weatherforecast.data.util.ResponseProcessor
import io.github.vladchenko.weatherforecast.feature.chosencity.data.repository.ChosenCityRepositoryImpl
import io.github.vladchenko.weatherforecast.feature.chosencity.data.repository.datasource.ChosenCityDataSource
import io.github.vladchenko.weatherforecast.feature.chosencity.data.repository.datasourceimpl.ChosenCityLocalDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityInteractor
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityRepository
import io.github.vladchenko.weatherforecast.feature.citysearch.data.mapper.CitySearchDtoMapper
import io.github.vladchenko.weatherforecast.feature.citysearch.data.mapper.CitySearchEntityMapper
import io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.CitySearchRepositoryImpl
import io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasource.CitySearchLocalDataSource
import io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasource.CitySearchRemoteDataSource
import io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasourceimpl.CitySearchLocalDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasourceimpl.CitySearchRemoteDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.CitySearchInteractor
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.CitySearchRepository
import io.github.vladchenko.weatherforecast.feature.recentcities.data.mapper.RecentCitiesMapper
import io.github.vladchenko.weatherforecast.feature.recentcities.data.repository.RecentCitiesRepositoryImpl
import io.github.vladchenko.weatherforecast.feature.recentcities.data.repository.datasource.RecentCitiesDataSource
import io.github.vladchenko.weatherforecast.feature.recentcities.data.repository.datasourceimpl.RecentCitiesDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.RecentCitiesInteractor
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.RecentCitiesRepository
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import io.github.vladchenko.weatherforecast.presentation.viewmodel.cityselection.CitySearchViewModelFactory
import kotlinx.serialization.InternalSerializationApi
import javax.inject.Singleton

/**
 * Dagger Hilt module that provides dependency injection bindings for city-related components.
 *
 * This module is installed in the [SingletonComponent], ensuring that all provided dependencies
 * have a singleton lifecycle across the application.
 *
 * It includes providers for:
 * - Data sources (local and remote) for city names and chosen city
 * - Repositories encapsulating data access logic
 * - Interactors (use cases) containing business logic
 * - ViewModel factory for [CitiesNamesViewModel]
 *
 * Dependencies are constructed with proper layering: data sources → repositories → interactors → ViewModels.
 *
 * @see ChosenCityDataSource
 * @see CitySearchRepository
 * @see CitySearchInteractor
 * @see CitySearchViewModelFactory
 */
@Module
@InstallIn(SingletonComponent::class)
class CityManagementModule {

    @Singleton
    @Provides
    @InternalSerializationApi
    fun provideCitiesSearchDtoMapper(): CitySearchDtoMapper {
        return CitySearchDtoMapper()
    }

    @Singleton
    @Provides
    @InternalSerializationApi
    fun provideCitiesSearchEntityMapper(): CitySearchEntityMapper {
        return CitySearchEntityMapper()
    }

    @Singleton
    @Provides
    fun provideCityDataSource(app: Application): ChosenCityDataSource {
        return ChosenCityLocalDataSourceImpl(
            app.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
        )
    }

    @Singleton
    @Provides
    fun provideCityRepository(
        chosenCityDataSource: ChosenCityDataSource,
        coroutineDispatchers: CoroutineDispatchers
    ): ChosenCityRepository {
        return ChosenCityRepositoryImpl(coroutineDispatchers, chosenCityDataSource)
    }

    @Singleton
    @Provides
    fun provideCityInteractor(chosenCityRepository: ChosenCityRepository): ChosenCityInteractor {
        return ChosenCityInteractor(chosenCityRepository)
    }

    @InternalSerializationApi
    @Singleton
    @Provides
    fun provideCitiesNamesLocalDataSource(
        loggingService: LoggingService,
        dao: CitySearchDAO
    ): CitySearchLocalDataSource {
        return CitySearchLocalDataSourceImpl(dao, loggingService)
    }

    @Singleton
    @Provides
    fun provideCitiesNamesRemoteDataSource(
        cityApiService: CityApiService,
        responseProcessor: ResponseProcessor,
        loggingService: LoggingService
    ): CitySearchRemoteDataSource {
        return CitySearchRemoteDataSourceImpl(cityApiService, loggingService, responseProcessor)
    }

    @Singleton
    @Provides
    @InternalSerializationApi
    fun provideCitiesNamesRepository(
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
    @InternalSerializationApi
    fun provideRecentCitiesMapper(): RecentCitiesMapper {
        return RecentCitiesMapper()
    }

    @Singleton
    @Provides
    fun provideRecentCitiesDataSource(
        dao: RecentCitiesDAO,
        loggingService: LoggingService
    ): RecentCitiesDataSource {
        return RecentCitiesDataSourceImpl(
            dao,
            loggingService
        )
    }

    @Singleton
    @Provides
    fun provideRecentCitiesRepository(
        recentCitiesMapper: RecentCitiesMapper,
        coroutineDispatchers: CoroutineDispatchers,
        recentCitiesDataSource: RecentCitiesDataSource
    ): RecentCitiesRepository {
        return RecentCitiesRepositoryImpl(
            recentCitiesMapper,
            coroutineDispatchers,
            recentCitiesDataSource
        )
    }

    @Singleton
    @Provides
    fun provideRecentCitiesInteractor(repository: RecentCitiesRepository): RecentCitiesInteractor {
        return RecentCitiesInteractor(repository)
    }

    @Singleton
    @Provides
    fun provideCitiesNamesViewModelFactory(
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