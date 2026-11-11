package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.github.vladchenko.weatherforecast.core.geolocation.GeoLocationEventBus
import io.github.vladchenko.weatherforecast.core.navigation.api.NavigationEventBus
import io.github.vladchenko.weatherforecast.core.presentation.mapper.ForecastErrorToMessageIdMapper
import io.github.vladchenko.weatherforecast.core.ui.dialog.WeatherDialogController
import io.github.vladchenko.weatherforecast.core.ui.event.CityErrorEventBus
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.CityErrorCoordinator
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.domaintouimapper.WeatherDomainToUiMapper
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.domaintouimapper.WeatherDomainToUiMapperImpl
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.outputmapper.WeatherOutputMapper
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.outputmapper.WeatherOutputMapperImpl
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.WeatherResponseHandler

/**
 * Hilt module for providing presentation-layer dependencies for the Current Weather feature.
 *
 * Defines how UI components such as mappers, [WeatherResponseHandler],
 * and [CityErrorCoordinator] are created and injected.
 * All bindings are tied to [ActivityRetainedComponent], ensuring instances
 * live as long as the owning Activity/ViewModel and are destroyed together with it.
 */
@Module
@InstallIn(ActivityRetainedComponent::class)
class CurrentWeatherPresentationModule {

    @Provides
    @ActivityRetainedScoped
    fun provideWeatherForecastUiConverter(): WeatherDomainToUiMapper {
        return WeatherDomainToUiMapperImpl()
    }

    @Provides
    @ActivityRetainedScoped
    fun provideWeatherResponseHandler(
        loggingService: LoggingService,
        cityErrorEventBus: CityErrorEventBus,
        statusStateHolder: StatusStateHolder,
        errorToMessageMapper: ForecastErrorToMessageIdMapper,
    ): WeatherResponseHandler {
        return WeatherResponseHandler(
            loggingService,
            cityErrorEventBus,
            statusStateHolder,
            errorToMessageMapper
        )
    }
    
    @Provides
    @ActivityRetainedScoped
    fun provideWeatherOutputMapper(
        weatherResponseHandler: WeatherResponseHandler,
        weatherDomainToUiMapper: WeatherDomainToUiMapper,
    ): WeatherOutputMapper {
        return WeatherOutputMapperImpl(
            weatherResponseHandler,
            weatherDomainToUiMapper
        )
    }

    @Provides
    @ActivityRetainedScoped
    fun provideCityErrorCoordinator(
        statusStateHolder: StatusStateHolder,
        cityErrorEventBus: CityErrorEventBus,
        navigationEventBus: NavigationEventBus,
        geoLocationEventBus: GeoLocationEventBus,
        dialogController: WeatherDialogController,
    ): CityErrorCoordinator {
        return CityErrorCoordinator(
            statusStateHolder,
            cityErrorEventBus,
            navigationEventBus,
            geoLocationEventBus,
            dialogController
        )
    }
}