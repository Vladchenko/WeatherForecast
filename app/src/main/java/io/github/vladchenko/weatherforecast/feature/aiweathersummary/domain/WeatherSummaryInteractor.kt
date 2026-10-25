package io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain

import android.util.Log
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.domain.model.TemperatureUnit
import io.github.vladchenko.weatherforecast.data.repository.CurrentWeatherRepository
import io.github.vladchenko.weatherforecast.data.repository.HourlyWeatherRepository
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryRequest
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.models.AiModel
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.models.WeatherSummary

/**
 * Interactor for AI weather summary business logic.
 *
 * Fetches current weather and hourly forecast, builds a prompt for the AI model,
 * then requests AI analysis through the repository.
 *
 * @param promptBuilder prompt builder for AI requests.
 * @param summaryRepository repository for AI weather summaries.
 * @param hourlyWeatherRepository hourly forecast repository.
 * @param currentWeatherRepository current weather repository.
 */
class WeatherSummaryInteractor(
    val promptBuilder: WeatherPromptBuilder,
    val summaryRepository: WeatherSummaryRepository,
    val hourlyWeatherRepository: HourlyWeatherRepository,
    val currentWeatherRepository: CurrentWeatherRepository
) {
    suspend fun fetchWeatherSummary(
        city: String,
        latitude: Double,
        longitude: Double,
        temperatureUnit: TemperatureUnit,
        aiModel: AiModel
    ): WeatherSummary {
        val currentWeather = currentWeatherRepository.refreshWeatherForLocation(
            city,
            latitude,
            longitude,
            temperatureUnit
        )
        val hourlyWeather = hourlyWeatherRepository.refreshWeatherForLocation(
            city,
            latitude,
            longitude,
            temperatureUnit
        )

        val currentWeatherData = extractData(currentWeather)
        val hourlyWeatherData = extractData(hourlyWeather)
        val messages = promptBuilder.buildMessages(
            city = city,
            currentWeather = currentWeatherData,
            hourlyWeather = hourlyWeatherData,
            temperatureUnit = temperatureUnit
        )
        val request = AISummaryRequest(model = aiModel.id, messages = messages)
        Log.d("WeatherSummaryInteractor", "Request: $request")
        return summaryRepository.fetchWeatherSummary(
            city,
            latitude,
            longitude,
            request
        )
    }

    private fun <T> extractData(result: LoadResult<T>): T? {
        return when (result) {
            is LoadResult.Remote -> result.data
            is LoadResult.Local -> result.data
            is LoadResult.Error -> null
        }
    }
}