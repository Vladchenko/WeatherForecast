package com.example.weatherforecast.di

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.weatherforecast.BuildConfig
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.connectivity.ConnectivityObserverImpl
import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.api.customexceptions.ErrorsCallAdapterFactory
import com.example.weatherforecast.data.workmanager.ForecastWorker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

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
            .addCallAdapterFactory(ErrorsCallAdapterFactory())
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
    fun provideWorkManager(@ApplicationContext applicationContext: Context): WorkManager {
        val workManager = WorkManager.getInstance(applicationContext)
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest =
            PeriodicWorkRequestBuilder<ForecastWorker>(30, TimeUnit.MINUTES, 25, TimeUnit.MINUTES)
                .setInitialDelay(5, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
        workManager.enqueueUniquePeriodicWork("ForecastPeriodicWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest)
        return workManager
    }

    @Singleton
    @Provides
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver {
        return ConnectivityObserverImpl(context)
    }
}