package io.github.vladchenko.weatherforecast.feature.recentcities.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.data.database.WeatherForecastDatabase
import io.github.vladchenko.weatherforecast.feature.recentcities.data.mapper.RecentCitiesMapper
import io.github.vladchenko.weatherforecast.feature.recentcities.data.repository.RecentCitiesRepositoryImpl
import io.github.vladchenko.weatherforecast.feature.recentcities.data.repository.datasource.RecentCitiesDAO
import io.github.vladchenko.weatherforecast.feature.recentcities.data.repository.datasource.RecentCitiesDataSource
import io.github.vladchenko.weatherforecast.feature.recentcities.data.repository.datasourceimpl.RecentCitiesDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.RecentCitiesInteractor
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.RecentCitiesRepository
import kotlinx.serialization.InternalSerializationApi
import javax.inject.Singleton

/**
 * Dagger module for providing dependencies related to the Recent Cities feature.
 *
 * This module defines how components such as mappers, data sources, repository,
 * and interactor are created and injected within the application. All bindings
 * are scoped to the SingletonComponent to ensure single instances across the app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class CityManagementModule {

    @Provides
    @Singleton
    @InternalSerializationApi
    fun provideRecentCitiesDAO(database: WeatherForecastDatabase): RecentCitiesDAO {
        return database.getRecentCitiesDao()
    }

    @Singleton
    @Provides
    @InternalSerializationApi
    fun provideRecentCitiesMapper(): RecentCitiesMapper {
        return RecentCitiesMapper()
    }

    @Singleton
    @Provides
    fun provideRecentCitiesDataSource(
        dao: RecentCitiesDAO,
        loggingService: LoggingService
    ): RecentCitiesDataSource {
        return RecentCitiesDataSourceImpl(
            dao,
            loggingService
        )
    }

    @Singleton
    @Provides
    fun provideRecentCitiesRepository(
        recentCitiesMapper: RecentCitiesMapper,
        coroutineDispatchers: CoroutineDispatchers,
        recentCitiesDataSource: RecentCitiesDataSource
    ): RecentCitiesRepository {
        return RecentCitiesRepositoryImpl(
            recentCitiesMapper,
            coroutineDispatchers,
            recentCitiesDataSource
        )
    }

    @Singleton
    @Provides
    fun provideRecentCitiesInteractor(repository: RecentCitiesRepository): RecentCitiesInteractor {
        return RecentCitiesInteractor(repository)
    }
}