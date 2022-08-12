package com.example.weatherforecast.data.api.customexceptions

import retrofit2.HttpException

/**
 *
 */
class WeatherForecastExceptionMapper(arguments: List<String>) : HttpExceptionMapper(arguments) {

    override fun map(httpException: HttpException): Exception? {
        return if (httpException.code() == 404) {
            // RecipeNotFoundException(RecipeId(callArguments.first()))     //TODO
            Exception()
        } else {
            null
        }
    }
}