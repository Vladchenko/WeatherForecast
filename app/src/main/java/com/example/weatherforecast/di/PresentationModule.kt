package com.example.weatherforecast.di

import com.example.weatherforecast.presentation.alertdialog.AlertDialogFactory
import com.example.weatherforecast.presentation.coordinator.WeatherCoordinator
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.utils.ResourceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module that provides presentation-layer dependencies for the forecast screen.
 *
 * This object module is installed in the [SingletonComponent], ensuring that factories
 * are created once and reused across the application.
 *
 * It provides:
 * - A factory for creating [StatusRenderer] instances, used to display status messages (e.g., loading, error)
 *   with proper string resource resolution via [ResourceManager]
 * - A factory for creating [WeatherCoordinator] instances, responsible for handling navigation
 *   and UI-side effects on the forecast screen
 *
 * These factories allow ViewModels to create presenters or coordinators without tight coupling
 * to concrete implementations, supporting testability and separation of concerns.
 */
@Module
@InstallIn(SingletonComponent::class)
object PresentationModule {

    @Singleton
    @Provides
    fun provideStatusNotifierFactory(resourceManager: ResourceManager): StatusRenderer {
        return StatusRenderer(resourceManager)
    }

    @Singleton
    @Provides
    fun provideForecastCoordinatorFactory(): WeatherCoordinator.Factory {
        return WeatherCoordinator.Factory()
    }

    @Singleton
    @Provides
    fun provideAlertDialogFactory(resourceManager: ResourceManager): AlertDialogFactory {
        return AlertDialogFactory(resourceManager)
    }
}
