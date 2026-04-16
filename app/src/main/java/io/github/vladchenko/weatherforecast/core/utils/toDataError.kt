package io.github.vladchenko.weatherforecast.core.utils

import io.github.vladchenko.weatherforecast.core.data.models.DataError
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Converts a [Throwable] into a [io.github.vladchenko.weatherforecast.core.data.models.DataError] for use in data sources.
 *
 * This function is internal to the data layer and should not be exposed to domain or presentation.
 * It standardizes mapping of common network/parsing exceptions to meaningful data-layer errors.
 */
fun Throwable.toDataError(): DataError {
    return when (this) {
        is SocketTimeoutException -> DataError.NetworkError(this)
        is UnknownHostException,
        is IOException -> DataError.NetworkError(this)
        is SerializationException -> DataError.ParsingError("JSON parsing failed", this)
        else -> DataError.UncategorizedError(this)
    }
}