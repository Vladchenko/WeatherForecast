package io.github.vladchenko.weatherforecast.feature.aiweathersummary.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.ChosenCityInteractor
import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.WeatherSummaryInteractor
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.models.WeatherSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the UI state of the AI weather forecast summary.
 *
 * Requests AI analysis from the interactor, processes results (remote response,
 * cached response, error) and publishes state to the UI via StateFlow.
 *
 * @param statusStateHolder holder for status bar messages.
 * @param preferencesManager app settings manager.
 * @param chosenCityInteractor interactor for the chosen city.
 * @param weatherSummaryInteractor interactor for AI summary business logic.
 */
@HiltViewModel
class WeatherSummaryViewModel @Inject constructor(
    private val statusStateHolder: StatusStateHolder,
    private val preferencesManager: PreferencesManager,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val weatherSummaryInteractor: WeatherSummaryInteractor
) : ViewModel() {

    private val _weatherSummary =
        MutableStateFlow<WeatherSummaryUiState>(WeatherSummaryUiState.Loading)
    val weatherSummary: StateFlow<WeatherSummaryUiState> = _weatherSummary.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000L),
        WeatherSummaryUiState.Loading
    )

    /**
     * Requests the AI weather forecast summary for the chosen city.
     *
     * Retrieves data about the chosen city, requests AI analysis and updates
     * the UI state depending on the result (Remote, Local, Error).
     */
    fun fetchWeatherSummary() {
        viewModelScope.launch {
            _weatherSummary.value = WeatherSummaryUiState.Loading
            statusStateHolder.updateInfoStatus(R.string.loading)

            val cityModel = chosenCityInteractor.loadChosenCity()

            _weatherSummary.value = try {
                val summary = weatherSummaryInteractor.fetchWeatherSummary(
                    cityModel.city,
                    cityModel.coordinate.latitude,
                    cityModel.coordinate.longitude,
                    preferencesManager.temperatureUnit.value,
                    preferencesManager.aiModel.value
                )
                when (summary) {
                    is WeatherSummary.Remote -> {
                        statusStateHolder.updateInfoStatus(
                            R.string.weather_summary_fetched,
                            cityModel.city
                        )
                        WeatherSummaryUiState.Remote(
                            llm = summary.data.llm,
                            summary = summary.data.summary
                        )
                    }

                    is WeatherSummary.Local -> {
                        statusStateHolder.updateWarningStatus(
                            R.string.weather_summary_cached_fetched,
                            cityModel.city
                        )
                        WeatherSummaryUiState.Local(
                            llm = summary.data.llm,
                            summary = summary.data.summary,
                            timeStamp = summary.data.timeStamp,
                            networkError = summary.networkError
                        )
                    }

                    is WeatherSummary.Error -> {
                        val messageId = when (val error = summary.error) {
                            is ForecastError.ApiKeyInvalid -> {
                                statusStateHolder.updateErrorStatus(R.string.error_api_key_invalid)
                                R.string.error_api_key_invalid
                            }
                            is ForecastError.CityNotFound -> {
                                statusStateHolder.updateErrorStatus(
                                    R.string.error_city_not_found,
                                    error.city
                                )
                                R.string.error_city_not_found
                            }
                            is ForecastError.NetworkError -> when (error.type) {
                                is ForecastError.NetworkError.Type.NoInternet -> {
                                    statusStateHolder.updateErrorStatus(R.string.error_no_internet)
                                    R.string.error_no_internet
                                }
                                is ForecastError.NetworkError.Type.Timeout -> {
                                    statusStateHolder.updateErrorStatus(R.string.error_timeout)
                                    R.string.error_timeout
                                }
                                is ForecastError.NetworkError.Type.ConnectionFailed -> {
                                    statusStateHolder.updateErrorStatus(R.string.error_connection_failed)
                                    R.string.error_connection_failed
                                }
                                is ForecastError.NetworkError.Type.SecurityError -> {
                                    statusStateHolder.updateErrorStatus(R.string.error_security)
                                    R.string.error_security
                                }
                                is ForecastError.NetworkError.Type.Other -> {
                                    statusStateHolder.updateErrorStatus(R.string.error_network)
                                    R.string.error_network
                                }
                            }
                            is ForecastError.DataParsingError -> {
                                statusStateHolder.updateErrorStatus(R.string.error_parsing)
                                R.string.error_parsing
                            }
                            is ForecastError.ServerError -> {
                                statusStateHolder.updateErrorStatus(R.string.error_server)
                                R.string.error_server
                            }
                            is ForecastError.AccessDenied -> {
                                statusStateHolder.updateErrorStatus(R.string.error_access_denied)
                                R.string.error_access_denied
                            }
                            is ForecastError.RateLimitExceeded -> {
                                statusStateHolder.updateErrorStatus(R.string.error_rate_limit)
                                R.string.error_rate_limit
                            }
                            is ForecastError.LocalStorageError -> {
                                statusStateHolder.updateErrorStatus(R.string.error_database)
                                R.string.error_database
                            }
                            is ForecastError.LocalDataCorrupted -> {
                                statusStateHolder.updateErrorStatus(R.string.local_data_corrupted)
                                R.string.local_data_corrupted
                            }
                            is ForecastError.NoDataAvailable -> {
                                statusStateHolder.updateErrorStatus(R.string.error_no_data)
                                R.string.error_no_data
                            }
                            is ForecastError.UncategorizedError -> {
                                statusStateHolder.updateErrorStatus(R.string.error_unknown)
                                R.string.error_unknown
                            }
                        }
                        WeatherSummaryUiState.Error(
                            city = cityModel.city,
                            messageId = messageId
                        )
                    }
                }
            } catch (e: Exception) {
                WeatherSummaryUiState.Error(cityModel.city, null)
            }
        }
    }
}