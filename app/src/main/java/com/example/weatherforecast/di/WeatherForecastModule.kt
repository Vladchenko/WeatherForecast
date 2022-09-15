package com.example.weatherforecast.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.Nullable
import androidx.room.Room
import com.example.weatherforecast.BuildConfig
import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.api.customexceptions.ErrorsCallAdapterFactory
import com.example.weatherforecast.data.converter.CitiesNamesDataToDomainConverter
import com.example.weatherforecast.data.converter.ForecastDataToDomainModelsConverter
import com.example.weatherforecast.data.database.CitiesNamesDAO
import com.example.weatherforecast.data.database.WeatherForecastDAO
import com.example.weatherforecast.data.database.WeatherForecastDataBase
import com.example.weatherforecast.data.repository.ChosenCityRepositoryImpl
import com.example.weatherforecast.data.repository.CitiesNamesRepositoryImpl
import com.example.weatherforecast.data.repository.WeatherForecastRepositoryImpl
import com.example.weatherforecast.data.repository.datasource.*
import com.example.weatherforecast.data.repository.datasourceimpl.*
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor
import com.example.weatherforecast.domain.citiesnames.CitiesNamesRepository
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.city.ChosenCityRepository
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRepository
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator
import com.example.weatherforecast.presentation.PresentationUtils
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModelFactory
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModelFactory
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
    fun provideWeatherForecastLocalDataSource(forecastDAO: WeatherForecastDAO): WeatherForecastLocalDataSource {
        return WeatherForecastLocalDataSourceImpl(forecastDAO)
    }

    @Singleton
    @Provides
    fun provideWeatherForecastRemoteDataSource(weatherForecastApiService: WeatherForecastApiService): WeatherForecastRemoteDataSource {
        return WeatherForecastRemoteDataSourceImpl(weatherForecastApiService)
    }

    @Singleton
    @Provides
    fun provideCityDataSource(app: Application): ChosenCityDataSource {
        return ChosenCityLocalDataSourceImpl(
            app.getSharedPreferences(PresentationUtils.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
        )
    }

    @Singleton
    @Provides
    fun provideArticlesDataBase(app: Application): WeatherForecastDataBase {
        return Room
            .databaseBuilder(app, WeatherForecastDataBase::class.java, "WeatherForecastDataBase")
            .fallbackToDestructiveMigration()   // For migration from old database to a new one
            .build()
    }

    @Singleton
    @Provides
    fun provideWeatherForecastDAO(database: WeatherForecastDataBase): WeatherForecastDAO {
        return database.getWeatherForecastInstance()
    }

    @Singleton
    @Provides
    fun provideConverter(): ForecastDataToDomainModelsConverter {
        return ForecastDataToDomainModelsConverter()
    }

    @Singleton
    @Provides
    fun provideWeatherForecastRepository(
        weatherForecastRemoteDataSource: WeatherForecastRemoteDataSource,
        weatherForecastLocalDataSource: WeatherForecastLocalDataSource,
        converter: ForecastDataToDomainModelsConverter
    ): WeatherForecastRepository {
        return WeatherForecastRepositoryImpl(
            weatherForecastRemoteDataSource,
            weatherForecastLocalDataSource,
            converter
        )
    }

    @Singleton
    @Provides
    fun provideCityRepository(chosenCityDataSource: ChosenCityDataSource): ChosenCityRepository {
        return ChosenCityRepositoryImpl(chosenCityDataSource)
    }

    @Singleton
    @Provides
    fun provideWeatherForecastRemoteInteractor(weatherForecastRepository: WeatherForecastRepository): WeatherForecastRemoteInteractor {
        return WeatherForecastRemoteInteractor(weatherForecastRepository)
    }

    @Singleton
    @Provides
    fun provideWeatherForecastLocalInteractor(weatherForecastRepository: WeatherForecastRepository): WeatherForecastLocalInteractor {
        return WeatherForecastLocalInteractor(weatherForecastRepository)
    }

    @Singleton
    @Provides
    fun provideCityInteractor(chosenCityRepository: ChosenCityRepository): ChosenCityInteractor {
        return ChosenCityInteractor(chosenCityRepository)
    }

    @Singleton
    @Provides
    fun provideWeatherForecastGeoLocator(@ApplicationContext context: Context): WeatherForecastGeoLocator {
        return WeatherForecastGeoLocator(context)
    }

    @Singleton
    @Provides
    fun provideViewModelFactory(
        app: Application,
        geoLocator: WeatherForecastGeoLocator,
        chosenCityInteractor: ChosenCityInteractor,
        weatherForecastLocalInteractor: WeatherForecastLocalInteractor,
        weatherForecastRemoteInteractor: WeatherForecastRemoteInteractor
    ): WeatherForecastViewModelFactory {
        return WeatherForecastViewModelFactory(
            app,
            geoLocator,
            chosenCityInteractor,
            weatherForecastLocalInteractor,
            weatherForecastRemoteInteractor
        )
    }

    @Singleton
    @Provides
    fun provideCitiesNamesDAO(database: WeatherForecastDataBase): CitiesNamesDAO {
        return database.getCitiesNamesInstance()
    }

    @Singleton
    @Provides
    fun provideCitiesNamesLocalDataSource(dao: CitiesNamesDAO): CitiesNamesLocalDataSource {
        return CitiesNamesLocalDataSourceImpl(dao)
    }

    @Singleton
    @Provides
    fun provideCitiesNamesRemoteDataSource(weatherForecastApiService: WeatherForecastApiService): CitiesNamesRemoteDataSource {
        return CitiesNamesRemoteDataSourceImpl(weatherForecastApiService)
    }

    @Singleton
    @Provides
    fun provideCitiesNamesConverter(): CitiesNamesDataToDomainConverter {
        return CitiesNamesDataToDomainConverter()
    }

    @Singleton
    @Provides
    fun provideCitiesNamesRepository(
        converter: CitiesNamesDataToDomainConverter,
        citiesNamesLocalDataSource: CitiesNamesLocalDataSource,
        citiesNamesRemoteDataSource: CitiesNamesRemoteDataSource
    ): CitiesNamesRepository {
        return CitiesNamesRepositoryImpl(
            citiesNamesLocalDataSource,
            citiesNamesRemoteDataSource,
            converter
        )
    }

    @Singleton
    @Provides
    fun provideCitiesNamesInteractor(citiesNamesRepository: CitiesNamesRepository): CitiesNamesInteractor {
        return CitiesNamesInteractor(citiesNamesRepository)
    }

    @Singleton
    @Provides
    fun provideCitiesNamesViewModelFactory(
        app: Application,
        citiesNamesInteractor: CitiesNamesInteractor
    ): CitiesNamesViewModelFactory {
        return CitiesNamesViewModelFactory(
            app,
            citiesNamesInteractor
        )
    }

    @Provides
    @Singleton
    @Nullable
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences? {
        return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREF_FILE_NAME = "Some file"
    }
}