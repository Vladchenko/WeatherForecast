package com.example.weatherforecast.di

import com.example.weatherforecast.presentation.coordinator.ForecastCoordinator
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.utils.ResourceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides presentation-layer dependencies for forecast screen
 */
@Module
@InstallIn(SingletonComponent::class)
object ForecastPresentationModule {

    @Singleton
    @Provides
    fun provideStatusNotifierFactory(resourceManager: ResourceManager): StatusRenderer.Factory {
        return StatusRenderer.Factory(resourceManager)
    }

    @Singleton
    @Provides
    fun provideForecastCoordinatorFactory(): ForecastCoordinator.Factory {
        return ForecastCoordinator.Factory()
    }
}
