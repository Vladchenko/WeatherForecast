package com.example.weatherforecast.presentation.viewmodel.forecast

import com.example.weatherforecast.models.presentation.WeatherForecastUi

/**
 * Sealed class representing the different UI states for the weather forecast screen.
 *
 * Used in the ViewModel to communicate the state of data loading to the UI.
 * Possible states include:
 * - [Loading]: Indicates that data is being fetched.
 * - [Success]: Emitted when data is successfully loaded, containing the [WeatherForecastUi] data
 *   and the source it came from ([DataSource.REMOTE] or [DataSource.LOCAL]).
 * - [Error]: Emitted when an error occurs during data fetching, with a user-readable error message.
 *
 * This sealed class ensures exhaustive handling of UI states in the UI layer (e.g., using `when` expressions).
 */
sealed class ForecastUiState {
    object Loading: ForecastUiState()
    data class Success(val forecast: WeatherForecastUi,
                       val source: DataSource): ForecastUiState()
    data class Error(val message: String): ForecastUiState()
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
     */
    REMOTE,

    /**
     * Data was retrieved from local cache/storage.
     */
    LOCAL
}