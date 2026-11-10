package io.github.vladchenko.weatherforecast.feature.aiweathersummary.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.core.presentation.mapper.ForecastErrorToMessageIdMapper
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.WeatherSummaryInteractor
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.models.WeatherSummary
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityRepository
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
 * @param chosenCityRepository interactor for the chosen city.
 * @param weatherSummaryInteractor interactor for AI summary business logic.
 * @param errorToMessageMapper maps [ForecastError] to string resource IDs for display.
 */
@HiltViewModel
class WeatherSummaryViewModel @Inject constructor(
    private val statusStateHolder: StatusStateHolder,
    private val preferencesManager: PreferencesManager,
    private val chosenCityRepository: ChosenCityRepository,
    private val weatherSummaryInteractor: WeatherSummaryInteractor,
    private val errorToMessageMapper: ForecastErrorToMessageIdMapper
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

            val cityModel = chosenCityRepository.loadChosenCity()

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
                        val messageId = errorToMessageMapper.map(summary.networkError)
                        statusStateHolder.updateWarningStatus(messageId, cityModel.city)
                        WeatherSummaryUiState.Local(
                            llm = summary.data.llm,
                            summary = summary.data.summary,
                            timeStamp = summary.data.timeStamp,
                            networkError = messageId.toString()
                        )
                    }

                    is WeatherSummary.Error -> {
                        val messageId = errorToMessageMapper.map(summary.error)
                        statusStateHolder.updateErrorStatus(messageId, cityModel.city)
                        WeatherSummaryUiState.Error(
                            city = cityModel.city,
                            messageId = messageId
                        )
                    }
                }
            } catch (_: Exception) {
                WeatherSummaryUiState.Error(cityModel.city, R.string.error_unknown)
            }
        }
    }


}