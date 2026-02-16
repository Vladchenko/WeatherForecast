package com.example.weatherforecast.di

import android.content.Context
import com.example.weatherforecast.data.preferences.PreferencesManager
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.dispatchers.CoroutineDispatchersImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Dagger Hilt module that provides core application-wide dependencies.
 *
 * This module is installed in the [SingletonComponent], ensuring that all provided instances
 * are created once and shared across the entire app lifecycle.
 *
 * It supplies:
 * - The default [TemperatureType] (currently fixed to Celsius)
 * - A [CoroutineDispatchers] implementation for structured concurrency
 * - A root [CoroutineScope] with a SupervisorJob and default dispatcher, suitable for long-running tasks
 *
 * These dependencies serve as foundational components used by various layers of the app,
 * including ViewModels, repositories, and background workers.
 *
 * @see TemperatureType
 * @see CoroutineDispatchers
 * @see CoroutineScope
 */
@Module
@InstallIn(SingletonComponent::class)
class CoreModule {

    @Singleton
    @Provides
    fun providePreferencesManager(coroutineScope: CoroutineScope,
                                  @ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context, coroutineScope)
    }

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
}