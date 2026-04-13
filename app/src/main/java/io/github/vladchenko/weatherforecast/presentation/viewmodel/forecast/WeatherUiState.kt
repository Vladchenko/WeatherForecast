package io.github.vladchenko.weatherforecast.presentation.viewmodel.forecast

import io.github.vladchenko.weatherforecast.models.presentation.CurrentWeatherUi

/**
 * Sealed interface representing the different UI states for weather data loading.
 *
 * Used by ViewModels to expose the state of asynchronous operations (e.g., fetching hourly or current weather)
 * to the UI in a unidirectional and reactive way via [StateFlow]. Ensures that all possible states are handled
 * exhaustively in the UI layer using `when` expressions.
 *
 * @param T The type of data held in the [Success] state (e.g., [CurrentWeatherUi], [HourlyWeatherDomainModel]).
 *
 * Possible states:
 * - [Loading]: Data is currently being fetched from a data source.
 * - [Success]: Data has been successfully retrieved, along with the origin ([DataSource]).
 * - [Error]: An error occurred during the request; contains debug-friendly info and a displayable message.
 */
sealed interface WeatherUiState<out T> {
    /**
     * Represents the state when weather data is actively being loaded from a repository.
     *
     * This state is typically used to trigger a loading spinner or placeholder content in the UI.
     * It carries no data since nothing has been loaded yet.
     */
    object Loading : WeatherUiState<Nothing>

    /**
     * Represents a successful result from a weather data request.
     *
     * @property data The resulting weather data model intended for UI rendering.
     * @property source Indicates whether the data was obtained from the network ([DataSource.REMOTE])
     *                  or local storage ([DataSource.LOCAL]). Can be used to show freshness indicators.
     */
    data class Success<T>(
        val data: T,
        val source: DataSource
    ) : WeatherUiState<T>

    /**
     * Represents a failure during weather data retrieval.
     *
     * @property city The name of the city associated with the failed request (can be `null`, e.g., for location-based requests).
     * @property message A human-readable error message suitable for display in the UI (e.g., "City not found", "No internet connection").
     */
    data class Error(
        val city: String?,
        val message: String
    ) : WeatherUiState<Nothing>
}

/**
 * Enumerates the possible origins of weather forecast data.
 *
 * Helps distinguish between fresh data from the network and cached data,
 * allowing the app to make decisions about UI hints, background sync, etc.
 */
enum class DataSource {
    /**
     * Data was retrieved from a remote API over the network.
     *
     * Indicates up-to-date information, possibly triggering cache updates.
     */
    REMOTE,

    /**
     * Data was served from local persistence (e.g., Room database, SharedPreferences).
     *
     * Used when there's no network connection or to show immediate results while refreshing in the background.
     */
    LOCAL
}