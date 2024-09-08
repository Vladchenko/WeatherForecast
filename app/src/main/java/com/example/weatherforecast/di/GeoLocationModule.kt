package com.example.weatherforecast.di

import android.content.Context
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.geolocation.Geolocator
import com.example.weatherforecast.geolocation.GeolocatorImpl
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class GeoLocationModule {

    @Singleton
    @Provides
    fun provideWeatherForecastGeoLocator(@ApplicationContext context: Context): WeatherForecastGeoLocator {
        return WeatherForecastGeoLocator(context)
    }

    @Singleton
    @Provides
    fun provideGeolocator(
        @ApplicationContext context: Context,
        coroutineDispatchers: CoroutineDispatchers
    ): Geolocator {
        return GeolocatorImpl(context, coroutineDispatchers)
    }
}