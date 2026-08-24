package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.outputmapper

import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models.CurrentWeather

/**
 * Interface for processing raw weather data loading results and mapping them to a UI-ready structure.
 *
 * Implementations of this interface are responsible for evaluating the loading result,
 * handling errors or loading states, and producing a [WeatherProcessingResult] suitable for UI display.
 */
interface WeatherOutputMapper {
    /**
     * Processes the raw weather data loading result and maps it to a UI processing result.
     *
     * @param cityModel The currently selected or requested city location model.
     * @param loadResult The result of the weather data loading operation ([LoadResult]).
     * @return A [WeatherProcessingResult] containing the mapped UI state, city update data, or error event.
     */
    fun mapToUi(
        cityModel: CityLocationModel,
        loadResult: LoadResult<CurrentWeather>
    ): WeatherProcessingResult
}