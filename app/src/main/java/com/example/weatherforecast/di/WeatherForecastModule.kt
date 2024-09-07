package com.example.weatherforecast.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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
import com.example.weatherforecast.data.repository.datasource.ChosenCityDataSource
import com.example.weatherforecast.data.repository.datasource.CitiesNamesLocalDataSource
import com.example.weatherforecast.data.repository.datasource.CitiesNamesRemoteDataSource
import com.example.weatherforecast.data.repository.datasource.WeatherForecastLocalDataSource
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.data.repository.datasourceimpl.ChosenCityLocalDataSourceImpl
import com.example.weatherforecast.data.repository.datasourceimpl.CitiesNamesLocalDataSourceImpl
import com.example.weatherforecast.data.repository.datasourceimpl.CitiesNamesRemoteDataSourceImpl
import com.example.weatherforecast.data.repository.datasourceimpl.WeatherForecastLocalDataSourceImpl
import com.example.weatherforecast.data.repository.datasourceimpl.WeatherForecastRemoteDataSourceImpl
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.data.workmanager.ForecastWorker
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.dispatchers.CoroutineDispatchersImpl
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor
import com.example.weatherforecast.domain.citiesnames.CitiesNamesRepository
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.city.ChosenCityRepository
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRepository
import com.example.weatherforecast.geolocation.Geolocator
import com.example.weatherforecast.geolocation.GeolocatorImpl
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator
import com.example.weatherforecast.presentation.PresentationUtils
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModelFactory
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModelFactory
import com.example.weatherforecast.presentation.viewmodel.geolocation.GeoLocationViewModelFactory
import com.example.weatherforecast.presentation.viewmodel.persistence.PersistenceViewModelFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Dependency injection (Dagger) module.
 */
@Module
@InstallIn(SingletonComponent::class)
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
    fun provideConverter(): ForecastDataToDomainModelsConverter {
        return ForecastDataToDomainModelsConverter()
    }

    @Singleton
    @Provides
    fun provideCoroutineDispatchers(): CoroutineDispatchers {
        return CoroutineDispatchersImpl()
    }

    @Singleton
    @Provides
    fun provideWeatherForecastRepository(
        weatherForecastRemoteDataSource: WeatherForecastRemoteDataSource,
        weatherForecastLocalDataSource: WeatherForecastLocalDataSource,
        converter: ForecastDataToDomainModelsConverter,
        coroutineDispatchers: CoroutineDispatchers,
        temperaturesType: TemperatureType
    ): WeatherForecastRepository {
        return WeatherForecastRepositoryImpl(
            weatherForecastRemoteDataSource,
            weatherForecastLocalDataSource,
            converter,
            coroutineDispatchers,
            temperaturesType
        )
    }

    @Singleton
    @Provides
    fun provideCityRepository(
        chosenCityDataSource: ChosenCityDataSource,
        coroutineDispatchers: CoroutineDispatchers
    ): ChosenCityRepository {
        return ChosenCityRepositoryImpl(coroutineDispatchers, chosenCityDataSource,)
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
    fun provideForecastViewModelFactory(
        temperatureType: TemperatureType,
        chosenCityInteractor: ChosenCityInteractor,
        coroutineDispatchers: CoroutineDispatchers,
        weatherForecastRemoteInteractor: WeatherForecastRemoteInteractor
    ): WeatherForecastViewModelFactory {
        return WeatherForecastViewModelFactory(
            temperatureType,
            chosenCityInteractor,
            coroutineDispatchers,
            weatherForecastRemoteInteractor
        )
    }

    @Singleton
    @Provides
    fun providePersistenceViewModelFactory(
        chosenCityInteractor: ChosenCityInteractor,
        coroutineDispatchers: CoroutineDispatchers,
        weatherForecastLocalInteractor: WeatherForecastLocalInteractor,
    ): PersistenceViewModelFactory {
        return PersistenceViewModelFactory(
            chosenCityInteractor,
            coroutineDispatchers,
            weatherForecastLocalInteractor,
        )
    }

    @Singleton
    @Provides
    fun provideGeoLocationViewModelFactory(
        app: Application,
        geoLocationHelper: Geolocator,
        geoLocator: WeatherForecastGeoLocator,
        chosenCityInteractor: ChosenCityInteractor,
        coroutineDispatchers: CoroutineDispatchers,
    ): GeoLocationViewModelFactory {
        return GeoLocationViewModelFactory(
            app,
            geoLocationHelper,
            geoLocator,
            chosenCityInteractor,
            coroutineDispatchers
        )
    }

    @Singleton
    @Provides
    fun provideGeolocator(
        @ApplicationContext context: Context,
        coroutineDispatchers: CoroutineDispatchers
    ): Geolocator {
        return GeolocatorImpl(context, coroutineDispatchers)
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
        coroutineDispatchers: CoroutineDispatchers,
        converter: CitiesNamesDataToDomainConverter,
        citiesNamesLocalDataSource: CitiesNamesLocalDataSource,
        citiesNamesRemoteDataSource: CitiesNamesRemoteDataSource
    ): CitiesNamesRepository {
        return CitiesNamesRepositoryImpl(
            coroutineDispatchers,
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
        coroutineDispatchers: CoroutineDispatchers,
        citiesNamesInteractor: CitiesNamesInteractor
    ): CitiesNamesViewModelFactory {
        return CitiesNamesViewModelFactory(
            coroutineDispatchers,
            citiesNamesInteractor
        )
    }

    @Provides
    @Singleton
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences? {
        return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
    }

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
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest)
        return workManager
    }

    @Singleton
    @Provides
    fun provideCoroutineScope(coroutineDispatchers: CoroutineDispatchers): CoroutineScope {
        // Run this code when providing an instance of CoroutineScope
        return CoroutineScope(SupervisorJob() + coroutineDispatchers.default)
    }

    @Singleton
    @Provides
    fun provideTemperatureType(): TemperatureType {
        return TemperatureType.CELSIUS
    }

    companion object {
        private const val PREF_FILE_NAME = "Some file"
    }
}