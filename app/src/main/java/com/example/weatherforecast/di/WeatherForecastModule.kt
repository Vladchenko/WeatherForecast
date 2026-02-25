package com.example.weatherforecast.di

import android.content.Context
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.api.WeatherApiService
import com.example.weatherforecast.data.database.CurrentWeatherDAO
import com.example.weatherforecast.data.database.HourlyWeatherDAO
import com.example.weatherforecast.data.mapper.CurrentWeatherDtoMapper
import com.example.weatherforecast.data.mapper.CurrentWeatherEntityMapper
import com.example.weatherforecast.data.mapper.HourlyWeatherDtoMapper
import com.example.weatherforecast.data.mapper.HourlyWeatherEntityMapper
import com.example.weatherforecast.data.preferences.PreferencesManager
import com.example.weatherforecast.data.repository.CurrentWeatherRepositoryImpl
import com.example.weatherforecast.data.repository.HourlyWeatherRepositoryImpl
import com.example.weatherforecast.data.repository.datasource.CurrentWeatherLocalDataSource
import com.example.weatherforecast.data.repository.datasource.CurrentWeatherRemoteDataSource
import com.example.weatherforecast.data.repository.datasource.HourlyWeatherLocalDataSource
import com.example.weatherforecast.data.repository.datasource.HourlyWeatherRemoteDataSource
import com.example.weatherforecast.data.repository.datasourceimpl.CurrentWeatherLocalDataSourceImpl
import com.example.weatherforecast.data.repository.datasourceimpl.CurrentWeatherRemoteDataSourceImpl
import com.example.weatherforecast.data.repository.datasourceimpl.HourlyWeatherLocalDataSourceImpl
import com.example.weatherforecast.data.repository.datasourceimpl.HourlyWeatherRemoteDataSourceImpl
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.data.util.ResponseProcessor
import com.example.weatherforecast.data.util.permission.PermissionChecker
import com.example.weatherforecast.data.util.permission.PermissionCheckerImpl
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.CurrentWeatherInteractor
import com.example.weatherforecast.domain.forecast.CurrentWeatherRepository
import com.example.weatherforecast.domain.forecast.HourlyWeatherInteractor
import com.example.weatherforecast.domain.forecast.HourlyWeatherRepository
import com.example.weatherforecast.geolocation.DeviceLocationProvider
import com.example.weatherforecast.geolocation.Geolocator
import com.example.weatherforecast.presentation.converter.WeatherDomainToUiConverter
import com.example.weatherforecast.presentation.converter.WeatherDomainToUiConverterImpl
import com.example.weatherforecast.presentation.converter.appbar.AppBarStateConverter
import com.example.weatherforecast.presentation.viewmodel.appBar.AppBarViewModelFactory
import com.example.weatherforecast.presentation.viewmodel.forecast.CurrentWeatherViewModelFactory
import com.example.weatherforecast.presentation.viewmodel.forecast.HourlyWeatherViewModelFactory
import com.example.weatherforecast.presentation.viewmodel.geolocation.GeoLocationViewModelFactory
import com.example.weatherforecast.utils.ResourceManager
import com.example.weatherforecast.utils.ResourceManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    fun providePermissionChecker(@ApplicationContext context: Context): PermissionChecker {
        return PermissionCheckerImpl(context)
    }

    @Singleton
    @Provides
    fun provideResourceManager(@ApplicationContext context: Context): ResourceManager {
        return ResourceManagerImpl(context)
    }

    @Singleton
    @Provides
    fun provideLoggingService(): LoggingService {
        return LoggingService()
    }

    @Singleton
    @Provides
    fun provideResponseProcessor(): ResponseProcessor {
        return ResponseProcessor()
    }

    @Singleton
    @Provides
    fun provideWeatherForecastLocalDataSource(forecastDAO: CurrentWeatherDAO): CurrentWeatherLocalDataSource {
        return CurrentWeatherLocalDataSourceImpl(forecastDAO)
    }

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
    fun provideHourlyForecastLocalDataSource(forecastDAO: HourlyWeatherDAO): HourlyWeatherLocalDataSource {
        return HourlyWeatherLocalDataSourceImpl(forecastDAO)
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
        dtoMapper: CurrentWeatherDtoMapper,
        entityMapper: CurrentWeatherEntityMapper,
        coroutineDispatchers: CoroutineDispatchers,
        currentWeatherLocalDataSource: CurrentWeatherLocalDataSource,
        currentWeatherRemoteDataSource: CurrentWeatherRemoteDataSource,
    ): CurrentWeatherRepository {
        return CurrentWeatherRepositoryImpl(
            dtoMapper,
            entityMapper,
            coroutineDispatchers,
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
        dtoMapper: HourlyWeatherDtoMapper,
        entityMapper: HourlyWeatherEntityMapper,
        coroutineDispatchers: CoroutineDispatchers,
        hourlyWeatherLocalDataSource: HourlyWeatherLocalDataSource,
        hourlyWeatherRemoteDataSource: HourlyWeatherRemoteDataSource
    ): HourlyWeatherRepository {
        return HourlyWeatherRepositoryImpl(
            coroutineDispatchers,
            dtoMapper,
            entityMapper,
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
        resourceManager: ResourceManager,
        preferencesManager: PreferencesManager,
        connectivityObserver: ConnectivityObserver,
        chosenCityInteractor: ChosenCityInteractor,
        coroutineDispatchers: CoroutineDispatchers,
        forecastRemoteInteractor: CurrentWeatherInteractor,
        uiConverter: WeatherDomainToUiConverter
    ): CurrentWeatherViewModelFactory {
        return CurrentWeatherViewModelFactory(
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
        resourceManager: ResourceManager,
        preferencesManager: PreferencesManager,
        connectivityObserver: ConnectivityObserver,
        chosenCityInteractor: ChosenCityInteractor,
        coroutineDispatchers: CoroutineDispatchers,
        forecastRemoteInteractor: HourlyWeatherInteractor
    ): HourlyWeatherViewModelFactory {
        return HourlyWeatherViewModelFactory(
            resourceManager,
            preferencesManager,
            connectivityObserver,
            chosenCityInteractor,
            coroutineDispatchers,
            forecastRemoteInteractor
        )
    }

    @Singleton
    @Provides
    fun provideGeoLocationViewModelFactory(
        permissionChecker: PermissionChecker,
        geoLocationHelper: Geolocator,
        geoLocator: DeviceLocationProvider,
        connectivityObserver: ConnectivityObserver,
        chosenCityInteractor: ChosenCityInteractor,
        coroutineDispatchers: CoroutineDispatchers,
    ): GeoLocationViewModelFactory {
        return GeoLocationViewModelFactory(
            permissionChecker,
            geoLocationHelper,
            geoLocator,
            connectivityObserver,
            chosenCityInteractor,
            coroutineDispatchers
        )
    }

    @Singleton
    @Provides
    fun provideAppBarStateConverter(resourceManager: ResourceManager,): AppBarStateConverter {
        return AppBarStateConverter(resourceManager)
    }

    @Singleton
    @Provides
    fun provideAppBarViewModelFactory(
        resourceManager: ResourceManager,
        appBarStateConverter: AppBarStateConverter
    ): AppBarViewModelFactory {
        return AppBarViewModelFactory(
            resourceManager,
            appBarStateConverter
        )
    }
}