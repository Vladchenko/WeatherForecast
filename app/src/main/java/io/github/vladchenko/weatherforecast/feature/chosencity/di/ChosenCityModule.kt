package io.github.vladchenko.weatherforecast.feature.chosencity.di

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.feature.chosencity.data.datasource.ChosenCityDataSource
import io.github.vladchenko.weatherforecast.feature.chosencity.data.datasource.ChosenCityLocalDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.chosencity.data.repository.ChosenCityRepositoryImpl

import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityRepository
import javax.inject.Singleton

/**
 * Hilt module for providing dependencies for the Chosen City feature.
 *
 * Defines how components such as data source and repository
 * are created and injected. All bindings are tied to [SingletonComponent],
 * ensuring single instances throughout the app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class ChosenCityModule {

    @Singleton
    @Provides
    fun provideChosenCityDataSource(sharedPreferences: SharedPreferences): ChosenCityDataSource {
        return ChosenCityLocalDataSourceImpl(sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideChosenCityRepository(
        chosenCityDataSource: ChosenCityDataSource,
        coroutineDispatchers: CoroutineDispatchers
    ): ChosenCityRepository {
        return ChosenCityRepositoryImpl(coroutineDispatchers, chosenCityDataSource)
    }
}