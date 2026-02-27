package com.example.weatherforecast.data.util

import com.example.weatherforecast.models.data.DataError
import com.example.weatherforecast.models.data.DataResult
import retrofit2.Response
import javax.inject.Inject

/**
 * Utility class responsible for processing raw [Response] objects from Retrofit
 * and converting them into standardized [DataResult] instances.
 *
 * ## Responsibilities
 * - Checks HTTP response success ([Response.isSuccessful])
 * - Validates presence of response body
 * - Extracts and parses error messages from failed responses
 * - Maps HTTP status codes and network issues to appropriate [DataError] types
 * - Returns a [DataResult.Success] with body if response is valid, or [DataResult.Error] otherwise
 *
 * ## Error Mapping Strategy
 * - `404 Not Found` → [DataError.RequestFailError] with extracted city name
 * - `401 Unauthorized` → [DataError.ApiKeyInvalid]
 * - `5xx Server Error` → [DataError.ServerError]
 * - Non-200 successful codes (e.g. 204) → mapped based on code
 * - Empty body → [DataError.ResponseNoBodyError]
 * - Any other HTTP or parsing issue → [DataError.NetworkError]
 *
 * ## Thread Safety
 * This class is stateless and can be safely injected and used across multiple threads.
 * Typically used within data sources running on IO dispatcher.
 */
class ResponseProcessor @Inject constructor() {

    /**
     * Processes a Retrofit [Response] and converts it into a [DataResult].
     *
     * Performs the following checks in order:
     * 1. If response is not successful (4xx, 5xx), returns [DataError.ServerError]
     * 2. If response body is null, returns [DataError.ResponseNoBodyError]
     * 3. If status code is not 200, maps to specific [DataError] (e.g. 404 → RequestFailError)
     * 4. Otherwise, wraps the body in [DataResult.Success]
     *
     * @param response the Retrofit [Response] to process; must not be null
     * @return [DataResult.Success] with body if successful and non-null, [DataResult.Error] otherwise
     *
     * @see Response.isSuccessful
     * @see Response.body
     * @see Response.code
     */
    fun <T> processResponse(response: Response<T>): DataResult<T> {
        val code = response.code()
        val message = readErrorBodySafely(response)

        if (!response.isSuccessful) {
            return DataResult.Error(
                DataError.ServerError(
                    response.code(),
                    "API call failed with code"
                )
            )
        }
        if (response.body() == null) {
            return DataResult.Error(DataError.ResponseNoBodyError)
        }

        return if (code != 200) {
            val error = when (code) {
                404 -> DataError.RequestFailError(
                    requestBody = extractCityFromUrl(response),
                    message = message
                )

                401 -> DataError.ApiKeyInvalid(message)
                in 500..599 -> DataError.ServerError(code, message)
                else -> DataError.NetworkError(Exception("HTTP $code: $message"))
            }
            DataResult.Error(error)
        } else {
            DataResult.Success(response.body()!!)
        }
    }

    /**
     * Extracts the last path segment from the request URL, typically representing the city name.
     *
     * Used to provide context in [DataError.RequestFailError] when a city is not found.
     *
     * @param response the response whose request URL is analyzed
     * @return the last path segment or "Unknown city" if not available
     */
    private fun extractCityFromUrl(response: Response<*>): String {
        return response.raw().request.url.pathSegments.lastOrNull() ?: "Unknown city"
    }

    /**
     * Safely reads the error body from the response without throwing exceptions.
     *
     * Catches any exception that might occur during reading (e.g. IOException).
     *
     * @param response the response from which to read the error body
     * @return error body as string, or fallback message if reading fails
     */
    private fun readErrorBodySafely(response: Response<*>): String {
        return try {
            response.errorBody()?.string() ?: "Empty error body"
        } catch (e: Exception) {
            "Failed to read error body: ${e.message}"
        }
    }
}