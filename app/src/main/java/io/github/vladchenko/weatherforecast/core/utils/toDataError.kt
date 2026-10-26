package io.github.vladchenko.weatherforecast.core.utils

import io.github.vladchenko.weatherforecast.core.data.model.DataError
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Converts a [Throwable] into a [io.github.vladchenko.weatherforecast.core.data.model.DataError] for use in data sources.
 *
 * This function is internal to the data layer and should not be exposed to domain or presentation.
 * It standardizes mapping of common network/parsing exceptions to meaningful data-layer errors.
 *
 * Note: [IOException] is used as a catch-all for Retrofit/OkHttp exceptions. If new local I/O sources
 * are added, move it from this branch to a separate handling.
 */
fun Throwable.toDataError(): DataError {
    return when (this) {
        is SocketTimeoutException -> DataError.NetworkError(this)
        is UnknownHostException -> DataError.NetworkError(this)
        is ConnectException -> DataError.NetworkError(this)
        is SSLException -> DataError.NetworkError(this)
        is SerializationException -> DataError.ParsingError(cause = this)
        is IOException -> DataError.NetworkError(this)
        else -> DataError.UncategorizedError(this)
    }
}