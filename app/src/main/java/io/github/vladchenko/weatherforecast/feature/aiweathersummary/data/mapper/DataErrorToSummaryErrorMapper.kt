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
     * @param cacheAvailable whether local cache was available; if false, errors are wrapped in cache-unavailable variants
     * @return the equivalent domain-level [ForecastError]
     */
    fun map(dataError: DataError, cacheAvailable: Boolean = true): ForecastError {
        return when (dataError) {
            is DataError.DatabaseError ->
                ForecastError.LocalStorageError

            is DataError.NetworkError ->
                if (cacheAvailable)
                    ForecastError.NetworkError(type = ForecastError.NetworkError.Type.NoInternet)
                else
                    ForecastError.NetworkAndCacheError

            is DataError.ParsingError ->
                ForecastError.DataParsingError

            is DataError.ResponseNoBodyError ->
                ForecastError.NoDataAvailable

            is DataError.UncategorizedError ->
                ForecastError.UncategorizedError(
                    cause = dataError.cause
                )

            is DataError.ApiKeyInvalid ->
                ForecastError.ApiKeyInvalid

            is DataError.CityNotFound ->
                ForecastError.CityNotFound(city = dataError.city)

            is DataError.ResourceNotFound ->
                ForecastError.ResourceNotFound

            is DataError.RateLimitExceeded ->
                if (cacheAvailable)
                    ForecastError.RateLimitExceeded
                else
                    ForecastError.ServerAndCacheError(code = 429)

            is DataError.AccessDenied ->
                if (cacheAvailable)
                    ForecastError.AccessDenied
                else
                    ForecastError.AccessDeniedAndCacheError

            is DataError.NetworkAndCacheError ->
                ForecastError.NetworkAndCacheError

            is DataError.ServerError ->
                if (cacheAvailable)
                    ForecastError.ServerError(code = dataError.code)
                else
                    ForecastError.ServerAndCacheError(code = dataError.code)
        }
    }
}
