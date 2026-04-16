package io.github.vladchenko.weatherforecast.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.data.mapper.DataErrorToForecastErrorMapper
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
import io.github.vladchenko.weatherforecast.data.api.WeatherApiService
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
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.presentation.viewmodel.HourlyWeatherViewModelFactory
import io.github.vladchenko.weatherforecast.presentation.viewmodel.geolocation.GeoLocationViewModelFactory
import kotlinx.serialization.InternalSerializationApi
import javax.inject.Singleton

/**
 * Dagger Hilt module that provides core data mapping, repository, and presentation-layer dependencies
 * for the weather forecast feature.
 *
 * This module is installed in the [SingletonComponent], ensuring that all provided instances
 * are scoped to the application lifecycle and shared across components.
 *
 * It supplies:
 * - **Mappers** (not TypeConverters):
 *   - [CurrentWeatherDtoMapper] and [HourlyWeatherDtoMapper] – convert API responses (DTOs) to domain models
 *   - [CurrentWeatherEntityMapper] and [HourlyWeatherEntityMapper] – map between database entities and domain models
 *   These mappers enable clean separation between layers instead of using Room TypeConverters for complex objects.
 *
 * - **Data sources**:
 *   - Local ([CurrentWeatherLocalDataSourceImpl], [HourlyWeatherLocalDataSourceImpl]) using Room DAOs
 *   - Remote ([CurrentWeatherRemoteDataSourceImpl], [HourlyWeatherRemoteDataSourceImpl]) using [WeatherApiService]
 *   Both include logging and response processing via [LoggingService] and [ResponseProcessor]
 *
 * - **Repositories**:
 *   - [CurrentWeatherRepositoryImpl] and [HourlyWeatherRepositoryImpl] orchestrate data flow between local/remote sources,
 *     using mappers and [CoroutineDispatchers] for background execution
 *
 * - **Interactors (use cases)**:
 *   - [CurrentWeatherInteractor] and [HourlyWeatherInteractor] serve as entry points for business logic
 *
 * - **ViewModel factories**:
 *   - [CurrentWeatherViewModelFactory], [HourlyWeatherViewModelFactory], [GeoLocationViewModelFactory], [AppBarViewModelFactory]
 *     inject required dependencies including interactors, converters, and utilities
 *
 * - **Supporting utilities**:
 *   - [PermissionCheckerImpl] – handles runtime location permissions
 *   - [ResourceManagerImpl] – provides string and UI resources
 *   - [WeatherDomainToUiConverterImpl] – transforms domain models into UI state
 *   - [AppBarStateConverter] – manages app bar title/subtitle based on location and connectivity
 *
 * This setup ensures a clean architecture with dependency inversion, testability, and full control over data transformation
 * through explicit mappers rather than implicit Room TypeConverters.
 *
 * @see CurrentWeatherRepository
 * @see CurrentWeatherViewModelFactory
 * @see GeoLocationViewModelFactory
 * @see WeatherDomainToUiConverter
 */
@Module
@InstallIn(SingletonComponent::class)
class WeatherForecastModule {

    @Singleton
    @Provides
    fun provideResponseProcessor(): ResponseProcessor {
        return ResponseProcessor()
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
        weatherApiService: WeatherApiService,
        loggingService: LoggingService,
        responseProcessor: ResponseProcessor
    ): CurrentWeatherRemoteDataSource {
        return CurrentWeatherRemoteDataSourceImpl(
            weatherApiService, loggingService, responseProcessor
        )
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
        hourlyForecastApiService: WeatherApiService,
        loggingService: LoggingService,
        responseProcessor: ResponseProcessor
    ): HourlyWeatherRemoteDataSource {
        return HourlyWeatherRemoteDataSourceImpl(
            hourlyForecastApiService, loggingService, responseProcessor
        )
    }

    @Singleton
    @Provides
    fun provideWeatherForecastUiConverter(): WeatherDomainToUiConverter {
        return WeatherDomainToUiConverterImpl()
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
    fun provideWeatherForecastRemoteInteractor(currentWeatherRepository: CurrentWeatherRepository): CurrentWeatherInteractor {
        return CurrentWeatherInteractor(currentWeatherRepository)
    }

    @Singleton
    @Provides
    fun provideHourlyForecastRemoteInteractor(hourlyWeatherRepository: HourlyWeatherRepository): HourlyWeatherInteractor {
        return HourlyWeatherInteractor(hourlyWeatherRepository)
    }

    @Singleton
    @Provides
    fun provideForecastViewModelFactory(
        loggingService: LoggingService,
        statusRenderer: StatusRenderer,
        resourceManager: ResourceManager,
        preferencesManager: PreferencesManager,
        connectivityObserver: ConnectivityObserver,
        chosenCityInteractor: ChosenCityInteractor,
        coroutineDispatchers: CoroutineDispatchers,
        forecastRemoteInteractor: CurrentWeatherInteractor,
        uiConverter: WeatherDomainToUiConverter
    ): CurrentWeatherViewModelFactory {
        return CurrentWeatherViewModelFactory(
            loggingService,
            statusRenderer,
            resourceManager,
            preferencesManager,
            connectivityObserver,
            chosenCityInteractor,
            coroutineDispatchers,
            forecastRemoteInteractor,
            uiConverter
        )
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

    @Singleton
    @Provides
    fun provideGeoLocationViewModelFactory(
        geoLocationHelper: Geolocator,
        loggingService: LoggingService,
        statusRenderer: StatusRenderer,
        resourceManager: ResourceManager,
        geoLocator: DeviceLocationProvider,
        permissionChecker: PermissionChecker,
        connectivityObserver: ConnectivityObserver,
        chosenCityInteractor: ChosenCityInteractor,
        coroutineDispatchers: CoroutineDispatchers,
    ): GeoLocationViewModelFactory {
        return GeoLocationViewModelFactory(
            geoLocationHelper,
            loggingService,
            statusRenderer,
            resourceManager,
            geoLocator,
            permissionChecker,
            connectivityObserver,
            chosenCityInteractor,
            coroutineDispatchers
        )
    }

    @Singleton
    @Provides
    fun provideAppBarStateConverter(resourceManager: ResourceManager): AppBarStateConverter {
        return AppBarStateConverter(resourceManager)
    }

    @Singleton
    @Provides
    fun provideAppBarViewModelFactory(
        statusRenderer: StatusRenderer,
        resourceManager: ResourceManager,
        appBarStateConverter: AppBarStateConverter
    ): AppBarViewModelFactory {
        return AppBarViewModelFactory(
            statusRenderer,
            resourceManager,
            appBarStateConverter
        )
    }
}