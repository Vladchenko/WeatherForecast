package io.github.vladchenko.weatherforecast.feature.hourlyforecast.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.data.mapper.DataErrorToForecastErrorMapper
import io.github.vladchenko.weatherforecast.core.di.DiConstants.WEATHER_RETROFIT_NAME
import io.github.vladchenko.weatherforecast.core.location.geolocation.DeviceLocationProvider
import io.github.vladchenko.weatherforecast.core.location.geolocation.geolocator.Geolocator
import io.github.vladchenko.weatherforecast.core.location.permission.PermissionChecker
import io.github.vladchenko.weatherforecast.core.location.permission.PermissionCheckerImpl
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManagerImpl
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.data.database.WeatherForecastDatabase
import io.github.vladchenko.weatherforecast.feature.currentweather.data.api.CurrentWeatherApiService
import io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.datasource.CurrentWeatherDAO
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.repository.datasource.HourlyWeatherDAO
import io.github.vladchenko.weatherforecast.data.util.ResponseProcessor
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityInteractor
import io.github.vladchenko.weatherforecast.feature.currentweather.data.mapper.CurrentWeatherDtoMapper
import io.github.vladchenko.weatherforecast.feature.currentweather.data.mapper.CurrentWeatherEntityMapper
import io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.CurrentWeatherRepositoryImpl
import io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.datasource.CurrentWeatherLocalDataSource
import io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.datasource.CurrentWeatherRemoteDataSource
import io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.datasourceimpl.CurrentWeatherLocalDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.datasourceimpl.CurrentWeatherRemoteDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.currentweather.domain.CurrentWeatherInteractor
import io.github.vladchenko.weatherforecast.feature.currentweather.domain.CurrentWeatherRepository
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.mapper.HourlyWeatherDtoMapper
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.mapper.HourlyWeatherEntityMapper
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.repository.HourlyWeatherRepositoryImpl
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.repository.datasource.HourlyWeatherLocalDataSource
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.repository.datasource.HourlyWeatherRemoteDataSource
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.repository.datasourceimpl.HourlyWeatherLocalDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.repository.datasourceimpl.HourlyWeatherRemoteDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.domain.HourlyWeatherInteractor
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.domain.HourlyWeatherRepository
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.converter.WeatherDomainToUiConverter
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.converter.WeatherDomainToUiConverterImpl
import io.github.vladchenko.weatherforecast.presentation.converter.appbar.AppBarStateConverter
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModelFactory
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CurrentWeatherViewModelFactory
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.api.HourlyForecastApiService
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.presentation.viewmodel.HourlyWeatherViewModelFactory
import io.github.vladchenko.weatherforecast.presentation.viewmodel.geolocation.GeoLocationViewModelFactory
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Dagger module for providing dependencies related to the Hourly Forecast feature.
 *
 * This module defines how components such as data sources, mappers, repository,
 * interactor, and view model factory are created and injected within the application.
 * All bindings are scoped to the SingletonComponent to ensure single instances
 * across the app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class HourlyForecastModule {

    @Provides
    @Singleton
    @InternalSerializationApi
    fun provideHourlyForecastDAO(database: WeatherForecastDatabase): HourlyWeatherDAO {
        return database.getHourlyForecastInstance()
    }

    @Singleton
    @Provides
    fun provideHourlyForecastApiService(@Named(WEATHER_RETROFIT_NAME) retrofit: Retrofit): HourlyForecastApiService {
        return retrofit.create(HourlyForecastApiService::class.java)
    }

    @Singleton
    @Provides
    @InternalSerializationApi
    fun provideHourlyForecastLocalDataSource(loggingService: LoggingService, forecastDAO: HourlyWeatherDAO): HourlyWeatherLocalDataSource {
        return HourlyWeatherLocalDataSourceImpl(forecastDAO, loggingService)
    }

    @Singleton
    @Provides
    fun provideHourlyForecastRemoteDataSource(
        hourlyForecastApiService: HourlyForecastApiService,
        loggingService: LoggingService,
        responseProcessor: ResponseProcessor
    ): HourlyWeatherRemoteDataSource {
        return HourlyWeatherRemoteDataSourceImpl(
            hourlyForecastApiService, loggingService, responseProcessor
        )
    }

    @Singleton
    @Provides
    @InternalSerializationApi
    fun provideHourlyWeatherDtoMapper(): HourlyWeatherDtoMapper {
        return HourlyWeatherDtoMapper()
    }

    @Singleton
    @Provides
    @InternalSerializationApi
    fun provideHourlyWeatherEntityMapper(): HourlyWeatherEntityMapper {
        return HourlyWeatherEntityMapper()
    }

    @Singleton
    @Provides
    @InternalSerializationApi
    fun provideHourlyForecastRepository(
        loggingService: LoggingService,
        dtoMapper: HourlyWeatherDtoMapper,
        entityMapper: HourlyWeatherEntityMapper,
        coroutineDispatchers: CoroutineDispatchers,
        errorMapper: DataErrorToForecastErrorMapper,
        hourlyWeatherLocalDataSource: HourlyWeatherLocalDataSource,
        hourlyWeatherRemoteDataSource: HourlyWeatherRemoteDataSource
    ): HourlyWeatherRepository {
        return HourlyWeatherRepositoryImpl(
            loggingService,
            coroutineDispatchers,
            dtoMapper,
            entityMapper,
            errorMapper,
            hourlyWeatherLocalDataSource,
            hourlyWeatherRemoteDataSource,
        )
    }

    @Singleton
    @Provides
    fun provideHourlyForecastRemoteInteractor(hourlyWeatherRepository: HourlyWeatherRepository): HourlyWeatherInteractor {
        return HourlyWeatherInteractor(hourlyWeatherRepository)
    }

    @Singleton
    @Provides
    fun provideHourlyForecastViewModelFactory(
        loggingService: LoggingService,
        statusRenderer: StatusRenderer,
        resourceManager: ResourceManager,
        preferencesManager: PreferencesManager,
        connectivityObserver: ConnectivityObserver,
        chosenCityInteractor: ChosenCityInteractor,
        forecastRemoteInteractor: HourlyWeatherInteractor
    ): HourlyWeatherViewModelFactory {
        return HourlyWeatherViewModelFactory(
            loggingService,
            statusRenderer,
            resourceManager,
            preferencesManager,
            connectivityObserver,
            chosenCityInteractor,
            forecastRemoteInteractor
        )
    }
}