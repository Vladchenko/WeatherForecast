package com.example.weatherforecast.di

import android.app.Application
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.converter.ForecastDataToDomainModelsConverter
import com.example.weatherforecast.data.converter.HourlyForecastDataToDomainModelsConverter
import com.example.weatherforecast.data.database.WeatherForecastDAO
import com.example.weatherforecast.data.repository.WeatherForecastRepositoryImpl
import com.example.weatherforecast.data.repository.WeatherForecastRemoteRepository
import com.example.weatherforecast.data.repository.WeatherForecastLocalRepository
import com.example.weatherforecast.data.repository.datasource.WeatherForecastLocalDataSource
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.data.repository.datasourceimpl.WeatherForecastLocalDataSourceImpl
import com.example.weatherforecast.data.repository.datasourceimpl.WeatherForecastRemoteDataSourceImpl
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.data.util.ResponseProcessor
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRepository
import com.example.weatherforecast.geolocation.Geolocator
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModelFactory
import com.example.weatherforecast.presentation.viewmodel.forecast.HourlyForecastViewModelFactory
import com.example.weatherforecast.presentation.viewmodel.geolocation.GeoLocationViewModelFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection (Dagger) module.
 */
@Module
@InstallIn(SingletonComponent::class)
class WeatherForecastModule {

    @Singleton
    @Provides
    fun provideLoggingService(): LoggingService {
        return LoggingService()
    }

    @Singleton
    @Provides
    fun provideResponseProcessor(): ResponseProcessor {
        return ResponseProcessor()
    }

    @Singleton
    @Provides
    fun provideWeatherForecastLocalDataSource(forecastDAO: WeatherForecastDAO): WeatherForecastLocalDataSource {
        return WeatherForecastLocalDataSourceImpl(forecastDAO)
    }

    @Singleton
    @Provides
    fun provideWeatherForecastRemoteDataSource(
        weatherForecastApiService: WeatherForecastApiService,
        loggingService: LoggingService,
        responseProcessor: ResponseProcessor
    ): WeatherForecastRemoteDataSource {
        return WeatherForecastRemoteDataSourceImpl(
            weatherForecastApiService,
            loggingService,
            responseProcessor
        )
    }
    
    @Singleton
    @Provides
    fun provideWeatherForecastConverter(): ForecastDataToDomainModelsConverter {
        return ForecastDataToDomainModelsConverter()
    }

    @Singleton
    @Provides
    fun provideHourlyForecastConverter(): HourlyForecastDataToDomainModelsConverter {
        return HourlyForecastDataToDomainModelsConverter()
    }

    @Singleton
    @Provides
    fun provideWeatherForecastRemoteRepository(
        remoteDataSource: WeatherForecastRemoteDataSource,
        modelsConverter: ForecastDataToDomainModelsConverter,
        hourlyForecastConverter: HourlyForecastDataToDomainModelsConverter,
        coroutineDispatchers: CoroutineDispatchers,
        temperatureType: TemperatureType
    ): WeatherForecastRemoteRepository {
        return WeatherForecastRemoteRepository(
            remoteDataSource,
            modelsConverter,
            hourlyForecastConverter,
            coroutineDispatchers,
            temperatureType
        )
    }

    @Singleton
    @Provides
    fun provideWeatherForecastLocalRepository(
        localDataSource: WeatherForecastLocalDataSource,
        modelsConverter: ForecastDataToDomainModelsConverter,
        coroutineDispatchers: CoroutineDispatchers,
        temperatureType: TemperatureType
    ): WeatherForecastLocalRepository {
        return WeatherForecastLocalRepository(
            localDataSource,
            modelsConverter,
            coroutineDispatchers,
            temperatureType
        )
    }

    @Singleton
    @Provides
    fun provideWeatherForecastRepository(
        remoteRepository: WeatherForecastRemoteRepository,
        localRepository: WeatherForecastLocalRepository
    ): WeatherForecastRepository {
        return WeatherForecastRepositoryImpl(
            remoteRepository,
            localRepository
        )
    }

    @Singleton
    @Provides
    fun provideWeatherForecastRemoteInteractor(weatherForecastRepository: WeatherForecastRepository): WeatherForecastRemoteInteractor {
        return WeatherForecastRemoteInteractor(weatherForecastRepository)
    }

    @Singleton
    @Provides
    fun provideWeatherForecastLocalInteractor(weatherForecastRepository: WeatherForecastRepository): WeatherForecastLocalInteractor {
        return WeatherForecastLocalInteractor(weatherForecastRepository)
    }

    @Singleton
    @Provides
    fun provideForecastViewModelFactory(
        temperatureType: TemperatureType,
        connectivityObserver: ConnectivityObserver,
        chosenCityInteractor: ChosenCityInteractor,
        coroutineDispatchers: CoroutineDispatchers,
        forecastLocalInteractor: WeatherForecastLocalInteractor,
        forecastRemoteInteractor: WeatherForecastRemoteInteractor
    ): WeatherForecastViewModelFactory {
        return WeatherForecastViewModelFactory(
            temperatureType,
            connectivityObserver,
            chosenCityInteractor,
            coroutineDispatchers,
            forecastLocalInteractor,
            forecastRemoteInteractor
        )
    }

    @Singleton
    @Provides
    fun provideHourlyForecastViewModelFactory(
        temperatureType: TemperatureType,
        connectivityObserver: ConnectivityObserver,
        chosenCityInteractor: ChosenCityInteractor,
        coroutineDispatchers: CoroutineDispatchers,
        forecastLocalInteractor: WeatherForecastLocalInteractor,
        forecastRemoteInteractor: WeatherForecastRemoteInteractor
    ): HourlyForecastViewModelFactory {
        return HourlyForecastViewModelFactory(
            temperatureType,
            connectivityObserver,
            chosenCityInteractor,
            coroutineDispatchers,
            forecastLocalInteractor,
            forecastRemoteInteractor
        )
    }

    @Singleton
    @Provides
    fun provideGeoLocationViewModelFactory(
        app: Application,
        geoLocationHelper: Geolocator,
        geoLocator: WeatherForecastGeoLocator,
        connectivityObserver: ConnectivityObserver,
        chosenCityInteractor: ChosenCityInteractor,
        coroutineDispatchers: CoroutineDispatchers,
    ): GeoLocationViewModelFactory {
        return GeoLocationViewModelFactory(
            app,
            geoLocationHelper,
            geoLocator,
            connectivityObserver,
            chosenCityInteractor,
            coroutineDispatchers
        )
    }
}