package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.outputmapper

import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.ui.state.DataSource
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models.CurrentWeather
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.domaintouimapper.WeatherDomainToUiMapper
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.models.CurrentWeatherUi
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.WeatherResponseHandler
import java.time.DateTimeException

/**
 * Implementation of [WeatherOutputMapper] that processes raw weather data loading results
 * and maps them to a UI-ready structure ([WeatherProcessingResult]).
 *
 * This class coordinates the evaluation of server/local responses, determines the appropriate
 * UI state ([WeatherUiState]), handles errors, and extracts city state updates. It delegates
 * domain-to-UI conversion to [weatherDomainToUiMapper].
 *
 * @property weatherResponseHandler Coordinates the processing of [LoadResult] for remote and local sources.
 * @property weatherDomainToUiMapper Converts domain models ([CurrentWeather]) to UI models ([CurrentWeatherUi]).
 */
class WeatherOutputMapperImpl(
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
            try {
                response.remoteWeatherToShow?.let {
                    uiState = WeatherUiState.Success(
                        weatherDomainToUiMapper.toCurrentWeatherUi(
                            model = it
                        ),
                        DataSource.REMOTE
                    )
                }
                response.localWeatherToShow?.let {
                    uiState = WeatherUiState.Success(
                        weatherDomainToUiMapper.toCurrentWeatherUi(
                            model = it
                        ),
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
            } catch (_: DateTimeException) {
                uiState = WeatherUiState.Error(
                    city = cityModel.city, message = null, messageId = R.string.bad_date_format,
                )
            }
        }
        return WeatherProcessingResult(
            uiState = uiState
        )
    }
}