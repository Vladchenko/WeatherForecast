package io.github.vladchenko.weatherforecast.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.dialog.AlertDialogFactory
import io.github.vladchenko.weatherforecast.core.ui.dialog.AlertDialogHelper
import io.github.vladchenko.weatherforecast.feature.geolocation.presentation.dialog.LocationDialogFactory
import io.github.vladchenko.weatherforecast.presentation.dialog.WeatherDialogController
import io.github.vladchenko.weatherforecast.presentation.dialog.WeatherDialogControllerImpl
import io.github.vladchenko.weatherforecast.presentation.dialog.WeatherDialogFactory
import javax.inject.Singleton

/**
 * Dagger Hilt module that provides presentation-layer dependencies for the weather feature.
 *
 * This object module is installed in the [SingletonComponent], ensuring that singleton-scoped
 * factories are created once and reused across the application.
 *
 * ## Provided Dependencies
 * 1. [WeatherDialogFactory] — responsible for creating specific [AlertDialogFactory] implementations
 *    (e.g., location dialogs, error dialogs) used throughout the presentation layer.
 * 2. [WeatherDialogController] — manages the presentation of weather-related dialogs
 *    (e.g., city not found, permission denied, geolocation error).
 *
 * By providing these as Hilt `@Singleton` bindings, the module enables loose coupling
 * between ViewModels/Coordinators and concrete dialog implementations, improving testability
 * and adhering to the Single Responsibility Principle.
 *
 * @see WeatherDialogFactory
 * @see WeatherDialogController
 * @see WeatherDialogControllerImpl
 */
@Module
@InstallIn(SingletonComponent::class)
object PresentationModule {

    /**
     * Provides the [WeatherDialogFactory] used to create weather-related dialog instances.
     *
     * Combines the base [AlertDialogFactory], location-specific dialogs via [LocationDialogFactory],
     * and localized strings from [ResourceManager] into a single factory.
     *
     * @param baseDialogFactory Base dialog factory for standard alert dialogs.
     * @param locationDialogFactory Factory for location/geolocation-specific dialogs.
     * @param resourceManager Provides localized string resources for dialog content.
     * @return A fully configured [WeatherDialogFactory] instance.
     */
    @Provides
    @Singleton
    fun provideWeatherDialogFactory(
        baseDialogFactory: AlertDialogFactory,
        locationDialogFactory: LocationDialogFactory,
        resourceManager: ResourceManager
    ): WeatherDialogFactory = WeatherDialogFactory(
        baseDialogFactory,
        locationDialogFactory,
        resourceManager
    )

    /**
     * Provides the [WeatherDialogController] responsible for managing dialog presentation.
     *
     * Wraps [WeatherDialogFactory] and [AlertDialogHelper] to offer a unified API
     * for showing and dismissing weather-related dialogs (e.g., city not found,
     * permission permanently denied, geolocation error).
     *
     * @param dialogFactory Factory for creating specific dialog instances.
     * @param dialogHelper Helper for managing dialog lifecycle and view interactions.
     * @return A fully configured [WeatherDialogController] instance.
     */
    @Provides
    @Singleton
    fun provideWeatherDialogController(
        dialogFactory: WeatherDialogFactory,
        dialogHelper: AlertDialogHelper
    ): WeatherDialogController = WeatherDialogControllerImpl(dialogFactory, dialogHelper)
}