package io.github.vladchenko.weatherforecast.feature.chosencity.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesConstants.SHARED_PREFERENCES_KEY
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.feature.chosencity.data.repository.ChosenCityRepositoryImpl
import io.github.vladchenko.weatherforecast.feature.chosencity.data.repository.datasource.ChosenCityDataSource
import io.github.vladchenko.weatherforecast.feature.chosencity.data.repository.datasourceimpl.ChosenCityLocalDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityInteractor
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityRepository
import javax.inject.Singleton

/**
 * Dagger module for providing dependencies related to the Chosen City feature.
 *
 * This module defines how components such as data sources, repository,
 * and interactor are created and injected within the application. All bindings
 * are scoped to the SingletonComponent to ensure single instances across the app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class ChosenCityModule {

    @Singleton
    @Provides
    fun provideChosenCityDataSource(app: Application): ChosenCityDataSource {
        return ChosenCityLocalDataSourceImpl(
            app.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
        )
    }

    @Singleton
    @Provides
    fun provideChosenCityRepository(
        chosenCityDataSource: ChosenCityDataSource,
        coroutineDispatchers: CoroutineDispatchers
    ): ChosenCityRepository {
        return ChosenCityRepositoryImpl(coroutineDispatchers, chosenCityDataSource)
    }

    @Singleton
    @Provides
    fun provideChosenCityInteractor(chosenCityRepository: ChosenCityRepository): ChosenCityInteractor {
        return ChosenCityInteractor(chosenCityRepository)
    }
}