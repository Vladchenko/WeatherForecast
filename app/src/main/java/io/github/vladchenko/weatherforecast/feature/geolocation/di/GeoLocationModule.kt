package io.github.vladchenko.weatherforecast.feature.geolocation.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.di.NominatimRetrofit
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.geolocation.data.DeviceLocationProvider
import io.github.vladchenko.weatherforecast.feature.geolocation.data.GeolocatorImpl
import io.github.vladchenko.weatherforecast.feature.geolocation.data.api.NominatimApi
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.Geolocator
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Hilt module for providing geolocation-related dependencies for the application.
 *
 * Defines how components such as [DeviceLocationProvider] and [Geolocator]
 * are created and injected. All bindings are tied to [SingletonComponent],
 * ensuring single instances throughout the app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class GeoLocationModule {

    @Singleton
    @Provides
    fun provideNominatimApi(@NominatimRetrofit retrofit: Retrofit): NominatimApi {
        return retrofit.create(NominatimApi::class.java)
    }

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
