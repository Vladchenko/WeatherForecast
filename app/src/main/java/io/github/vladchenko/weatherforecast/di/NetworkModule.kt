package io.github.vladchenko.weatherforecast.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.BuildConfig
import io.github.vladchenko.weatherforecast.data.api.WeatherApiService
import io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasource.remote.CityApiService
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
    fun provideWeatherForecastApiService(@Named(WEATHER) retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideCityApiService(@Named(WEATHER) retrofit: Retrofit): CityApiService {
        return retrofit.create(CityApiService::class.java)
    }
    
    companion object {
        /**
         * Named binding for the Retrofit instance used with OpenWeatherMap API.
         *
         * Used to provide [WeatherApiService] and [CityApiService].
         */
        const val WEATHER = "WeatherRetrofit"
    }
}