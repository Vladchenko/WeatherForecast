package io.github.vladchenko.weatherforecast.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.data.mapper.DataErrorToForecastErrorMapper
import io.github.vladchenko.weatherforecast.core.location.dialog.LocationDialogFactory
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchersImpl
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.core.location.permission.PermissionChecker
import io.github.vladchenko.weatherforecast.core.location.permission.PermissionCheckerImpl
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManagerImpl
import io.github.vladchenko.weatherforecast.core.ui.dialog.AlertDialogFactory
import io.github.vladchenko.weatherforecast.core.ui.dialog.AlertDialogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * TODO
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