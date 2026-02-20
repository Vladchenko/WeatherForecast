package com.example.weatherforecast.presentation.viewmodel.forecast

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.preferences.PreferencesManager
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.HourlyWeatherInteractor
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.models.domain.ForecastError
import com.example.weatherforecast.models.domain.HourlyWeatherDomainModel
import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import com.example.weatherforecast.utils.ResourceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Viewmodel for hourly weather forecast.
 *
 * @constructor
 * @param connectivityObserver observes internet connectivity state.
 * @param coroutineDispatchers dispatchers coroutines.
 * @property resourceManager resource manager.
 * @property preferencesManager preferences manager.
 * @property chosenCityInteractor interactor to get chosen city.
 * @property hourlyWeatherInteractor interactor to get hourly weather forecast.
 */
@HiltViewModel
class HourlyWeatherViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
    coroutineDispatchers: CoroutineDispatchers,
    private val resourceManager: ResourceManager,
    private val preferencesManager: PreferencesManager,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val hourlyWeatherInteractor: HourlyWeatherInteractor,
) : AbstractViewModel(connectivityObserver, coroutineDispatchers) {

    val hourlyWeatherStateFlow: StateFlow<HourlyWeatherDomainModel?>
        get() = _hourlyWeatherStateFlow
    private val _hourlyWeatherStateFlow = MutableStateFlow<HourlyWeatherDomainModel?>(null)

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, throwable.stackTraceToString())
        showError(throwable.message.toString())
        showProgressBarState.value = false
    }

    /**
     * Download hourly weather forecast on a [city].
     */
    fun getHourlyWeatherForCity(city: String) {
        showProgressBarState.value = true
        viewModelScope.launch(exceptionHandler) {
            val temperatureType = preferencesManager.temperatureType.first()
            val result = hourlyWeatherInteractor.loadHourlyForecastForCity(
                temperatureType,
                city
            )
            processServerResponse(city, result)
        }
    }

    /**
     * Download hourly weather forecast on a [cityModel].
     */
    fun getHourlyWeatherForLocation(cityModel: CityLocationModel) {
        showProgressBarState.value = true
        viewModelScope.launch(exceptionHandler) {
            val temperatureType = preferencesManager.temperatureType.first()
            val result = hourlyWeatherInteractor.loadHourlyWeatherForLocation(
                temperatureType,
                cityModel.location.latitude,
                cityModel.location.longitude
            )
            processServerResponse(cityModel.city, result)
        }
    }

    private fun processServerResponse(city: String, result: LoadResult<HourlyWeatherDomainModel>) {
        showProgressBarState.value = false
        when (result) {
            is LoadResult.Remote -> {
                _hourlyWeatherStateFlow.tryEmit(result.data)
                showMessage(
                    R.string.forecast_for_city_success,
                    result.data.city
                )
            }

            is LoadResult.Local -> {
                // TODO Implement showing local hourly forecast
                // showLocalForecast(result.data)
                showWarning(
                    resourceManager.getString(
                        R.string.forecast_for_city_outdated, city
                    )
                )
            }

            is LoadResult.Error -> {
                showError(
                    when (result.error) {
                        is ForecastError.NoInternet ->
                            resourceManager.getString(R.string.disconnected)

                        else -> resourceManager.getString(R.string.forecast_for_city_error)
                    }
                )
            }
        }
    }

    companion object {
        private const val TAG = "HourlyWeatherViewModel"
    }
}
