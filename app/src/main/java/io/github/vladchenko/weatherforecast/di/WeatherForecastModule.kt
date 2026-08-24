package io.github.vladchenko.weatherforecast.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.geolocation.GeoLocationEventBus
import io.github.vladchenko.weatherforecast.core.network.NetworkStateHolder
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.data.util.ResponseProcessor
import io.github.vladchenko.weatherforecast.feature.geolocation.data.DeviceLocationProvider
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.Geolocator
import io.github.vladchenko.weatherforecast.feature.geolocation.presentation.viewmodel.GeoLocationViewModelFactory
import io.github.vladchenko.weatherforecast.presentation.dialog.WeatherDialogController
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModelFactory
import javax.inject.Singleton

/**
 * Dagger module for providing high-level feature components and view models.
 *
 * This module defines bindings for:
 * - [ResponseProcessor] to handle API response validation and error mapping
 * - [GeoLocationViewModelFactory] for creating location-aware view models
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
        geoLocator: DeviceLocationProvider,
        statusStateHolder: StatusStateHolder,
        dialogController: WeatherDialogController,
        geoLocationEventBus: GeoLocationEventBus,
    ): GeoLocationViewModelFactory {
        return GeoLocationViewModelFactory(
            geoLocationHelper,
            loggingService,
            geoLocator,
            statusStateHolder,
            dialogController,
            geoLocationEventBus
        )
    }

    @Singleton
    @Provides
    fun provideAppBarViewModelFactory(
        stateHolder: StatusStateHolder,
        networkStateHolder: NetworkStateHolder
    ): AppBarViewModelFactory {
        return AppBarViewModelFactory(
            stateHolder,
            networkStateHolder
        )
    }
}