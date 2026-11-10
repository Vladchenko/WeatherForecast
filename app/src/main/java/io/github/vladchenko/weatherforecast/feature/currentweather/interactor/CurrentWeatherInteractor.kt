package io.github.vladchenko.weatherforecast.feature.currentweather.interactor

import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.domain.model.TemperatureUnit
import io.github.vladchenko.weatherforecast.feature.currentweather.domain.CurrentWeatherRepository
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models.CurrentWeather

/**
 * Interactor responsible for processing business logic related to current weather data.
 *
 * Acts as an intermediary between the presentation layer (ViewModel) and the repository layer,
 * encapsulating the current weather loading scenario for a specific location.
 *
 * @property currentWeatherRepository repository responsible for providing weather data
 *                                   from remote or local sources
 */
class CurrentWeatherInteractor(
    private val currentWeatherRepository: CurrentWeatherRepository
) {

    /**
     * Loads current weather for the given geographic location.
     *
     * This method initiates weather data update from the repository, which can fetch data
     * from the network or fall back to cached data depending on implementation and availability.
     *
     * @param city city name for which weather is requested
     * @param latitude geographic latitude of the location
     * @param longitude geographic longitude of the location
     * @param temperatureUnit temperature unit (e.g., Celsius, Fahrenheit)
     * @return [LoadResult] wrapping the operation result:
     *         - [LoadResult.Remote] with [CurrentWeather] data on success
     *         - [LoadResult.Local] with cached data on remote request failure
     *         - [LoadResult.Error] with error description on failure
     */
    suspend fun loadWeatherForLocation(
        city: String,
        latitude: Double,
        longitude: Double,
        temperatureUnit: TemperatureUnit
    ): LoadResult<CurrentWeather> {
        return currentWeatherRepository.refreshWeatherForLocation(
            city,
            latitude,
            longitude,
            temperatureUnit
        )
    }
}