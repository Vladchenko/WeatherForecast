package io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.api

import io.github.vladchenko.weatherforecast.BuildConfig
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryRequest
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming

private const val AUTHORIZATION = "Authorization"
private const val V_1_CHAT_COMPLETIONS = "v1/chat/completions"
private const val CONTENT_TYPE_APPLICATION_JSON = "Content-Type: application/json"
private const val BEARER_BUILD_CONFIG_OPEN_ROUTER_API_KEY_ = "Bearer ${BuildConfig.OPEN_ROUTER_API_KEY}"

/**
 * Retrofit API service for interacting with OpenRouter AI API.
 *
 * Handles all HTTP requests for generating AI-powered weather summaries
 * using large language models (Gemma, MiniMax, Qwen, etc.).
 */
interface WeatherSummaryApiService {

    /**
     * Fetches an AI-generated weather summary for the given city.
     *
     * Sends current weather and hourly forecast data to the AI model
     * and receives a practical weather recommendation.
     *
     * @param authorization Bearer token for OpenRouter API authentication
     * @param request AI chat completion request containing messages
     * @return AI response with generated weather summary
     */
    @POST(V_1_CHAT_COMPLETIONS)
    @Headers(CONTENT_TYPE_APPLICATION_JSON)
    suspend fun fetchWeatherSummary(
        @Header(AUTHORIZATION) authorization: String = BEARER_BUILD_CONFIG_OPEN_ROUTER_API_KEY_,
        @Body request: AISummaryRequest
    ): Response<AISummaryResponse>

    /**
     * Fetches an AI-generated weather summary using streaming response.
     *
     * Suitable for displaying partial results as they are generated.
     *
     * @param authorization Bearer token for OpenRouter API authentication
     * @param request AI chat completion request containing messages
     * @return Retrofit response with raw response body for streaming parsing
     */
    @POST(V_1_CHAT_COMPLETIONS)
    @Headers(CONTENT_TYPE_APPLICATION_JSON)
    @Streaming
    fun fetchWeatherSummaryStreaming(
        @Header(AUTHORIZATION) authorization: String = BEARER_BUILD_CONFIG_OPEN_ROUTER_API_KEY_,
        @Body request: AISummaryRequest
    ): Response<okhttp3.ResponseBody>
}