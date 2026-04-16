package io.github.vladchenko.weatherforecast.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.location.geolocation.DeviceLocationProvider
import io.github.vladchenko.weatherforecast.core.location.geolocation.geolocator.Geolocator
import io.github.vladchenko.weatherforecast.core.location.permission.PermissionChecker
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.data.util.ResponseProcessor
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityInteractor
import io.github.vladchenko.weatherforecast.feature.geolocation.presentation.viewmodel.GeoLocationViewModelFactory
import io.github.vladchenko.weatherforecast.presentation.converter.appbar.AppBarStateConverter
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModelFactory
import javax.inject.Singleton

/**
 * TODO
 */
@Module
@InstallIn(SingletonComponent::class)
class WeatherForecastModule {

    @Singleton
    @Provides
    fun provideResponseProcessor(): ResponseProcessor {
        return ResponseProcessor()
    }

    @Singleton
    @Provides
    fun provideGeoLocationViewModelFactory(
        geoLocationHelper: Geolocator,
        loggingService: LoggingService,
        statusRenderer: StatusRenderer,
        resourceManager: ResourceManager,
        geoLocator: DeviceLocationProvider,
        permissionChecker: PermissionChecker,
        connectivityObserver: ConnectivityObserver,
        chosenCityInteractor: ChosenCityInteractor,
        coroutineDispatchers: CoroutineDispatchers,
    ): GeoLocationViewModelFactory {
        return GeoLocationViewModelFactory(
            geoLocationHelper,
            loggingService,
            statusRenderer,
            resourceManager,
            geoLocator,
            permissionChecker,
            connectivityObserver,
            chosenCityInteractor,
            coroutineDispatchers
        )
    }

    @Singleton
    @Provides
    fun provideAppBarStateConverter(resourceManager: ResourceManager): AppBarStateConverter {
        return AppBarStateConverter(resourceManager)
    }

    @Singleton
    @Provides
    fun provideAppBarViewModelFactory(
        statusRenderer: StatusRenderer,
        resourceManager: ResourceManager,
        appBarStateConverter: AppBarStateConverter
    ): AppBarViewModelFactory {
        return AppBarViewModelFactory(
            statusRenderer,
            resourceManager,
            appBarStateConverter
        )
    }
}