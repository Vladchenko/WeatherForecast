package com.example.weatherforecast.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.weatherforecast.data.database.CitiesNamesDAO
import com.example.weatherforecast.data.database.WeatherForecastDAO
import com.example.weatherforecast.data.database.WeatherForecastDataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PersistenceModule {

    @Singleton
    @Provides
    fun provideWeatherForecastDataBase(app: Application): WeatherForecastDataBase {
        return WeatherForecastDataBase.getInstance(app.applicationContext)
    }

    @Singleton
    @Provides
    fun provideWeatherForecastDAO(database: WeatherForecastDataBase): WeatherForecastDAO {
        return database.getWeatherForecastInstance()
    }

    @Singleton
    @Provides
    fun provideCitiesNamesDAO(database: WeatherForecastDataBase): CitiesNamesDAO {
        return database.getCitiesNamesInstance()
    }

    @Provides
    @Singleton
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences? {
        return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREF_FILE_NAME = "Some file"
    }
}