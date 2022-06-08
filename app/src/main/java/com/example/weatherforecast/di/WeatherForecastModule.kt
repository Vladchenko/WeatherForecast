package com.example.weatherforecast.di

import android.app.Application
import com.example.weatherforecast.BuildConfig
import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.converter.ResponseToResourceConverter
import com.example.weatherforecast.data.repository.WeatherForecastRepositoryImpl
import com.example.weatherforecast.data.repository.datasourceimpl.WeatherForecastDataSourceImpl
import com.example.weatherforecast.domain.WeatherForecastInteractor
import com.example.weatherforecast.domain.WeatherForecastRepository
import com.example.weatherforecast.presentation.viewmodel.WeatherForecastViewModelFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
/**
 * Dependency injection (Dagger) module.
 */
class WeatherForecastModule {

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
    fun provideWeatherForecastApiService(retrofit: Retrofit): WeatherForecastApiService {
        return retrofit.create(WeatherForecastApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideWeatherForecastRemoteDataSource(weatherForecastApiService: WeatherForecastApiService): WeatherForecastDataSourceImpl {
        return WeatherForecastDataSourceImpl(weatherForecastApiService)
    }

    @Singleton
    @Provides
    fun provideConverter(): ResponseToResourceConverter {
        return ResponseToResourceConverter()
    }

    @Singleton
    @Provides
    fun provideWeatherForecastRepository(
        weatherForecastDataSourceImpl: WeatherForecastDataSourceImpl,
        converter: ResponseToResourceConverter
    ): WeatherForecastRepository {
        return WeatherForecastRepositoryImpl(weatherForecastDataSourceImpl, converter)
    }

    @Singleton
    @Provides
    fun provideWeatherForecastInteractor(weatherForecastRepository: WeatherForecastRepository): WeatherForecastInteractor {
        return WeatherForecastInteractor(weatherForecastRepository)
    }

    @Singleton
    @Provides
    fun provideViewModelFactory(
        app: Application,
        weatherForecastInteractor: WeatherForecastInteractor
    ): WeatherForecastViewModelFactory {
        return WeatherForecastViewModelFactory(
            app,
            weatherForecastInteractor
        )
    }
}