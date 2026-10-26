package io.github.vladchenko.weatherforecast.core.presentation.mapper

import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError

/**
 * Maps domain-level [ForecastError] instances to string resource IDs for display.
 *
 * This class encapsulates the error-to-message mapping logic, keeping ViewModels clean
 * and following the Single Responsibility Principle.
 *
 * Centralized in core to avoid duplication across features — all features share
 * the same R resources, so a single mapper is sufficient.
 */
class ForecastErrorToMessageIdMapper {

    /**
     * Maps a [ForecastError] to the corresponding string resource ID for display.
     *
     * @param error the domain-level error to map
     * @return the string resource ID for the error message
     */
    fun map(error: ForecastError): Int = when (error) {
        is ForecastError.ApiKeyInvalid -> R.string.error_api_key_invalid
        is ForecastError.CityNotFound -> R.string.error_city_not_found
        is ForecastError.NetworkError -> mapNetworkErrorToMessageId(error)
        is ForecastError.NetworkAndCacheError -> R.string.error_network_and_cache_unavailable
        is ForecastError.DataParsingError -> R.string.error_parsing
        is ForecastError.ServerError -> R.string.error_server
        is ForecastError.ServerAndCacheError -> R.string.error_server_and_cache_unavailable
        is ForecastError.AccessDenied -> R.string.error_access_denied
        is ForecastError.AccessDeniedAndCacheError -> R.string.error_access_denied_and_cache_unavailable
        is ForecastError.RateLimitExceeded -> R.string.error_rate_limit
        is ForecastError.LocalStorageError -> R.string.error_database
        is ForecastError.LocalDataCorrupted -> R.string.local_data_corrupted
        is ForecastError.NoDataAvailable -> R.string.no_weather_data_available
        is ForecastError.ResourceNotFound -> R.string.error_resource_not_found
        is ForecastError.UncategorizedError -> R.string.error_unknown
    }

    private fun mapNetworkErrorToMessageId(error: ForecastError.NetworkError): Int =
        when (error.type) {
            is ForecastError.NetworkError.Type.NoInternet -> R.string.network_disconnected
            is ForecastError.NetworkError.Type.HostUnreachable -> R.string.error_host_unreachable
            is ForecastError.NetworkError.Type.Timeout -> R.string.request_timeout
            is ForecastError.NetworkError.Type.ConnectionFailed -> R.string.connection_refused
            is ForecastError.NetworkError.Type.SecurityError -> R.string.ssl_error
            is ForecastError.NetworkError.Type.Other -> R.string.network_error_generic
        }
}
