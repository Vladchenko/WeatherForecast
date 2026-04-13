package io.github.vladchenko.weatherforecast.di

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.BuildConfig
import io.github.vladchenko.weatherforecast.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.connectivity.ConnectivityObserverImpl
import io.github.vladchenko.weatherforecast.data.api.ApiConstants.DEVELOPER_EMAIL
import io.github.vladchenko.weatherforecast.data.api.ApiConstants.NOMINATIM_BASE_URL
import io.github.vladchenko.weatherforecast.data.api.ApiConstants.USER_AGENT
import io.github.vladchenko.weatherforecast.data.api.CityApiService
import io.github.vladchenko.weatherforecast.data.api.NominatimApi
import io.github.vladchenko.weatherforecast.data.api.WeatherApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Dagger Hilt module that provides network and connectivity-related dependencies for the application.
 *
 * This module is installed in the [SingletonComponent], ensuring that all provided instances
 * are scoped to the application lifecycle.
 *
 * It supplies:
 * - A configured [Retrofit] instance with:
 *   - [OkHttpClient] featuring HTTP logging (in debug builds)
 *   - [GsonConverterFactory] for JSON parsing
 *   - Base URL from [BuildConfig.API_BASE_URL]
 * - API service interfaces: [WeatherApiService] and [CityApiService]
 * - [WorkManager] and its [Configuration] for background task scheduling
 * - [ConnectivityObserver] implementation to monitor network state changes
 *
 * These dependencies enable type-safe HTTP communication, proper error handling,
 * background work coordination, and reactive connectivity awareness throughout the app.
 *
 * @see Retrofit
 * @see WeatherApiService
 * @see CityApiService
 * @see WorkManager
 * @see ConnectivityObserver
 */
@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Singleton
    @Provides
    @Named(WEATHER)
    fun provideDefaultRetrofit(): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

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
    fun provideWeatherForecastApiService(@Named(WEATHER) retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideCityApiService(@Named(WEATHER) retrofit: Retrofit): CityApiService {
        return retrofit.create(CityApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver {
        return ConnectivityObserverImpl(context)
    }
    
    companion object {
        /**
         * Named binding for the Retrofit instance used with OpenWeatherMap API.
         *
         * Used to provide [WeatherApiService] and [CityApiService].
         */
        const val WEATHER = "WeatherRetrofit"

        /**
         * Named binding for the Retrofit instance used with Nominatim (OpenStreetMap) API.
         *
         * Ensures correct injection of [NominatimApi] with proper headers and base URL.
         */
        const val NOMINATIM = "NominatimRetrofit"
    }
}