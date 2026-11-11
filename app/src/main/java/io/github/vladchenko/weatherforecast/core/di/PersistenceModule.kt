package io.github.vladchenko.weatherforecast.core.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.database.WeatherForecastDatabase
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesConstants.SHARED_PREFERENCES_KEY
import javax.inject.Singleton

/**
 * Hilt module for providing persistence-related dependencies for the application.
 *
 * Defines how [WeatherForecastDatabase] and [SharedPreferences] are created and injected.
 * All bindings are tied to [SingletonComponent], ensuring single instances
 * throughout the app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {

    @Provides
    @Singleton
    fun provideWeatherForecastDatabase(
        @ApplicationContext context: Context
    ): WeatherForecastDatabase {
        return WeatherForecastDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences(
            SHARED_PREFERENCES_KEY,
            Context.MODE_PRIVATE
        )
    }
}
