package com.example.weatherforecast.di

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.weatherforecast.BuildConfig
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.connectivity.ConnectivityObserverImpl
import com.example.weatherforecast.data.api.CityApiService
import com.example.weatherforecast.data.api.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    fun provideRetrofit(): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder().apply {
            addInterceptor(loggingInterceptor)
        }.build()
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .build()
    }

    @Singleton
    @Provides
    fun provideWeatherForecastApiService(retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideCityApiService(retrofit: Retrofit): CityApiService {
        return retrofit.create(CityApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver {
        return ConnectivityObserverImpl(context)
    }
}