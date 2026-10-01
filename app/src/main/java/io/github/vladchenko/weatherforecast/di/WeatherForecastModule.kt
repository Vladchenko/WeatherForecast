package io.github.vladchenko.weatherforecast.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.data.util.ResponseProcessor
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
}