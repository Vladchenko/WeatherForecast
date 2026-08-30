package io.github.vladchenko.weatherforecast.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.geolocation.GeoLocationEventBus
import io.github.vladchenko.weatherforecast.core.navigation.NavigationEventBus
import io.github.vladchenko.weatherforecast.core.ui.event.CityErrorEventBus
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.data.util.ResponseProcessor
import io.github.vladchenko.weatherforecast.presentation.coordinator.CitySelectionCoordinator
import io.github.vladchenko.weatherforecast.presentation.dialog.WeatherDialogController
import javax.inject.Singleton

/**
 * Dagger module for providing high-level feature components and view models.
 *
 * This module defines bindings for:
 * - [ResponseProcessor] to handle API response validation and error mapping
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
    fun provideCitySelectionCoordinator(
        statusStateHolder: StatusStateHolder,
        cityErrorEventBus: CityErrorEventBus,
        navigationEventBus: NavigationEventBus,
        geoLocationEventBus: GeoLocationEventBus,
        dialogController: WeatherDialogController,
    ): CitySelectionCoordinator {
        return CitySelectionCoordinator(
            statusStateHolder,
            cityErrorEventBus,
            navigationEventBus,
            geoLocationEventBus,
            dialogController
        )
    }
}