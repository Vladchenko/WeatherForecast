package io.github.vladchenko.weatherforecast.feature.currentweather.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.data.mapper.DataErrorToForecastErrorMapper
import io.github.vladchenko.weatherforecast.core.di.DiConstants.WEATHER_RETROFIT_NAME
import io.github.vladchenko.weatherforecast.core.network.NetworkStateHolder
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.data.database.WeatherForecastDatabase
import io.github.vladchenko.weatherforecast.data.util.ResponseProcessor
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityInteractor
import io.github.vladchenko.weatherforecast.feature.currentweather.data.api.CurrentWeatherApiService
import io.github.vladchenko.weatherforecast.feature.currentweather.data.mapper.CurrentWeatherDtoMapper
import io.github.vladchenko.weatherforecast.feature.currentweather.data.mapper.CurrentWeatherEntityMapper
import io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.CurrentWeatherRepositoryImpl
import io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.datasource.CurrentWeatherDAO
import io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.datasource.CurrentWeatherLocalDataSource
import io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.datasource.CurrentWeatherRemoteDataSource
import io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.datasourceimpl.CurrentWeatherLocalDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.datasourceimpl.CurrentWeatherRemoteDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.CurrentWeatherInteractor
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.CurrentWeatherRepository
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.domaintouimapper.WeatherDomainToUiMapper
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.domaintouimapper.WeatherDomainToUiMapperImpl
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.outputmapper.WeatherOutputMapper
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.outputmapper.WeatherOutputMapperImpl
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CurrentWeatherViewModelFactory
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.WeatherResponseHandler
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Dagger module for providing dependencies related to the Current Weather feature.
 *
 * This module defines how components such as data sources, mappers, repository,
 * interactor, and view model factory are created and injected within the application.
 * All bindings are scoped to the SingletonComponent to ensure single instances
 * across the app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class CurrentWeatherModule {

    @Provides
    @Singleton
    @InternalSerializationApi
    fun provideWeatherForecastDAO(database: WeatherForecastDatabase): CurrentWeatherDAO {
        return database.getWeatherForecastInstance()
    }

    @Singleton
    @Provides
    fun provideWeatherForecastApiService(@Named(WEATHER_RETROFIT_NAME) retrofit: Retrofit): CurrentWeatherApiService {
        return retrofit.create(CurrentWeatherApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideWeatherForecastLocalDataSource(
        loggingService: LoggingService,
        forecastDAO: CurrentWeatherDAO
    ): CurrentWeatherLocalDataSource {
        return CurrentWeatherLocalDataSourceImpl(forecastDAO, loggingService)
    }

    @InternalSerializationApi
    @Singleton
    @Provides
    fun provideWeatherForecastRemoteDataSource(
        currentWeatherApiService: CurrentWeatherApiService,
        loggingService: LoggingService,
        responseProcessor: ResponseProcessor
    ): CurrentWeatherRemoteDataSource {
        return CurrentWeatherRemoteDataSourceImpl(
            currentWeatherApiService, loggingService, responseProcessor
        )
    }

    @Singleton
    @Provides
    fun provideWeatherForecastUiConverter(): WeatherDomainToUiMapper {
        return WeatherDomainToUiMapperImpl()
    }

    @Singleton
    @Provides
    @InternalSerializationApi
    fun provideCurrentWeatherDtoMapper(): CurrentWeatherDtoMapper {
        return CurrentWeatherDtoMapper()
    }

    @Singleton
    @Provides
    @InternalSerializationApi
    fun provideCurrentWeatherEntityMapper(): CurrentWeatherEntityMapper {
        return CurrentWeatherEntityMapper()
    }

    @Singleton
    @Provides
    @InternalSerializationApi
    fun provideWeatherForecastRepository(
        loggingService: LoggingService,
        dtoMapper: CurrentWeatherDtoMapper,
        entityMapper: CurrentWeatherEntityMapper,
        coroutineDispatchers: CoroutineDispatchers,
        errorMapper: DataErrorToForecastErrorMapper,
        currentWeatherLocalDataSource: CurrentWeatherLocalDataSource,
        currentWeatherRemoteDataSource: CurrentWeatherRemoteDataSource,
    ): CurrentWeatherRepository {
        return CurrentWeatherRepositoryImpl(
            loggingService,
            dtoMapper,
            entityMapper,
            coroutineDispatchers,
            errorMapper,
            currentWeatherLocalDataSource,
            currentWeatherRemoteDataSource
        )
    }

    @Singleton
    @Provides
    fun provideWeatherForecastRemoteInteractor(currentWeatherRepository: CurrentWeatherRepository): CurrentWeatherInteractor {
        return CurrentWeatherInteractor(currentWeatherRepository)
    }

    @Singleton
    @Provides
    fun provideForecastViewModelFactory(
        loggingService: LoggingService,
        stateHolder: StatusStateHolder,
        resourceManager: ResourceManager,
        networkStateHolder: NetworkStateHolder,
        preferencesManager: PreferencesManager,
        weatherOutputMapper: WeatherOutputMapper,
        chosenCityInteractor: ChosenCityInteractor,
        forecastInteractor: CurrentWeatherInteractor,
    ): CurrentWeatherViewModelFactory {
        return CurrentWeatherViewModelFactory(
            loggingService,
            resourceManager,
            stateHolder,
            networkStateHolder,
            preferencesManager,
            weatherOutputMapper,
            chosenCityInteractor,
            forecastInteractor,
        )
    }

    @Singleton
    @Provides
    fun provideWeatherResponseHandler(
        loggingService: LoggingService,
        statusStateHolder: StatusStateHolder,
        resourceManager: ResourceManager,
    ): WeatherResponseHandler {
        return WeatherResponseHandler(
            loggingService,
            resourceManager,
            statusStateHolder,
        )
    }

    @Singleton
    @Provides
    fun provideWeatherOutputMapper(
        resourceManager: ResourceManager,
        weatherResponseHandler: WeatherResponseHandler,
        weatherDomainToUiMapper: WeatherDomainToUiMapper,
    ): WeatherOutputMapper {
        return WeatherOutputMapperImpl(
            resourceManager,
            weatherResponseHandler,
            weatherDomainToUiMapper
        )
    }
}