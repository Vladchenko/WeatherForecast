package com.example.weatherforecast.models.domain

import com.example.weatherforecast.models.data.HourlyForecastResponse
import com.example.weatherforecast.models.data.WeatherForecastResponse

sealed class LoadResult<out T> {
    data class Remote<T>(
        val data: T,
        val rawResponse: Any // Changed to Any to accept both response types
    ) : LoadResult<T>()

    data class Local<T>(
        val data: T,
        val error: String
    ) : LoadResult<T>()

    data class Fail<T>(val exception: Exception): LoadResult<T>()
}