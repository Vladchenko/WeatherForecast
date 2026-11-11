package io.github.vladchenko.weatherforecast.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.core.data.mapper.DataErrorToForecastErrorMapper
import io.github.vladchenko.weatherforecast.core.geolocation.GeoLocationEventBus
import io.github.vladchenko.weatherforecast.core.geolocation.GeoLocationEventBusImpl
import io.github.vladchenko.weatherforecast.core.navigation.api.NavigationEventBus
import io.github.vladchenko.weatherforecast.core.navigation.impl.NavigationEventBusImpl
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.core.presentation.mapper.ForecastErrorToMessageIdMapper
import io.github.vladchenko.weatherforecast.core.ui.dialog.AlertDialogFactory
import io.github.vladchenko.weatherforecast.core.ui.dialog.AlertDialogHelper
import io.github.vladchenko.weatherforecast.core.ui.dialog.LocationDialogFactory
import io.github.vladchenko.weatherforecast.core.ui.dialog.WeatherDialogController
import io.github.vladchenko.weatherforecast.core.ui.dialog.WeatherDialogControllerImpl
import io.github.vladchenko.weatherforecast.core.ui.dialog.WeatherDialogFactory
import io.github.vladchenko.weatherforecast.core.ui.event.CityErrorEventBus
import io.github.vladchenko.weatherforecast.core.ui.event.CityErrorEventBusImpl
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolderImpl
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchersImpl
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Hilt module for providing core application-wide dependencies.
 *
 * Defines how components such as [CoroutineDispatchers], [CoroutineScope],
 * [PreferencesManager], [LoggingService], and various EventBus implementations
 * are created and injected. All bindings are tied to [SingletonComponent],
 * ensuring single instances throughout the app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class CoreModule {

    @Singleton
    @Provides
    fun provideCoroutineDispatchers(): CoroutineDispatchers {
        return CoroutineDispatchersImpl()
    }

    @Singleton
    @Provides
    fun provideApplicationCoroutineScope(coroutineDispatchers: CoroutineDispatchers): CoroutineScope {
        return CoroutineScope(SupervisorJob() + coroutineDispatchers.default)
    }

    @Singleton
    @Provides
    fun providePreferencesManager(coroutineScope: CoroutineScope,
                                  @ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context, coroutineScope)
    }

    @Singleton
    @Provides
    fun provideLoggingService(): LoggingService {
        return LoggingService()
    }

    @Provides
    @Singleton
    fun provideAlertDialogFactory(): AlertDialogFactory = AlertDialogFactory()

    @Provides
    @Singleton
    fun provideAlertDialogHelper(
        @ApplicationContext context: Context
    ): AlertDialogHelper = AlertDialogHelper(context)

    @Singleton
    @Provides
    fun provideDataErrorToForecastErrorMapper(): DataErrorToForecastErrorMapper =
        DataErrorToForecastErrorMapper()

    @Singleton
    @Provides
    fun provideForecastErrorToMessageIdMapper(): ForecastErrorToMessageIdMapper =
        ForecastErrorToMessageIdMapper()

    @Provides
    @Singleton
    fun provideLocationDialogFactory(
        alertDialogFactory: AlertDialogFactory,
    ): LocationDialogFactory = LocationDialogFactory(alertDialogFactory)

    @Provides
    @Singleton
    fun provideWeatherDialogFactory(
        baseDialogFactory: AlertDialogFactory,
        locationDialogFactory: LocationDialogFactory,
    ): WeatherDialogFactory = WeatherDialogFactory(
        baseDialogFactory,
        locationDialogFactory
    )

    @Provides
    @Singleton
    fun provideWeatherDialogController(
        dialogFactory: WeatherDialogFactory,
        dialogHelper: AlertDialogHelper
    ): WeatherDialogController = WeatherDialogControllerImpl(dialogFactory, dialogHelper)

    @Singleton
    @Provides
    fun provideWeatherStateHolder(): StatusStateHolder = StatusStateHolderImpl()

    @Singleton
    @Provides
    fun provideGeoLocationStateHolder(): GeoLocationEventBus {
        return GeoLocationEventBusImpl()
    }

    @Singleton
    @Provides
    fun provideCityErrorEventBus(): CityErrorEventBus {
        return CityErrorEventBusImpl()
    }

    @Singleton
    @Provides
    fun provideNavigationEventBus(): NavigationEventBus {
        return NavigationEventBusImpl()
    }
}