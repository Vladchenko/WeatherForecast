package io.github.vladchenko.weatherforecast.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.data.api.CityApiService
import io.github.vladchenko.weatherforecast.data.database.CitiesNamesDAO
import io.github.vladchenko.weatherforecast.data.database.RecentCitiesDAO
import io.github.vladchenko.weatherforecast.data.mapper.CitiesSearchDtoMapper
import io.github.vladchenko.weatherforecast.data.mapper.CitiesSearchEntityMapper
import io.github.vladchenko.weatherforecast.data.mapper.RecentCitiesMapper
import io.github.vladchenko.weatherforecast.data.repository.ChosenCityRepositoryImpl
import io.github.vladchenko.weatherforecast.data.repository.CitiesNamesRepositoryImpl
import io.github.vladchenko.weatherforecast.data.repository.RecentCitiesRepositoryImpl
import io.github.vladchenko.weatherforecast.data.repository.datasource.ChosenCityDataSource
import io.github.vladchenko.weatherforecast.data.repository.datasource.CitiesNamesLocalDataSource
import io.github.vladchenko.weatherforecast.data.repository.datasource.CitiesNamesRemoteDataSource
import io.github.vladchenko.weatherforecast.data.repository.datasource.RecentCitiesDataSource
import io.github.vladchenko.weatherforecast.data.repository.datasourceimpl.ChosenCityLocalDataSourceImpl
import io.github.vladchenko.weatherforecast.data.repository.datasourceimpl.CitiesNamesLocalDataSourceImpl
import io.github.vladchenko.weatherforecast.data.repository.datasourceimpl.CitiesNamesRemoteDataSourceImpl
import io.github.vladchenko.weatherforecast.data.repository.datasourceimpl.RecentCitiesDataSourceImpl
import io.github.vladchenko.weatherforecast.data.util.LoggingService
import io.github.vladchenko.weatherforecast.data.util.ResponseProcessor
import io.github.vladchenko.weatherforecast.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.domain.citiesnames.CitiesNamesInteractor
import io.github.vladchenko.weatherforecast.domain.citiesnames.CitiesNamesRepository
import io.github.vladchenko.weatherforecast.domain.city.ChosenCityInteractor
import io.github.vladchenko.weatherforecast.domain.city.ChosenCityRepository
import io.github.vladchenko.weatherforecast.domain.recentcities.RecentCitiesInteractor
import io.github.vladchenko.weatherforecast.domain.recentcities.RecentCitiesRepository
import io.github.vladchenko.weatherforecast.presentation.PresentationUtils
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import io.github.vladchenko.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModelFactory
import io.github.vladchenko.weatherforecast.utils.ResourceManager
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
 * @see CitiesNamesRepository
 * @see CitiesNamesInteractor
 * @see CitiesNamesViewModelFactory
 */
@Module
@InstallIn(SingletonComponent::class)
class CityManagementModule {

    @Singleton
    @Provides
    @InternalSerializationApi
    fun provideCitiesSearchDtoMapper(): CitiesSearchDtoMapper {
        return CitiesSearchDtoMapper()
    }

    @Singleton
    @Provides
    @InternalSerializationApi
    fun provideCitiesSearchEntityMapper(): CitiesSearchEntityMapper {
        return CitiesSearchEntityMapper()
    }

    @Singleton
    @Provides
    fun provideCityDataSource(app: Application): ChosenCityDataSource {
        return ChosenCityLocalDataSourceImpl(
            app.getSharedPreferences(PresentationUtils.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
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
        dao: CitiesNamesDAO
    ): CitiesNamesLocalDataSource {
        return CitiesNamesLocalDataSourceImpl(dao, loggingService)
    }

    @Singleton
    @Provides
    fun provideCitiesNamesRemoteDataSource(
        cityApiService: CityApiService,
        responseProcessor: ResponseProcessor,
        loggingService: LoggingService
    ): CitiesNamesRemoteDataSource {
        return CitiesNamesRemoteDataSourceImpl(cityApiService, loggingService, responseProcessor)
    }

    @Singleton
    @Provides
    @InternalSerializationApi
    fun provideCitiesNamesRepository(
        loggingService: LoggingService,
        dtoMapper: CitiesSearchDtoMapper,
        entityMapper: CitiesSearchEntityMapper,
        coroutineDispatchers: CoroutineDispatchers,
        citiesNamesLocalDataSource: CitiesNamesLocalDataSource,
        citiesNamesRemoteDataSource: CitiesNamesRemoteDataSource
    ): CitiesNamesRepository {
        return CitiesNamesRepositoryImpl(
            loggingService,
            dtoMapper,
            entityMapper,
            coroutineDispatchers,
            citiesNamesLocalDataSource,
            citiesNamesRemoteDataSource,
        )
    }

    @Singleton
    @Provides
    fun provideCitiesNamesInteractor(citiesNamesRepository: CitiesNamesRepository): CitiesNamesInteractor {
        return CitiesNamesInteractor(citiesNamesRepository)
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
        citiesNamesInteractor: CitiesNamesInteractor,
        recentCitiesInteractor: RecentCitiesInteractor
    ): CitiesNamesViewModelFactory {
        return CitiesNamesViewModelFactory(
            loggingService,
            statusRenderer,
            resourceManager,
            connectivityObserver,
            citiesNamesInteractor,
            recentCitiesInteractor
        )
    }

}