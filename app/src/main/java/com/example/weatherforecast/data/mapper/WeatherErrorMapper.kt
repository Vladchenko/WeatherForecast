package com.example.weatherforecast.data.util

import com.example.weatherforecast.models.domain.ForecastError
import com.example.weatherforecast.models.domain.LoadResult
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.UnknownHostException

/**
 * Utility object to map HTTP/network errors to domain ForecastError.
 */
object WeatherErrorMapper {

    fun mapToLoadResult(response: Response<*>): LoadResult.Error {
        val code = response.code()
        val message = readErrorBodySafely(response)

        val error = when (code) {
            404 -> ForecastError.CityNotFound(
                city = extractCityFromUrl(response),
                message = message
            )
            401 -> ForecastError.ApiKeyInvalid(message)
            in 500..599 -> ForecastError.ServerError(code, message)
            else -> ForecastError.NetworkError(Exception("HTTP $code: $message"))
        }
        return LoadResult.Error(error)
    }

    fun mapToLoadResult(throwable: Throwable): LoadResult.Error {
        val error = when (throwable) {
            is ForecastError -> throwable
            is UnknownHostException, is IOException -> ForecastError.NoInternet
            is HttpException -> {
                val code = throwable.code()
                val message = try {
                    throwable.response()?.errorBody()?.string() ?: "Empty error body"
                } catch (e: Exception) {
                    "Failed to read error body: ${e.message}"
                }
                when (code) {
                    404 -> ForecastError.CityNotFound(city = "Unknown", message = message)
                    401 -> ForecastError.ApiKeyInvalid(message)
                    in 500..599 -> ForecastError.ServerError(code, message)
                    else -> ForecastError.NetworkError(Exception("HTTP $code: $message"))
                }
            }
            else -> ForecastError.NetworkError(throwable)
        }
        return LoadResult.Error(error)
    }

    private fun extractCityFromUrl(response: Response<*>): String {
        return response.raw().request.url.pathSegments.lastOrNull() ?: "Unknown city"
    }

    private fun readErrorBodySafely(response: Response<*>): String {
        return try {
            response.errorBody()?.string() ?: "Empty error body"
        } catch (e: Exception) {
            "Failed to read error body: ${e.message}"
        }
    }
}