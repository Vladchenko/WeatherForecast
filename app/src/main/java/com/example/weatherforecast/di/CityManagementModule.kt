package com.example.weatherforecast.di

import android.app.Application
import android.content.Context
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.api.CityApiService
import com.example.weatherforecast.data.database.CitiesNamesDAO
import com.example.weatherforecast.data.mapper.CitiesSearchDtoMapper
import com.example.weatherforecast.data.mapper.CitiesSearchEntityMapper
import com.example.weatherforecast.data.repository.ChosenCityRepositoryImpl
import com.example.weatherforecast.data.repository.CitiesNamesRepositoryImpl
import com.example.weatherforecast.data.repository.datasource.ChosenCityDataSource
import com.example.weatherforecast.data.repository.datasource.CitiesNamesLocalDataSource
import com.example.weatherforecast.data.repository.datasource.CitiesNamesRemoteDataSource
import com.example.weatherforecast.data.repository.datasourceimpl.ChosenCityLocalDataSourceImpl
import com.example.weatherforecast.data.repository.datasourceimpl.CitiesNamesLocalDataSourceImpl
import com.example.weatherforecast.data.repository.datasourceimpl.CitiesNamesRemoteDataSourceImpl
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor
import com.example.weatherforecast.domain.citiesnames.CitiesNamesRepository
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.city.ChosenCityRepository
import com.example.weatherforecast.presentation.PresentationUtils
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModelFactory
import com.example.weatherforecast.utils.ResourceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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

    @Singleton
    @Provides
    fun provideCitiesNamesLocalDataSource(loggingService: LoggingService,
                                          dao: CitiesNamesDAO): CitiesNamesLocalDataSource {
        return CitiesNamesLocalDataSourceImpl(dao, loggingService)
    }

    @Singleton
    @Provides
    fun provideCitiesNamesRemoteDataSource(cityApiService: CityApiService,
                                           loggingService: LoggingService): CitiesNamesRemoteDataSource {
        return CitiesNamesRemoteDataSourceImpl(cityApiService, loggingService)
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
    fun provideCitiesNamesViewModelFactory(
        loggingService: LoggingService,
        statusRenderer: StatusRenderer,
        resourceManager: ResourceManager,
        connectivityObserver: ConnectivityObserver,
        citiesNamesInteractor: CitiesNamesInteractor
    ): CitiesNamesViewModelFactory {
        return CitiesNamesViewModelFactory(
            loggingService,
            statusRenderer,
            resourceManager,
            connectivityObserver,
            citiesNamesInteractor
        )
    }
}