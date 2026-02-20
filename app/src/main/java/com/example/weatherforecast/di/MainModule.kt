package com.example.weatherforecast.di

import android.content.Context
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.api.WeatherApiService
import com.example.weatherforecast.data.converter.CurrentWeatherModelConverter
import com.example.weatherforecast.data.converter.HourlyWeatherModelConverter
import com.example.weatherforecast.data.database.CurrentWeatherDAO
import com.example.weatherforecast.data.database.HourlyWeatherDAO
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
import javax.inject.Singleton

/**
 * Dagger Hilt module that provides core business logic and presentation-layer dependencies
 * for the weather forecast feature.
 *
 * This module is installed in the [SingletonComponent], ensuring that all provided instances
 * are scoped to the application lifecycle and shared across components.
 *
 * It supplies:
 * - Data converters: [CurrentWeatherModelConverter], [HourlyWeatherModelConverter],
 *   and [WeatherDomainToUiConverter] for transforming API responses to domain and UI models
 * - Local and remote data sources for current and hourly forecasts, using DAOs and API services
 * - Repositories ([CurrentWeatherRepositoryImpl], [HourlyWeatherRepositoryImpl]) that encapsulate
 *   data access logic with proper threading via [CoroutineDispatchers]
 * - Interactors (use cases) for local and remote forecast operations
 * - ViewModel factories for:
 *   - [CurrentWeatherViewModelFactory] – main forecast screen
 *   - [HourlyWeatherViewModelFactory] – hourly forecast panel
 *   - [GeoLocationViewModelFactory] – location permission and retrieval logic
 *   - [AppBarViewModelFactory] – app bar title/subtitle management
 * - Supporting utilities: [PermissionChecker], [ResourceManager], [LoggingService], [ResponseProcessor]
 *
 * Enables clean separation of concerns, testability, and dependency injection throughout the app.
 *
 * @see CurrentWeatherRepository
 * @see CurrentWeatherViewModelFactory
 * @see GeoLocationViewModelFactory
 * @see AppBarStateConverter
 */
@Module
@InstallIn(SingletonComponent::class)
class MainModule {

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
    fun provideWeatherForecastDomainConverter(): CurrentWeatherModelConverter {
        return CurrentWeatherModelConverter()
    }

    @Singleton
    @Provides
    fun provideWeatherForecastUiConverter(): WeatherDomainToUiConverter {
        return WeatherDomainToUiConverterImpl()
    }

    @Singleton
    @Provides
    fun provideHourlyForecastConverter(): HourlyWeatherModelConverter {
        return HourlyWeatherModelConverter()
    }

    @Singleton
    @Provides
    fun provideWeatherForecastRepository(
        currentWeatherRemoteDataSource: CurrentWeatherRemoteDataSource,
        currentWeatherLocalDataSource: CurrentWeatherLocalDataSource,
        converter: CurrentWeatherModelConverter,
        coroutineDispatchers: CoroutineDispatchers,
    ): CurrentWeatherRepository {
        return CurrentWeatherRepositoryImpl(
            currentWeatherRemoteDataSource,
            currentWeatherLocalDataSource,
            converter,
            coroutineDispatchers
        )
    }

    @Singleton
    @Provides
    fun provideHourlyForecastRepository(
        hourlyWeatherRemoteDataSource: HourlyWeatherRemoteDataSource,
        hourlyWeatherLocalDataSource: HourlyWeatherLocalDataSource,
        converter: HourlyWeatherModelConverter,
        coroutineDispatchers: CoroutineDispatchers
    ): HourlyWeatherRepository {
        return HourlyWeatherRepositoryImpl(
            hourlyWeatherRemoteDataSource,
            hourlyWeatherLocalDataSource,
            converter,
            coroutineDispatchers,
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