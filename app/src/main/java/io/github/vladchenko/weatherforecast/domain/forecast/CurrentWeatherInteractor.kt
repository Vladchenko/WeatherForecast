package io.github.vladchenko.weatherforecast.domain.forecast

import io.github.vladchenko.weatherforecast.data.util.TemperatureType
import io.github.vladchenko.weatherforecast.models.domain.CurrentWeather
import io.github.vladchenko.weatherforecast.models.domain.LoadResult

/**
 * Interactor responsible for handling business logic related to current weather data.
 *
 * Acts as an intermediary between the presentation layer (ViewModel) and the repository layer,
 * encapsulating the use case of loading current weather for a specific location.
 *
 * @property currentWeatherRepository The repository responsible for providing weather data
 *                                   from remote or local sources.
 */
class CurrentWeatherInteractor(private val currentWeatherRepository: CurrentWeatherRepository) {

    /**
     * Loads the current weather for a given geographic location.
     *
     * This method triggers a refresh of weather data from the repository, which may fetch from
     * the network or fall back to cached data depending on implementation and availability.
     *
     * @param city The name of the city for which weather is requested.
     * @param temperatureType The unit type for temperature (e.g., Celsius, Fahrenheit).
     * @param latitude The geographical latitude of the location.
     * @param longitude The geographical longitude of the location.
     * @return A [LoadResult] that wraps the result of the operation:
     *         - [LoadResult.Success] with [CurrentWeather] data if successful.
     *         - [LoadResult.Error] with an error description if the operation failed.
     *
     * @see CurrentWeatherRepository.refreshWeatherForLocation
     */
    suspend fun loadWeatherForLocation(
        city: String,
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<CurrentWeather> {
        return currentWeatherRepository.refreshWeatherForLocation(
            city,
            temperatureType,
            latitude,
            longitude
        )
    }
}