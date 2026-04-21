package io.github.vladchenko.weatherforecast.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.data.util.ResponseProcessor
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityInteractor
import io.github.vladchenko.weatherforecast.feature.geolocation.data.DeviceLocationProvider
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.Geolocator
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.PermissionChecker
import io.github.vladchenko.weatherforecast.feature.geolocation.presentation.viewmodel.GeoLocationViewModelFactory
import io.github.vladchenko.weatherforecast.presentation.converter.appbar.AppBarStateMapper
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModelFactory
import javax.inject.Singleton

/**
 * Dagger module for providing high-level feature components and view models.
 *
 * This module defines bindings for:
 * - [ResponseProcessor] to handle API response validation and error mapping
 * - [GeoLocationViewModelFactory] for creating location-aware view models
 * - [AppBarStateMapper] to convert domain state into UI-specific app bar data
 * - [AppBarViewModelFactory] for creating shared app bar view models
 *
 * All dependencies are scoped to [SingletonComponent], ensuring single instances
 * across the application lifecycle. Depends on core services such as logging,
 * resource management, dispatchers, and connectivity observation.
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
    fun provideAppBarStateConverter(resourceManager: ResourceManager): AppBarStateMapper {
        return AppBarStateMapper(resourceManager)
    }

    @Singleton
    @Provides
    fun provideAppBarViewModelFactory(
        statusRenderer: StatusRenderer,
        resourceManager: ResourceManager,
        appBarStateMapper: AppBarStateMapper
    ): AppBarViewModelFactory {
        return AppBarViewModelFactory(
            statusRenderer,
            resourceManager,
            appBarStateMapper
        )
    }
}