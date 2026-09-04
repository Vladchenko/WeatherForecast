package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel

import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.ui.event.CityErrorEventBus
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models.CurrentWeather
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.models.WeatherResponseHandlerResult

/**
 * A handler responsible for processing raw weather data loading results and mapping them
 * into a UI-ready structure ([WeatherResponseHandlerResult]).
 *
 * This class encapsulates the business logic for handling various weather loading outcomes,
 * including successful remote fetches, local cache fallbacks, and diverse error scenarios
 * (API errors, network issues, etc.). It also manages necessary side effects such as
 * updating UI status messages via [StatusStateHolder] and determining the city model that
 * should be persisted as the user's "chosen city".
 *
 * @property loggingService Service for recording debug and error events.
 * @property cityErrorEventBus Unified event bus for broadcasting city-related errors.
 * @property statusStateHolder Component for broadcasting UI status updates (snackbars, dialogs, etc.).
 */
class WeatherResponseHandler(
    private val loggingService: LoggingService,
    private val cityErrorEventBus: CityErrorEventBus,
    private val statusStateHolder: StatusStateHolder,
) {

    /**
     * Processes the [loadResult] obtained from the weather data interactor and returns
     * a [WeatherResponseHandlerResult] ready for UI consumption.
     *
     * This method evaluates the type of [loadResult] (Remote, Local, Error, Loading),
     * triggers the appropriate status updates, and constructs the result object containing:
     * - Weather data to display (if successful).
     * - Error messages or events to present to the user.
     * - The city model to save to the local database to persist the user's last selection.
     *
     * @param cityModel The [CityLocationModel] representing the city for which the weather was requested.
     * @param loadResult The [LoadResult] containing the raw weather data, cached data, or error details.
     * @return A [WeatherResponseHandlerResult] containing the processed data and instructions for the ViewModel.
     */
    fun processServerResponse(
        cityModel: CityLocationModel,
        loadResult: LoadResult<CurrentWeather>
    ): WeatherResponseHandlerResult {
        when (loadResult) {
            is LoadResult.Remote -> {
                statusStateHolder.updateInfoStatus(
                    R.string.forecast_loaded_success,
                    cityModel.city
                )
                loggingService.logDebugEvent(
                    TAG,
                    "Chosen city saved to database: $cityModel.city"
                )
                return WeatherResponseHandlerResult(
                    remoteWeatherToShow = loadResult.data.copy(city = cityModel.city),
                )
            }

            is LoadResult.Local -> {
                statusStateHolder.updateWarningStatus(
                    R.string.forecast_outdated, cityModel.city
                )
                return WeatherResponseHandlerResult(
                    localWeatherToShow = loadResult.data.copy(city = cityModel.city),
                )
            }

            is LoadResult.Error -> {
                var errorMessage: Int = Integer.MIN_VALUE
                when (val error = loadResult.error) {
                    is ForecastError.ApiKeyInvalid -> {
                        errorMessage = R.string.api_key_invalid
                        statusStateHolder.updateErrorStatus(errorMessage)
                    }

                    is ForecastError.CityNotFound -> {
                        errorMessage = R.string.city_not_found
                        statusStateHolder.updateWarningStatus(errorMessage, error.city)
                        cityErrorEventBus.send(CityErrorEvent.CityNotFound(error.city))
                    }

                    is ForecastError.LocalDataCorrupted -> {
                        errorMessage = R.string.local_data_corrupted
                        statusStateHolder.updateErrorStatus(errorMessage, cityModel.city)
                    }

                    is ForecastError.NetworkError ->
                        when (error.type) {
                            ForecastError.NetworkError.Type.ConnectionFailed -> {
                                errorMessage = R.string.connection_refused
                                statusStateHolder.updateErrorStatus(errorMessage, cityModel.city)
                            }

                            ForecastError.NetworkError.Type.NoInternet -> {
                                errorMessage = R.string.network_disconnected
                                statusStateHolder.updateErrorStatus(errorMessage, cityModel.city)
                            }

                            ForecastError.NetworkError.Type.Timeout -> {
                                errorMessage = R.string.request_timeout
                                statusStateHolder.updateErrorStatus(errorMessage, cityModel.city)
                            }

                            ForecastError.NetworkError.Type.SecurityError -> {
                                errorMessage = R.string.ssl_error
                                statusStateHolder.updateErrorStatus(errorMessage, cityModel.city)
                            }

                            else -> {
                                errorMessage = R.string.network_error_generic
                                statusStateHolder.updateErrorStatus(errorMessage, cityModel.city)
                            }
                        }

                    is ForecastError.NoDataAvailable -> {
                        errorMessage = R.string.no_weather_data_available
                        statusStateHolder.updateErrorStatus(errorMessage, cityModel.city)
                    }

                    is ForecastError.UncategorizedError -> {
                        val error = error.cause ?: error.message
                        loggingService.logError(TAG, "Uncategorized error: $error")
                        statusStateHolder.updateErrorStatus(error.toString())
                    }
                }
                return WeatherResponseHandlerResult(
                    errorToShow = errorMessage,
                )
            }
        }
    }

    companion object {
        private const val TAG = "WeatherResponseHandler"
    }
}