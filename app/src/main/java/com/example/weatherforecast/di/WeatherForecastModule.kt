package com.example.weatherforecast.di

import android.app.Application
import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.converter.ForecastDataToDomainModelsConverter
import com.example.weatherforecast.data.database.WeatherForecastDAO
import com.example.weatherforecast.data.repository.WeatherForecastRepositoryImpl
import com.example.weatherforecast.data.repository.datasource.WeatherForecastLocalDataSource
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.data.repository.datasourceimpl.WeatherForecastLocalDataSourceImpl
import com.example.weatherforecast.data.repository.datasourceimpl.WeatherForecastRemoteDataSourceImpl
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRepository
import com.example.weatherforecast.geolocation.Geolocator
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator
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
    fun provideWeatherForecastLocalDataSource(forecastDAO: WeatherForecastDAO): WeatherForecastLocalDataSource {
        return WeatherForecastLocalDataSourceImpl(forecastDAO)
    }

    @Singleton
    @Provides
    fun provideWeatherForecastRemoteDataSource(weatherForecastApiService: WeatherForecastApiService): WeatherForecastRemoteDataSource {
        return WeatherForecastRemoteDataSourceImpl(weatherForecastApiService)
    }

    @Singleton
    @Provides
    fun provideWeatherForecastConverter(): ForecastDataToDomainModelsConverter {
        return ForecastDataToDomainModelsConverter()
    }

    @Singleton
    @Provides
    fun provideWeatherForecastRepository(
        weatherForecastRemoteDataSource: WeatherForecastRemoteDataSource,
        weatherForecastLocalDataSource: WeatherForecastLocalDataSource,
        converter: ForecastDataToDomainModelsConverter,
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
        chosenCityInteractor: ChosenCityInteractor,
        coroutineDispatchers: CoroutineDispatchers,
        forecastLocalInteractor: WeatherForecastLocalInteractor,
        forecastRemoteInteractor: WeatherForecastRemoteInteractor
    ): WeatherForecastViewModelFactory {
        return WeatherForecastViewModelFactory(
            temperatureType,
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
        chosenCityInteractor: ChosenCityInteractor,
        coroutineDispatchers: CoroutineDispatchers,
    ): GeoLocationViewModelFactory {
        return GeoLocationViewModelFactory(
            app,
            geoLocationHelper,
            geoLocator,
            chosenCityInteractor,
            coroutineDispatchers
        )
    }
}