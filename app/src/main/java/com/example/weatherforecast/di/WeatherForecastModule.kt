package com.example.weatherforecast.di

import android.app.Application
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.converter.CurrentForecastModelConverter
import com.example.weatherforecast.data.converter.HourlyForecastModelsConverter
import com.example.weatherforecast.data.database.HourlyForecastDAO
import com.example.weatherforecast.data.database.WeatherForecastDAO
import com.example.weatherforecast.data.repository.HourlyForecastRepositoryImpl
import com.example.weatherforecast.data.repository.WeatherForecastRepositoryImpl
import com.example.weatherforecast.data.repository.datasource.HourlyForecastLocalDataSource
import com.example.weatherforecast.data.repository.datasource.HourlyForecastRemoteDataSource
import com.example.weatherforecast.data.repository.datasource.WeatherForecastLocalDataSource
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.data.repository.datasourceimpl.HourlyForecastLocalDataSourceImpl
import com.example.weatherforecast.data.repository.datasourceimpl.HourlyForecastRemoteDataSourceImpl
import com.example.weatherforecast.data.repository.datasourceimpl.WeatherForecastLocalDataSourceImpl
import com.example.weatherforecast.data.repository.datasourceimpl.WeatherForecastRemoteDataSourceImpl
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.data.util.ResponseProcessor
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.HourlyForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.HourlyForecastRemoteInteractor
import com.example.weatherforecast.domain.forecast.HourlyForecastRepository
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRepository
import com.example.weatherforecast.geolocation.Geolocator
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator
import com.example.weatherforecast.presentation.converter.ForecastDomainToUiConverter
import com.example.weatherforecast.presentation.converter.ForecastDomainToUiConverterImpl
import com.example.weatherforecast.presentation.viewmodel.forecast.HourlyForecastViewModelFactory
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModelFactory
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
            weatherForecastApiService, loggingService, responseProcessor
        )
    }

    @Singleton
    @Provides
    fun provideHourlyForecastLocalDataSource(forecastDAO: HourlyForecastDAO): HourlyForecastLocalDataSource {
        return HourlyForecastLocalDataSourceImpl(forecastDAO)
    }

    @Singleton
    @Provides
    fun provideHourlyForecastRemoteDataSource(
        hourlyForecastApiService: WeatherForecastApiService,
        loggingService: LoggingService,
        responseProcessor: ResponseProcessor
    ): HourlyForecastRemoteDataSource {
        return HourlyForecastRemoteDataSourceImpl(
            hourlyForecastApiService, loggingService, responseProcessor
        )
    }

    @Singleton
    @Provides
    fun provideWeatherForecastDomainConverter(): CurrentForecastModelConverter {
        return CurrentForecastModelConverter()
    }

    @Singleton
    @Provides
    fun provideWeatherForecastUiConverter(): ForecastDomainToUiConverter {
        return ForecastDomainToUiConverterImpl()
    }

    @Singleton
    @Provides
    fun provideHourlyForecastConverter(): HourlyForecastModelsConverter {
        return HourlyForecastModelsConverter()
    }

    @Singleton
    @Provides
    fun provideWeatherForecastRepository(
        weatherForecastRemoteDataSource: WeatherForecastRemoteDataSource,
        weatherForecastLocalDataSource: WeatherForecastLocalDataSource,
        converter: CurrentForecastModelConverter,
        coroutineDispatchers: CoroutineDispatchers,
        temperaturesType: TemperatureType
    ): WeatherForecastRepository {
        return WeatherForecastRepositoryImpl(
            weatherForecastRemoteDataSource,
            weatherForecastLocalDataSource,
            converter,
            coroutineDispatchers,
            temperaturesType
        )
    }

    @Singleton
    @Provides
    fun provideHourlyForecastRepository(
        hourlyForecastRemoteDataSource: HourlyForecastRemoteDataSource,
        hourlyForecastLocalDataSource: HourlyForecastLocalDataSource,
        converter: HourlyForecastModelsConverter,
        coroutineDispatchers: CoroutineDispatchers,
        temperaturesType: TemperatureType
    ): HourlyForecastRepository {
        return HourlyForecastRepositoryImpl(
            hourlyForecastRemoteDataSource,
            hourlyForecastLocalDataSource,
            converter,
            coroutineDispatchers,
            temperaturesType
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
    fun provideHourlyForecastRemoteInteractor(hourlyForecastRepository: HourlyForecastRepository): HourlyForecastRemoteInteractor {
        return HourlyForecastRemoteInteractor(hourlyForecastRepository)
    }

    @Singleton
    @Provides
    fun provideHourlyForecastLocalInteractor(hourlyForecastRepository: HourlyForecastRepository): HourlyForecastLocalInteractor {
        return HourlyForecastLocalInteractor(hourlyForecastRepository)
    }

    @Singleton
    @Provides
    fun provideForecastViewModelFactory(
        app: Application,
        temperatureType: TemperatureType,
        connectivityObserver: ConnectivityObserver,
        chosenCityInteractor: ChosenCityInteractor,
        coroutineDispatchers: CoroutineDispatchers,
        forecastLocalInteractor: WeatherForecastLocalInteractor,
        forecastRemoteInteractor: WeatherForecastRemoteInteractor,
        uiConverter: ForecastDomainToUiConverter
    ): WeatherForecastViewModelFactory {
        return WeatherForecastViewModelFactory(
            app,
            temperatureType,
            connectivityObserver,
            chosenCityInteractor,
            coroutineDispatchers,
            forecastLocalInteractor,
            forecastRemoteInteractor,
            uiConverter
        )
    }

    @Singleton
    @Provides
    fun provideHourlyForecastViewModelFactory(
        temperatureType: TemperatureType,
        connectivityObserver: ConnectivityObserver,
        chosenCityInteractor: ChosenCityInteractor,
        coroutineDispatchers: CoroutineDispatchers,
        forecastLocalInteractor: HourlyForecastLocalInteractor,
        forecastRemoteInteractor: HourlyForecastRemoteInteractor
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