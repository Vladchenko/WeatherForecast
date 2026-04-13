package io.github.vladchenko.weatherforecast.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.data.api.NominatimApi
import io.github.vladchenko.weatherforecast.data.util.LoggingService
import io.github.vladchenko.weatherforecast.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.geolocation.DeviceLocationProvider
import io.github.vladchenko.weatherforecast.geolocation.Geolocator
import io.github.vladchenko.weatherforecast.geolocation.GeolocatorImpl
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
    fun provideWeatherForecastGeoLocator(
        loggingService: LoggingService,
        @ApplicationContext context: Context
    ): DeviceLocationProvider {
        return DeviceLocationProvider(loggingService, context)
    }

    @Singleton
    @Provides
    fun provideGeolocator(
        nominatimApi: NominatimApi,
        coroutineDispatchers: CoroutineDispatchers
    ): Geolocator {
        return GeolocatorImpl(nominatimApi, coroutineDispatchers)
    }
}