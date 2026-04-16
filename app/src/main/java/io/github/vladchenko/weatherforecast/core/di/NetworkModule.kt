package io.github.vladchenko.weatherforecast.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.BuildConfig
import io.github.vladchenko.weatherforecast.core.di.DiConstants.WEATHER_RETROFIT_NAME
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserverImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Dagger module for providing network-related dependencies across the application.
 *
 * This module defines how core networking components are created and injected,
 * including:
 * - [ConnectivityObserver] for monitoring network connectivity state
 * - Named [Retrofit] instance for weather API calls ([WEATHER_RETROFIT_NAME])
 *
 * The Retrofit instance is configured with:
 * - Base URL from BuildConfig (e.g., "https://api.openweathermap.org/")
 * - [HttpLoggingInterceptor] enabled only in debug builds
 * - [GsonConverterFactory] for JSON parsing
 *
 * All bindings are scoped to [SingletonComponent] to ensure single instances
 * throughout the app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Singleton
    @Provides
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver {
        return ConnectivityObserverImpl(context)
    }

    @Singleton
    @Provides
    @Named(WEATHER_RETROFIT_NAME)
    fun provideWeatherRetrofit(): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
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
}