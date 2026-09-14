package io.github.vladchenko.weatherforecast.core.data.mapper

import io.github.vladchenko.weatherforecast.core.data.model.DataError
import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Maps data-layer errors to domain-level errors.
 *
 * Technical details such as Java networking exceptions are interpreted here
 * and converted into domain-level concepts before crossing the data/domain boundary.
 */
class DataErrorToForecastErrorMapper {

    /**
     * Converts a [DataError] from the data layer into a corresponding [ForecastError] for the domain layer.
     *
     * @param dataError the data-layer error to map; must not be null
     * @return the equivalent domain-level [ForecastError]
     */
    fun map(dataError: DataError): ForecastError {
        return when (dataError) {

            is DataError.ApiKeyInvalid ->
                ForecastError.ApiKeyInvalid(message = dataError.message)

            is DataError.CityNotFound ->
                ForecastError.CityNotFound(city = dataError.city, message = dataError.message)

            is DataError.NetworkError -> dataError.toForecastError()

            is DataError.ParsingError ->
                ForecastError.DataParsingError(message = dataError.message)

            is DataError.ServerError ->
                ForecastError.ServerError(code = dataError.code, message = dataError.message)

            is DataError.DatabaseError ->
                ForecastError.LocalStorageError(message = dataError.message)

            DataError.ResponseNoBodyError ->
                ForecastError.NoDataAvailable(message = "Empty response body")

            is DataError.UncategorizedError ->
                ForecastError.UncategorizedError(
                    message = dataError.cause.message
                        ?: "An unexpected error occurred"
                )
        }
    }

    private fun DataError.NetworkError.toForecastError():
            ForecastError.NetworkError {

        val type = when (cause) {
            is UnknownHostException ->
                ForecastError.NetworkError.Type.NoInternet

            is SocketTimeoutException ->
                ForecastError.NetworkError.Type.Timeout

            is ConnectException ->
                ForecastError.NetworkError.Type.ConnectionFailed

            is SSLException ->
                ForecastError.NetworkError.Type.SecurityError

            else ->
                ForecastError.NetworkError.Type.Other
        }

        return ForecastError.NetworkError(type = type)
    }
}