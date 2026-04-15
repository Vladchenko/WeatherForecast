package io.github.vladchenko.weatherforecast.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.models.data.DataErrorToForecastErrorMapper
import javax.inject.Singleton

/**
 * TODO
 */
@Module
@InstallIn(SingletonComponent::class)
class CoreModule {

    @Singleton
    @Provides
    fun provideDataErrorToForecastErrorMapper(): DataErrorToForecastErrorMapper =
        DataErrorToForecastErrorMapper()
}