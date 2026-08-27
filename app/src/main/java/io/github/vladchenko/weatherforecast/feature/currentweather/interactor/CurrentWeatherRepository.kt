package io.github.vladchenko.weatherforecast.feature.currentweather.interactor

import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.model.TemperatureType
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models.CurrentWeather

/**
 * Repository interface providing domain-layer access to current weather data.
 *
 * This interface defines the contract for retrieving weather forecasts
 * with built-in fallback to cached data when remote fetching fails.
 * Implementations are responsible for:
 * - Fetching fresh data from remote sources
 * - Caching results locally for offline access
 * - Handling errors and returning appropriate [LoadResult] variants
 *
 * The repository follows a "remote-first, cache-fallback" strategy:
 * 1. Attempt to load fresh data from the network
 * 2. If remote loading fails, try to load cached data
 * 3. If both fail, return an error with details
 */
interface CurrentWeatherRepository {

    /**
     * Retrieves weather data for the specified [city] and coordinates.
     *
     * 1. Fetches data from [currentWeatherRemoteDataSource].
     * 2. On success: saves the result to the local cache and returns [LoadResult.Remote].
     * 3. On failure: tries to load cached data. If available, returns [LoadResult.Local] with the original error context.
     * 4. If both fail: returns [LoadResult.Error] with the appropriate [ForecastError].
     *
     * @param city the city name to fetch weather for.
     * @param temperatureType the unit for temperature (e.g., Celsius, Fahrenheit).
     * @param latitude geographical latitude for the request.
     * @param longitude geographical longitude for the request.
     * @return [LoadResult] indicating the source of data (Remote, Local) or an error.
     */
    suspend fun refreshWeatherForLocation(
        city: String,
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<CurrentWeather>
}