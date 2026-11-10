package io.github.vladchenko.weatherforecast.feature.currentweather.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.data.mapper.DataErrorToForecastErrorMapper
import io.github.vladchenko.weatherforecast.core.database.WeatherForecastDatabase
import io.github.vladchenko.weatherforecast.core.di.HiltRetrofitBuilder
import io.github.vladchenko.weatherforecast.core.geolocation.GeoLocationEventBus
import io.github.vladchenko.weatherforecast.core.navigation.api.NavigationEventBus
import io.github.vladchenko.weatherforecast.core.network.util.ResponseProcessor
import io.github.vladchenko.weatherforecast.core.presentation.mapper.ForecastErrorToMessageIdMapper
import io.github.vladchenko.weatherforecast.core.ui.dialog.WeatherDialogController
import io.github.vladchenko.weatherforecast.core.ui.event.CityErrorEventBus
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.currentweather.data.api.CurrentWeatherApiService
import io.github.vladchenko.weatherforecast.feature.currentweather.data.dao.CurrentWeatherDAO
import io.github.vladchenko.weatherforecast.feature.currentweather.data.datasource.local.CurrentWeatherLocalDataSource
import io.github.vladchenko.weatherforecast.feature.currentweather.data.datasource.local.CurrentWeatherLocalDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.currentweather.data.datasource.remote.CurrentWeatherRemoteDataSource
import io.github.vladchenko.weatherforecast.feature.currentweather.data.datasource.remote.CurrentWeatherRemoteDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.currentweather.data.mapper.CurrentWeatherDtoMapper
import io.github.vladchenko.weatherforecast.feature.currentweather.data.mapper.CurrentWeatherEntityMapper
import io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.CurrentWeatherRepositoryImpl
import io.github.vladchenko.weatherforecast.feature.currentweather.di.CurrentWeatherConstants.CURRENT_WEATHER_RETROFIT
import io.github.vladchenko.weatherforecast.feature.currentweather.domain.CurrentWeatherRepository
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.CurrentWeatherInteractor
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.CityErrorCoordinator
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.domaintouimapper.WeatherDomainToUiMapper
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.domaintouimapper.WeatherDomainToUiMapperImpl
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.outputmapper.WeatherOutputMapper
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.outputmapper.WeatherOutputMapperImpl
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.WeatherResponseHandler
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module for providing dependencies for the current weather feature.
 *
 * Defines how components such as data sources,
 * mappers, repository, interactor, and view model factory are created and injected.
 * All bindings are tied to SingletonComponent, ensuring single instances
 * throughout the app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class CurrentWeatherModule {

    @Provides
    @Singleton
    fun provideWeatherForecastDAO(database: WeatherForecastDatabase): CurrentWeatherDAO {
        return database.getWeatherForecastInstance()
    }

    @Singleton
    @Provides
    @Named(CURRENT_WEATHER_RETROFIT)
    fun provideCurrentWeatherRetrofit(hiltRetrofitBuilder: HiltRetrofitBuilder): Retrofit {
        return hiltRetrofitBuilder.createWeatherRetrofit()
    }

    @Singleton
    @Provides
    fun provideWeatherForecastApiService(
        @Named(CURRENT_WEATHER_RETROFIT) retrofit: Retrofit
    ): CurrentWeatherApiService {
        return retrofit.create(CurrentWeatherApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideWeatherForecastLocalDataSource(
        loggingService: LoggingService,
        forecastDAO: CurrentWeatherDAO
    ): CurrentWeatherLocalDataSource {
        return CurrentWeatherLocalDataSourceImpl(forecastDAO, loggingService)
    }

    @Singleton
    @Provides
    fun provideWeatherForecastRemoteDataSource(
        currentWeatherApiService: CurrentWeatherApiService,
        loggingService: LoggingService,
        responseProcessor: ResponseProcessor
    ): CurrentWeatherRemoteDataSource {
        return CurrentWeatherRemoteDataSourceImpl(
            currentWeatherApiService, loggingService, responseProcessor
        )
    }

    @Singleton
    @Provides
    fun provideWeatherForecastUiConverter(): WeatherDomainToUiMapper {
        return WeatherDomainToUiMapperImpl()
    }

    @Singleton
    @Provides
    fun provideCurrentWeatherDtoMapper(): CurrentWeatherDtoMapper {
        return CurrentWeatherDtoMapper()
    }

    @Singleton
    @Provides
    fun provideCurrentWeatherEntityMapper(): CurrentWeatherEntityMapper {
        return CurrentWeatherEntityMapper()
    }

    @Singleton
    @Provides
    fun provideWeatherForecastRepository(
        loggingService: LoggingService,
        dtoMapper: CurrentWeatherDtoMapper,
        entityMapper: CurrentWeatherEntityMapper,
        coroutineDispatchers: CoroutineDispatchers,
        errorMapper: DataErrorToForecastErrorMapper,
        currentWeatherLocalDataSource: CurrentWeatherLocalDataSource,
        currentWeatherRemoteDataSource: CurrentWeatherRemoteDataSource,
    ): CurrentWeatherRepository {
        return CurrentWeatherRepositoryImpl(
            loggingService,
            dtoMapper,
            entityMapper,
            coroutineDispatchers,
            errorMapper,
            currentWeatherLocalDataSource,
            currentWeatherRemoteDataSource
        )
    }

    @Singleton
    @Provides
    fun provideWeatherForecastRemoteInteractor(
        currentWeatherRepository: CurrentWeatherRepository
    ): CurrentWeatherInteractor {
        return CurrentWeatherInteractor(currentWeatherRepository)
    }

    @Singleton
    @Provides
    fun provideWeatherResponseHandler(
        loggingService: LoggingService,
        cityErrorEventBus: CityErrorEventBus,
        statusStateHolder: StatusStateHolder,
        errorToMessageMapper: ForecastErrorToMessageIdMapper,
    ): WeatherResponseHandler {
        return WeatherResponseHandler(
            loggingService,
            cityErrorEventBus,
            statusStateHolder,
            errorToMessageMapper
        )
    }

    @Singleton
    @Provides
    fun provideWeatherOutputMapper(
        weatherResponseHandler: WeatherResponseHandler,
        weatherDomainToUiMapper: WeatherDomainToUiMapper,
    ): WeatherOutputMapper {
        return WeatherOutputMapperImpl(
            weatherResponseHandler,
            weatherDomainToUiMapper
        )
    }

    @Singleton
    @Provides
    fun provideCityErrorCoordinator(
        statusStateHolder: StatusStateHolder,
        cityErrorEventBus: CityErrorEventBus,
        navigationEventBus: NavigationEventBus,
        geoLocationEventBus: GeoLocationEventBus,
        dialogController: WeatherDialogController,
    ): CityErrorCoordinator {
        return CityErrorCoordinator(
            statusStateHolder,
            cityErrorEventBus,
            navigationEventBus,
            geoLocationEventBus,
            dialogController
        )
    }
}