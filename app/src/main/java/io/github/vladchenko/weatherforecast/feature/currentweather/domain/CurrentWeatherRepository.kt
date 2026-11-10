package io.github.vladchenko.weatherforecast.feature.currentweather.domain

import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.domain.model.TemperatureUnit
import io.github.vladchenko.weatherforecast.feature.currentweather.data.datasource.remote.CurrentWeatherRemoteDataSource
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models.CurrentWeather

/**
 * Repository interface providing access to current weather data at the domain level.
 *
 * This interface defines the contract for fetching weather forecasts
 * with a built-in fallback to use cached data on remote fetch failure.
 * Implementations are responsible for:
 * - Fetching fresh data from remote sources
 * - Caching results locally for offline access
 * - Handling errors and returning appropriate [io.github.vladchenko.weatherforecast.core.domain.model.LoadResult] variants
 *
 * The repository follows a "remote first, then cache" strategy:
 * 1. Attempt to fetch fresh data from the network
 * 2. If remote fetch fails, attempt to load cached data
 * 3. If both attempts fail, return an error with details
 */
interface CurrentWeatherRepository {

    /**
     * Fetches weather data for the specified [city] and coordinates.
     *
     * 1. Fetches data from [CurrentWeatherRemoteDataSource].
     * 2. On success: saves the result to local cache and returns [LoadResult.Remote].
     * 3. On failure: attempts to load cached data. If available, returns [LoadResult.Local] with context of the original error.
     * 4. If both attempts fail: returns [LoadResult.Error] with appropriate [ForecastError].
     *
     * @param city city name to fetch weather for
     * @param latitude geographic latitude for the request
     * @param longitude geographic longitude for the request
     * @param temperatureUnit temperature unit (e.g., Celsius, Fahrenheit)
     * @return [LoadResult] indicating data source (Remote, Local) or error
     */
    suspend fun refreshWeatherForLocation(
        city: String,
        latitude: Double,
        longitude: Double,
        temperatureUnit: TemperatureUnit
    ): LoadResult<CurrentWeather>
}