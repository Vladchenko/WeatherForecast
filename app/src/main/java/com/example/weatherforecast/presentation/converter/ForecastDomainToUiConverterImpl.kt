package com.example.weatherforecast.presentation.converter

import android.util.Log
import com.example.weatherforecast.models.domain.WeatherForecast
import com.example.weatherforecast.models.presentation.Coordinate
import com.example.weatherforecast.models.presentation.WeatherForecastUi
import com.example.weatherforecast.presentation.PresentationConstants.UI_DATE_FORMAT
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Implementation of [ForecastDomainToUiConverter]
 */
class ForecastDomainToUiConverterImpl: ForecastDomainToUiConverter {

    override fun convert(model: WeatherForecast,
                         defaultErrorMessage: String,
                         getWeatherIconId: (String) -> Int
    ): WeatherForecastUi {
        val displayDate = getCurrentDateOrError(
            dateTime = model.dateTime,
            errorMessage = defaultErrorMessage
        )
        val iconId = getWeatherIconId(model.iconCode)
        return WeatherForecastUi(
            city = model.city,
            coordinate = Coordinate(model.coordinate.latitude, model.coordinate.longitude),
            dateTime = displayDate,
            weatherIconId = iconId,
            temperature = model.temperature,
            weatherType = model.weatherType,
            temperatureType = model.temperatureType,
            serverError = model.serverError,
        )
    }

    private fun getCurrentDateOrError(dateTime: String, errorMessage: String, ): String {
        return try {
            SimpleDateFormat(UI_DATE_FORMAT, Locale.getDefault())
                .format(Date(dateTime.toLong() * 1000))
        } catch (ex: Exception) {
            Log.e(TAG, ex.message.toString())
            errorMessage
        }
    }

    companion object {
        private const val TAG = "ForecastDomainToUiConverterImpl"
    }
}