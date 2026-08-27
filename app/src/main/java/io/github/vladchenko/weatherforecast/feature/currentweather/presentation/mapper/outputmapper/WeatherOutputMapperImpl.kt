package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.outputmapper

import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.state.DataSource
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.toWeatherIconRes
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models.CurrentWeather
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.domaintouimapper.WeatherDomainToUiMapper
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.models.CurrentWeatherUi
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.WeatherResponseHandler

/**
 * Implementation of [WeatherOutputMapper] that processes raw weather data loading results
 * and maps them to a UI-ready structure ([WeatherProcessingResult]).
 *
 * This class coordinates the evaluation of server/local responses, determines the appropriate
 * UI state ([WeatherUiState]), handles errors, and extracts city state updates. It delegates
 * domain-to-UI conversion to [weatherDomainToUiMapper] and resource retrieval to [resourceManager].
 *
 * @property resourceManager Provides localized strings and resources for UI messages.
 * @property weatherResponseHandler Coordinates the processing of [LoadResult] for remote and local sources.
 * @property weatherDomainToUiMapper Converts domain models ([CurrentWeather]) to UI models ([CurrentWeatherUi]).
 */
class WeatherOutputMapperImpl(
    private val resourceManager: ResourceManager,
    private val weatherResponseHandler: WeatherResponseHandler,
    private val weatherDomainToUiMapper: WeatherDomainToUiMapper,
) : WeatherOutputMapper {

    override fun mapToUi(
        cityModel: CityLocationModel,
        loadResult: LoadResult<CurrentWeather>
    ): WeatherProcessingResult {
        val processedResponse = weatherResponseHandler.processServerResponse(
            cityModel = cityModel,
            loadResult = loadResult,
        )
        var uiState: WeatherUiState<CurrentWeatherUi>? = null
        processedResponse.let { response ->
            response.remoteWeatherToShow?.let {
                uiState = WeatherUiState.Success(
                    toUiModel(it),
                    DataSource.REMOTE
                )
            }
            response.localWeatherToShow?.let {
                uiState = WeatherUiState.Success(
                    toUiModel(it),
                    DataSource.LOCAL
                )
            }
            response.errorToShow?.let {
                uiState = WeatherUiState.Error(
                    city = cityModel.city, message = null, messageId = it
                )
            }
            if (response.isLoading == true) {
                uiState = WeatherUiState.Loading()
            }
        }
        return WeatherProcessingResult(
            uiState = uiState
        )
    }

    private fun toUiModel(forecastModel: CurrentWeather) =
        weatherDomainToUiMapper.toCurrentWeatherUi(
            model = forecastModel,
            defaultErrorMessage = resourceManager.getString(R.string.bad_date_format),
            toWeatherIconRes = { weatherIconId ->
                toWeatherIconRes(weatherIconId)
            }
        )
}