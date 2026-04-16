package io.github.vladchenko.weatherforecast.core.data.mapper

import io.github.vladchenko.weatherforecast.core.data.model.DataError
import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError

/**
 * Mapper that converts data-layer errors ([DataError]) into domain-level errors ([ForecastError]).
 *
 * Ensures clean separation between layers by preventing data-layer types from leaking into the domain.
 * Used by repositories to transform results from [DataResult] (data layer) into [LoadResult] (domain layer).
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
            is DataError.ApiKeyInvalid -> ForecastError.ApiKeyInvalid(dataError.message)
            is DataError.DatabaseError -> ForecastError.LocalDataCorrupted("Database error")
            is DataError.NetworkError -> ForecastError.NetworkError.fromThrowable(dataError.cause)
            is DataError.RequestFailError -> ForecastError.CityNotFound(
                city = dataError.query,
                message = dataError.message
            )
            is DataError.ResponseNoBodyError -> ForecastError.NoDataAvailable("Empty response body")
            is DataError.ServerError -> ForecastError.NoDataAvailable("Server error: ${dataError.code}")
            is DataError.UncategorizedError -> ForecastError.UncategorizedError(
                message = dataError.cause.message ?: "An unexpected error occurred",
                cause = dataError.cause
            )
            is DataError.ParsingError -> ForecastError.NoDataAvailable("Failed to parse weather data")
        }
    }
}