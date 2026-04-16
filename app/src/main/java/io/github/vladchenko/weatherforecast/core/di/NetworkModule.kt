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
import io.github.vladchenko.weatherforecast.core.network.api.ApiConstants.DEVELOPER_EMAIL
import io.github.vladchenko.weatherforecast.core.network.api.ApiConstants.NOMINATIM
import io.github.vladchenko.weatherforecast.core.network.api.ApiConstants.NOMINATIM_BASE_URL
import io.github.vladchenko.weatherforecast.core.network.api.ApiConstants.USER_AGENT
import io.github.vladchenko.weatherforecast.core.location.geolocation.api.NominatimApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * TODO
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

    @Module
    @InstallIn(SingletonComponent::class)
    object NetworkModule {

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
}