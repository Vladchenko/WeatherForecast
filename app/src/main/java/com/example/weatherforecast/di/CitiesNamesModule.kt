package com.example.weatherforecast.di

import android.app.Application
import android.content.Context
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.converter.CitiesNamesModelConverter
import com.example.weatherforecast.data.database.CitiesNamesDAO
import com.example.weatherforecast.data.repository.ChosenCityRepositoryImpl
import com.example.weatherforecast.data.repository.CitiesNamesRepositoryImpl
import com.example.weatherforecast.data.repository.datasource.ChosenCityDataSource
import com.example.weatherforecast.data.repository.datasource.CitiesNamesLocalDataSource
import com.example.weatherforecast.data.repository.datasource.CitiesNamesRemoteDataSource
import com.example.weatherforecast.data.repository.datasourceimpl.ChosenCityLocalDataSourceImpl
import com.example.weatherforecast.data.repository.datasourceimpl.CitiesNamesLocalDataSourceImpl
import com.example.weatherforecast.data.repository.datasourceimpl.CitiesNamesRemoteDataSourceImpl
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor
import com.example.weatherforecast.domain.citiesnames.CitiesNamesRepository
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.city.ChosenCityRepository
import com.example.weatherforecast.presentation.PresentationUtils
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModelFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CitiesNamesModule {

    @Singleton
    @Provides
    fun provideCityDataSource(app: Application): ChosenCityDataSource {
        return ChosenCityLocalDataSourceImpl(
            app.getSharedPreferences(PresentationUtils.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
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
    fun provideCityInteractor(chosenCityRepository: ChosenCityRepository): ChosenCityInteractor {
        return ChosenCityInteractor(chosenCityRepository)
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
    fun provideCitiesNamesConverter(): CitiesNamesModelConverter {
        return CitiesNamesModelConverter()
    }

    @Singleton
    @Provides
    fun provideCitiesNamesRepository(
        coroutineDispatchers: CoroutineDispatchers,
        converter: CitiesNamesModelConverter,
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
        connectivityObserver: ConnectivityObserver,
        coroutineDispatchers: CoroutineDispatchers,
        citiesNamesInteractor: CitiesNamesInteractor
    ): CitiesNamesViewModelFactory {
        return CitiesNamesViewModelFactory(
            connectivityObserver,
            coroutineDispatchers,
            citiesNamesInteractor
        )
    }

}