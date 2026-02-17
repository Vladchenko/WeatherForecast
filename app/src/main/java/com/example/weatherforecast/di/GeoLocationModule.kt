package com.example.weatherforecast.di

import android.content.Context
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.geolocation.DeviceLocationProvider
import com.example.weatherforecast.geolocation.Geolocator
import com.example.weatherforecast.geolocation.GeolocatorImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module that provides geolocation-related dependencies for the application.
 *
 * This module is installed in the [SingletonComponent], ensuring that all provided instances
 * are scoped to the application lifecycle.
 *
 * It supplies:
 * - A [DeviceLocationProvider] instance for handling location permission checks and requests
 * - A [Geolocator] implementation ([GeolocatorImpl]) for retrieving device location via GPS/network
 *
 * Dependencies require the application context and, in the case of [Geolocator], a [CoroutineDispatchers]
 * instance for performing asynchronous location operations.
 *
 * @see DeviceLocationProvider
 * @see Geolocator
 * @see GeolocatorImpl
 */
@Module
@InstallIn(SingletonComponent::class)
class GeoLocationModule {

    @Singleton
    @Provides
    fun provideWeatherForecastGeoLocator(@ApplicationContext context: Context): DeviceLocationProvider {
        return DeviceLocationProvider(context)
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