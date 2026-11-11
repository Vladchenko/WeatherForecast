package io.github.vladchenko.weatherforecast.core.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.coordinator.NetworkStatusCoordinator
import io.github.vladchenko.weatherforecast.core.network.NetworkStateHolder
import io.github.vladchenko.weatherforecast.core.network.NetworkStateHolderImpl
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserverImpl
import io.github.vladchenko.weatherforecast.core.network.util.ResponseProcessor
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Dagger module for providing network-related dependencies across the application.
 *
 * This module defines how core networking components are created and injected,
 * including:
 * - [ConnectivityObserver] for monitoring network connectivity state
 * - Shared [OkHttpClient] without feature-specific interceptors
 * - [HiltRetrofitBuilder] for creating feature-specific [Retrofit] instances
 *
 * Feature-specific Retrofit instances (with their own baseUrl and interceptors)
 * are created via [HiltRetrofitBuilder].
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

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    @Singleton
    @Provides
    fun provideHiltRetrofitBuilder(okHttpClient: OkHttpClient): HiltRetrofitBuilder {
        return HiltRetrofitBuilder(okHttpClient)
    }

    @Singleton
    @Provides
    @WeatherRetrofit
    fun provideWeatherRetrofit(hiltRetrofitBuilder: HiltRetrofitBuilder): Retrofit {
        return hiltRetrofitBuilder.createWeatherRetrofit()
    }

    @Singleton
    @Provides
    @WeatherSummaryRetrofit
    fun provideWeatherSummaryRetrofit(hiltRetrofitBuilder: HiltRetrofitBuilder): Retrofit {
        return hiltRetrofitBuilder.createWeatherSummaryRetrofit()
    }

    @Singleton
    @Provides
    @NominatimRetrofit
    fun provideNominatimRetrofit(hiltRetrofitBuilder: HiltRetrofitBuilder): Retrofit {
        return hiltRetrofitBuilder.createNominatimRetrofit()
    }

    @Singleton
    @Provides
    fun provideNetworkStateHolder(): NetworkStateHolder {
        return NetworkStateHolderImpl()
    }

    @Singleton
    @Provides
    fun provideNetworkStatusCoordinator(
        coroutineScope: CoroutineScope,
        statusStateHolder: StatusStateHolder,
        networkStateHolder: NetworkStateHolder,
        connectivityObserver: ConnectivityObserver,
    ): NetworkStatusCoordinator {
        return NetworkStatusCoordinator(
            coroutineScope,
            statusStateHolder,
            networkStateHolder,
            connectivityObserver
        )
    }

    @Singleton
    @Provides
    fun provideResponseProcessor(): ResponseProcessor {
        return ResponseProcessor()
    }

    @Singleton
    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
