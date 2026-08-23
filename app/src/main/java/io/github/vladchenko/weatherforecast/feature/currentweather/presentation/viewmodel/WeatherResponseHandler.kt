package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel

import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.ui.status.StatusType.Error
import io.github.vladchenko.weatherforecast.core.ui.status.StatusType.Info
import io.github.vladchenko.weatherforecast.core.ui.status.StatusType.Warning
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models.CurrentWeather
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.createLocation
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
 */
class WeatherResponseHandler(
    private val loggingService: LoggingService,
    private val resourceManager: ResourceManager,
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
                statusStateHolder.updateStatus(
                    Info(
                        resourceManager.getString(
                            R.string.forecast_loaded_success,
                            cityModel.city
                        )
                    )
                )
                val cityLocationModel = CityLocationModel(
                    cityModel.city,
                    createLocation(
                        loadResult.data.coordinate.latitude,
                        loadResult.data.coordinate.longitude
                    )
                )
                loggingService.logDebugEvent(
                    TAG,
                    "Chosen city saved to database: $cityModel.city"
                )
                return WeatherResponseHandlerResult(
                    remoteWeatherToShow = loadResult.data.copy(city = cityModel.city),
                    cityModelToSave = cityLocationModel,
                )
            }

            is LoadResult.Local -> {
                statusStateHolder.updateStatus(
                    Warning(
                        resourceManager.getString(
                            R.string.forecast_outdated, cityModel.city
                        )
                    )
                )
                val cityLocationModel = CityLocationModel(
                    cityModel.city,
                    createLocation(
                        loadResult.data.coordinate.latitude,
                        loadResult.data.coordinate.longitude
                    )
                )
                return WeatherResponseHandlerResult(
                    localWeatherToShow = loadResult.data.copy(city = cityModel.city),
                    cityModelToSave = cityLocationModel,
                )
            }

            is LoadResult.Error -> {
                val cityLocationModel = CityLocationModel(cityModel.city, cityModel.location)
                var cityError: CityErrorEvent? = null
                var errorMessage = ""
                when (val error = loadResult.error) {
                    is ForecastError.ApiKeyInvalid -> {
                        errorMessage =
                            resourceManager.getString(R.string.api_key_invalid, cityModel.city)
                        statusStateHolder.updateStatus(Error(errorMessage))
                    }

                    is ForecastError.CityNotFound -> {
                        errorMessage =
                            resourceManager.getString(R.string.city_not_found, error.city)
                        statusStateHolder.updateStatus(
                            Warning(errorMessage)
                        )
                        cityError = CityErrorEvent.CityNotFound(error.city)
                    }

                    is ForecastError.LocalDataCorrupted -> {
                        errorMessage = resourceManager.getString(
                            R.string.local_data_corrupted,
                            cityModel.city
                        )
                        statusStateHolder.updateStatus(Error(errorMessage))
                    }

                    is ForecastError.NetworkError ->
                        when (error.type) {
                            ForecastError.NetworkError.Type.ConnectionFailed -> {
                                errorMessage = resourceManager.getString(
                                    R.string.connection_refused,
                                    cityModel.city
                                )
                                statusStateHolder.updateStatus(Error(errorMessage))
                            }

                            ForecastError.NetworkError.Type.NoInternet -> {
                                errorMessage = resourceManager.getString(
                                    R.string.network_disconnected,
                                    cityModel.city
                                )
                                statusStateHolder.updateStatus(Error(errorMessage))
                            }

                            ForecastError.NetworkError.Type.Timeout -> {
                                errorMessage = resourceManager.getString(
                                    R.string.request_timeout,
                                    cityModel.city
                                )
                                statusStateHolder.updateStatus(Error(errorMessage))
                            }

                            ForecastError.NetworkError.Type.SecurityError -> {
                                errorMessage = resourceManager.getString(
                                    R.string.ssl_error,
                                    cityModel.city
                                )
                                statusStateHolder.updateStatus(Error(errorMessage))
                            }

                            else -> {
                                errorMessage = resourceManager.getString(
                                    R.string.network_error_generic,
                                    cityModel.city
                                )
                                statusStateHolder.updateStatus(Error(errorMessage))
                            }
                        }

                    is ForecastError.NoDataAvailable -> {
                        errorMessage = resourceManager.getString(
                            R.string.no_weather_data_available,
                            cityModel.city
                        )
                        statusStateHolder.updateStatus(Error(errorMessage))
                    }

                    is ForecastError.UncategorizedError -> {
                        val error = error.cause ?: error.message
                        loggingService.logError(TAG, "Uncategorized error: $error")
                        statusStateHolder.updateStatus(Error(error.toString()))
                    }
                }
                return WeatherResponseHandlerResult(
                    cityModelToSave = cityLocationModel,
                    errorToShow = errorMessage,
                    cityError = cityError,
                )
            }

            LoadResult.Loading -> {
                statusStateHolder.updateStatus(
                    Info(
                        resourceManager.getString(R.string.forecast_downloading)
                    )
                )
                return WeatherResponseHandlerResult(
                    isLoading = true
                )
            }
        }
    }

    companion object {
        private const val TAG = "WeatherResponseHandler"
    }
}