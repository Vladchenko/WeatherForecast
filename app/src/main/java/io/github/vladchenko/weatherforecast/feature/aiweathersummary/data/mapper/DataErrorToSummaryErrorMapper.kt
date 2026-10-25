package io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.mapper

import io.github.vladchenko.weatherforecast.core.data.model.DataError
import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError

/**
 * Maps data-layer errors to domain-level errors for AI weather summaries.
 *
 * Technical details from the data layer are interpreted here
 * and converted into domain-level concepts before crossing the data/domain boundary.
 */
class DataErrorToSummaryErrorMapper {

    /**
     * Converts a [DataError] from the data layer into a corresponding [ForecastError] for the domain layer.
     *
     * @param dataError the data-layer error to map; must not be null
     * @return the equivalent domain-level [ForecastError]
     */
    fun map(dataError: DataError): ForecastError {
        return when (dataError) {
            is DataError.DatabaseError ->
                ForecastError.LocalStorageError(message = dataError.message)

            is DataError.NetworkError ->
                ForecastError.NetworkError(type = ForecastError.NetworkError.Type.NoInternet)

            is DataError.ParsingError ->
                ForecastError.DataParsingError(message = dataError.message)

            is DataError.ResponseNoBodyError ->
                ForecastError.NoDataAvailable(message = "AI response was empty")

            is DataError.UncategorizedError ->
                ForecastError.UncategorizedError(
                    message = dataError.cause.message ?: "An unexpected error occurred"
                )

            is DataError.ApiKeyInvalid ->
                ForecastError.ApiKeyInvalid(message = dataError.message)

            is DataError.CityNotFound ->
                ForecastError.CityNotFound(city = dataError.city, message = dataError.message)

            is DataError.ServerError -> mapServerError(dataError)
        }
    }

    /**
     * Maps server errors to user-friendly domain errors based on HTTP status code.
     */
    private fun mapServerError(error: DataError.ServerError): ForecastError {
        return when (error.code) {
            403 -> ForecastError.AccessDenied(message = error.message)
            429 -> ForecastError.RateLimitExceeded(message = error.message)
            else -> ForecastError.ServerError(code = error.code, message = error.message)
        }
    }
}
