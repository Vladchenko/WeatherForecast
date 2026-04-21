package io.github.vladchenko.weatherforecast.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.data.mapper.DataErrorToForecastErrorMapper
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManagerImpl
import io.github.vladchenko.weatherforecast.core.ui.dialog.AlertDialogFactory
import io.github.vladchenko.weatherforecast.core.ui.dialog.AlertDialogHelper
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchersImpl
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.geolocation.data.permission.PermissionCheckerImpl
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.PermissionChecker
import io.github.vladchenko.weatherforecast.feature.geolocation.presentation.dialog.LocationDialogFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Dagger module for providing core application-wide dependencies.
 *
 * This module defines bindings for fundamental components used across features,
 * including:
 * - [CoroutineDispatchers] and [CoroutineScope] for background operations
 * - [PreferencesManager] for persistent key-value storage
 * - [LoggingService] for consistent logging throughout the app
 * - [PermissionChecker] for handling runtime location permissions
 * - [ResourceManager] for accessing app resources (strings, colors, etc.)
 * - Factories and helpers for common UI dialogs ([AlertDialogFactory], [LocationDialogFactory])
 * - [DataErrorToForecastErrorMapper] for converting data-layer errors to domain errors
 *
 * All bindings are scoped to [SingletonComponent], ensuring single instances
 * live for the entire application lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class CoreModule {

    @Singleton
    @Provides
    fun provideCoroutineDispatchers(): CoroutineDispatchers {
        return CoroutineDispatchersImpl()
    }

    @Singleton
    @Provides
    fun provideCoroutineScope(coroutineDispatchers: CoroutineDispatchers): CoroutineScope {
        return CoroutineScope(SupervisorJob() + coroutineDispatchers.default)
    }

    @Singleton
    @Provides
    fun providePreferencesManager(coroutineScope: CoroutineScope,
                                  @ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context, coroutineScope)
    }

    @Singleton
    @Provides
    fun provideLoggingService(): LoggingService {
        return LoggingService()
    }

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

    @Provides
    @Singleton
    fun provideAlertDialogFactory(): AlertDialogFactory = AlertDialogFactory()

    @Provides
    @Singleton
    fun provideAlertDialogHelper(
        @ApplicationContext context: Context
    ): AlertDialogHelper = AlertDialogHelper(context)

    @Singleton
    @Provides
    fun provideDataErrorToForecastErrorMapper(): DataErrorToForecastErrorMapper =
        DataErrorToForecastErrorMapper()

    @Provides
    @Singleton
    fun provideLocationDialogFactory(
        alertDialogFactory: AlertDialogFactory,
        resourceManager: ResourceManager
    ): LocationDialogFactory = LocationDialogFactory(alertDialogFactory, resourceManager)
}