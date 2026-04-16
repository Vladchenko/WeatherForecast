package io.github.vladchenko.weatherforecast.feature.geolocation.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.geolocation.data.DeviceLocationProvider
import io.github.vladchenko.weatherforecast.feature.geolocation.data.GeolocatorImpl
import io.github.vladchenko.weatherforecast.feature.geolocation.data.api.GeoLocationApiConstants.DEVELOPER_EMAIL
import io.github.vladchenko.weatherforecast.feature.geolocation.data.api.GeoLocationApiConstants.NOMINATIM
import io.github.vladchenko.weatherforecast.feature.geolocation.data.api.GeoLocationApiConstants.NOMINATIM_BASE_URL
import io.github.vladchenko.weatherforecast.feature.geolocation.data.api.GeoLocationApiConstants.USER_AGENT
import io.github.vladchenko.weatherforecast.feature.geolocation.data.api.NominatimApi
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.Geolocator
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Dagger Hilt module that provides geolocation-related dependencies for the application.
 *
 * This module is installed in the [dagger.hilt.components.SingletonComponent], ensuring that all provided instances
 * are scoped to the application lifecycle.
 *
 * It supplies:
 * - A [io.github.vladchenko.weatherforecast.feature.geolocation.data.DeviceLocationProvider] instance for handling location permission checks and requests
 * - A [Geolocator] implementation ([GeolocatorImpl]) for retrieving device location via GPS/network
 *
 * Dependencies require the application context and, in the case of [Geolocator], a [io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers]
 * instance for performing asynchronous location operations.
 *
 * @see io.github.vladchenko.weatherforecast.feature.geolocation.data.DeviceLocationProvider
 * @see Geolocator
 * @see GeolocatorImpl
 */
@Module
@InstallIn(SingletonComponent::class)
class GeoLocationModule {

    @Singleton
    @Provides
    @Named(NOMINATIM)
    fun provideNominatimRetrofit(): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", USER_AGENT)
                    .addHeader("From", DEVELOPER_EMAIL)
                    .build()
                chain.proceed(request)
            }
            .build()
        return Retrofit.Builder()
            .baseUrl(NOMINATIM_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideNominatimApi(@Named(NOMINATIM) retrofit: Retrofit): NominatimApi {
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