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
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CityErrorEvent.CityNotFound

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
                val error = loadResult.error
                val messageId = defineErrorMessage(error)

                if (error is ForecastError.CityNotFound) {
                    statusStateHolder.updateWarningStatus(messageId, error.city)
                    cityErrorEventBus.send(CityNotFound(error.city))
                } else {
                    statusStateHolder.updateErrorStatus(messageId, cityModel.city)
                }

                return WeatherResponseHandlerResult(errorToShow = messageId)
            }
        }
    }

    private fun defineErrorMessage(error: ForecastError): Int = when (error) {
        is ForecastError.NetworkError -> mapNetworkErrorToMessageId(error)
        is ForecastError.NetworkAndCacheError -> R.string.error_network_and_cache_unavailable
        is ForecastError.ServerError -> R.string.server_error
        is ForecastError.ServerAndCacheError -> R.string.error_server_and_cache_unavailable
        is ForecastError.AccessDeniedAndCacheError -> R.string.error_access_denied_and_cache_unavailable
        is ForecastError.AccessDenied -> R.string.error_access_denied
        is ForecastError.RateLimitExceeded -> R.string.error_rate_limit
        is ForecastError.LocalStorageError -> R.string.error_database
        is ForecastError.LocalDataCorrupted -> R.string.local_data_corrupted
        is ForecastError.NoDataAvailable -> R.string.no_weather_data_available
        is ForecastError.ResourceNotFound -> R.string.error_resource_not_found
        is ForecastError.DataParsingError -> R.string.error_parsing
        is ForecastError.UncategorizedError -> R.string.error_unknown
        is ForecastError.ApiKeyInvalid -> R.string.api_key_invalid
        is ForecastError.CityNotFound -> R.string.city_not_found
    }

    private fun mapNetworkErrorToMessageId(error: ForecastError.NetworkError): Int = when (error.type) {
        is ForecastError.NetworkError.Type.ConnectionFailed -> R.string.connection_refused
        is ForecastError.NetworkError.Type.NoInternet -> R.string.network_disconnected
        is ForecastError.NetworkError.Type.HostUnreachable -> R.string.error_host_unreachable
        is ForecastError.NetworkError.Type.Timeout -> R.string.request_timeout
        is ForecastError.NetworkError.Type.SecurityError -> R.string.ssl_error
        is ForecastError.NetworkError.Type.Other -> R.string.network_error_generic
    }

    companion object {
        private const val TAG = "WeatherResponseHandler"
    }
}