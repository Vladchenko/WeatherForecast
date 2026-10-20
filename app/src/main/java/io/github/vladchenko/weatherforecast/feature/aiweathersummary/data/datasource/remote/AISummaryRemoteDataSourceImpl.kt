package io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.remote

import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.api.AISummaryApiService
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryRequest
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryResponse
import retrofit2.HttpException
import java.io.IOException

/**
 * Implementation of [AISummaryRemoteDataSource] for working with the remote API.
 * Uses [AISummaryApiService] to send requests to the AI model.
 *
 * @param apiService service for working with the remote AI summary API.
 */
class AISummaryRemoteDataSourceImpl(
    private val apiService: AISummaryApiService,
) : AISummaryRemoteDataSource {

    /**
     * Requests AI weather forecast analysis via the API.
     *
     * @throws IOException if a network error occurs.
     * @throws HttpException if the API returns an error response.
     */
    override suspend fun fetchWeatherSummary(request: AISummaryRequest): AISummaryResponse {
        val response = apiService.fetchWeatherSummary(request = request)
        
        if (response.isSuccessful) {
            return response.body() ?: throw HttpException(response)
        }
        
        // Parse error body to get API error message
        val errorBody = response.errorBody()?.string()
        val errorMessage = try {
            val errorResponse = com.google.gson.Gson().fromJson(
                errorBody,
                ApiErrorResponse::class.java
            )
            errorResponse.error?.message ?: "API error: ${response.code()}"
        } catch (e: Exception) {
            "API error: ${response.code()}"
        }
        
        throw ApiHttpException(errorMessage, response.code())
    }
}

/**
 * HTTP error response from OpenRouter API.
 */
private data class ApiErrorResponse(
    val error: ApiErrorWrapper?
)

/**
 * API error wrapper.
 */
private data class ApiErrorWrapper(
    val message: String,
    val code: Int? = null
)

/**
 * Custom exception for API HTTP errors.
 *
 * @param message error message from the API.
 * @param statusCode HTTP status code.
 */
class ApiHttpException(
    override val message: String,
    val statusCode: Int
) : Exception(message)