package com.example.weatherforecast.data.api.customexceptions

import retrofit2.HttpException

/**
 * Custom mapper for Retrofit exceptions.
 */
class WeatherForecastExceptionMapper(arguments: List<String>) : HttpExceptionMapper(arguments) {

    override fun map(httpException: HttpException): Exception? {
        return if (httpException.code() == 404) {
            CityNotFoundException(callArguments[0], callArguments[0] + " - city is not found")
        } else {
            httpException
        }
    }
}