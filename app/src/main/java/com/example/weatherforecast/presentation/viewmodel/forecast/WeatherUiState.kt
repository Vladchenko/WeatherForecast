package com.example.weatherforecast.presentation.viewmodel.forecast

import com.example.weatherforecast.models.presentation.CurrentWeatherUi

/**
 * Sealed class representing the different UI states for the weather forecast screen.
 *
 * Used in the ViewModel to communicate the state of data loading to the UI.
 * Possible states include:
 * - [Loading]: Indicates that data is being fetched.
 * - [Success]: Emitted when data is successfully loaded, containing the [CurrentWeatherUi] data
 *   and the source it came from ([DataSource.REMOTE] or [DataSource.LOCAL]).
 * - [Error]: Emitted when an error occurs during data fetching, with a user-readable error message.
 *
 * This sealed class ensures exhaustive handling of UI states in the UI layer (e.g., using `when` expressions).
 */
sealed class WeatherUiState {
    /**
     * Represents the state when the weather data is being loaded.
     *
     * The UI should display a loading indicator while in this state.
     */
    object Loading : WeatherUiState()

    /**
     * Represents the successful retrieval of weather forecast data.
     *
     * @property forecast The retrieved weather data model for display.
     * @property source Indicates whether the data came from the network or local cache.
     */
    data class Success(
        val forecast: CurrentWeatherUi,
        val source: DataSource
    ) : WeatherUiState()

    /**
     * Represents an error state during weather data retrieval.
     *
     * @property city The name of the city for which the request failed (may be null).
     * @property message A user-friendly error message suitable for display in the UI.
     */
    data class Error(
        val city: String?,
        val message: String
    ) : WeatherUiState()
}

/**
 * Enum indicating the data source from which the weather forecast was retrieved.
 *
 * Used to distinguish between remote (network) and local (cached) data,
 * allowing the UI or business logic to react accordingly (e.g., show a "fresh data" hint).
 */
enum class DataSource {
    /**
     * Data was fetched from the remote server (API).
     *
     * Indicates fresh data retrieved over the network.
     */
    REMOTE,

    /**
     * Data was retrieved from local cache/storage.
     *
     * Used when offline or when showing cached data while refreshing in the background.
     */
    LOCAL
}