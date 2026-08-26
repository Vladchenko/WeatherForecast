package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.models

import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models.CurrentWeather
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CityErrorEvent

/**
 * Represents the result of processing a server response within [WeatherResponseHandler].
 *
 * This data class is used to transfer data from [WeatherResponseHandler]
 * to [CurrentWeatherViewModel] after the weather request has been processed.
 * The ViewModel independently manages the UI state based on the received result,
 * ensuring a proper separation of responsibilities:
 * - Handler: Business logic for processing responses.
 * - ViewModel: UI state management.
 *
 * @property errorToShow The error resource id to display to the user.
 *                       Contains localized error text, or null if there is no error.
 * @property isLoading Indicates whether the weather data is currently being loaded.
 * @property cityError A special city error event (e.g., city not found),
 *                     which may contain a callback for navigation. Null if there is no such error.
 * @property localWeatherToShow The weather data model from database to display in the UI.
 *                              Contains the loaded data, or null if loading failed.
 * @property remoteWeatherToShow The weather data model loaded from internet to display in the UI.
 *                               Contains the loaded data, or null if loading failed.
 */
data class WeatherResponseHandlerResult(
    val errorToShow: Int? = null,
    val isLoading: Boolean? = false,
    val cityError: CityErrorEvent? = null,
    val localWeatherToShow: CurrentWeather? = null,
    val remoteWeatherToShow: CurrentWeather? = null,
)