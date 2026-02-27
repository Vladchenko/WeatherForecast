package com.example.weatherforecast.models.data

import com.example.weatherforecast.models.domain.ForecastError

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
            is DataError.NetworkError -> ForecastError.NoInternet
            is DataError.ServerError -> ForecastError.NoDataAvailable("Server error: ${dataError.code}")
            is DataError.RequestFailError -> ForecastError.CityNotFound(
                city = dataError.requestBody,
                message = dataError.message
            )
            is DataError.ApiKeyInvalid -> ForecastError.NoDataAvailable(dataError.message)
            DataError.ResponseNoBodyError -> ForecastError.NoDataAvailable("Empty response body")
            DataError.DatabaseError -> ForecastError.LocalDataCorrupted("Database error")
        }
    }
}