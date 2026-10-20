package io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models

import com.google.gson.annotations.SerializedName

/**
 * Response object from AI API (OpenRouter).
 *
 * Contains information about the generated response, model, token usage statistics,
 * and additional metadata.
 *
 * @param id request identifier.
 * @param model identifier of the used model.
 * @param created response creation timestamp.
 * @param object type of object (usually "chat.completion").
 * @param choices list of response options.
 * @param usage token usage statistics.
 * @param systemFingerprint system identifier.
 * @param error API error (present when request fails).
 */
data class AISummaryResponse(
    val id: String,
    val model: String,
    val created: Long,
    val `object`: String, // обычно "chat.completion"
    val choices: List<Choice>,
    val usage: Usage? = null,
    @SerializedName("system_fingerprint") val systemFingerprint: String? = null,
    val error: ApiError? = null
)

/**
 * API error response from OpenRouter.
 *
 * @param message human-readable error description.
 * @param type error type (e.g., "not_found", "invalid_request_error").
 * @param code error code (e.g., 404, 429).
 */
data class ApiError(
    val message: String,
    val type: String? = null,
    val code: Int? = null
)

/**
 * Response option from the AI model.
 *
 * @param index option index.
 * @param message message from the assistant.
 * @param finishReason completion reason ("stop", "length", "tool_calls", "content_filter", "error").
 * @param nativeFinishReason native completion reason from the provider.
 */
data class Choice(
    val index: Int,
    val message: ResponseMessage,
    @SerializedName("finish_reason") val finishReason: String?, // "stop", "length", "tool_calls", "content_filter", "error"
    @SerializedName("native_finish_reason") val nativeFinishReason: String? = null
)

/**
 * Message from AI assistant.
 *
 * @param role role (always "assistant").
 * @param content text content of the response.
 * @param reasoning model reasoning chain (if supported).
 * @param toolCalls tool calls (if function calling was used).
 */
data class ResponseMessage(
    val role: String, // "assistant"
    val content: String?,
    @SerializedName("reasoning") val reasoning: String? = null, // Цепочка размышлений (если модель её возвращает)
    @SerializedName("tool_calls") val toolCalls: List<ToolCall>? = null
)


/**
 * Token usage statistics.
 *
 * @param promptTokens number of tokens in the request.
 * @param completionTokens number of tokens in the response.
 * @param totalTokens total number of tokens.
 * @param promptTokensDetails details for request tokens (cache, audio, video).
 * @param completionTokensDetails details for response tokens (reasoning, audio, images).
 * @param cost cost in credits (0 for free models).
 */
data class Usage(
    @SerializedName("prompt_tokens") val promptTokens: Int,
    @SerializedName("completion_tokens") val completionTokens: Int,
    @SerializedName("total_tokens") val totalTokens: Int,
    @SerializedName("prompt_tokens_details") val promptTokensDetails: PromptTokensDetails? = null,
    @SerializedName("completion_tokens_details") val completionTokensDetails: CompletionTokensDetails? = null,
    val cost: Double? = null // Стоимость в кредитах (для бесплатной модели будет 0)
)

/**
 * Request token usage details.
 *
 * @param cachedTokens number of tokens from cache.
 * @param cacheWriteTokens number of tokens written to cache.
 * @param audioTokens number of audio tokens.
 * @param videoTokens number of video tokens.
 */
data class PromptTokensDetails(
    @SerializedName("cached_tokens") val cachedTokens: Int? = null,
    @SerializedName("cache_write_tokens") val cacheWriteTokens: Int? = null,
    @SerializedName("audio_tokens") val audioTokens: Int? = null,
    @SerializedName("video_tokens") val videoTokens: Int? = null
)

/**
 * Response token usage details.
 *
 * @param reasoningTokens number of tokens used for reasoning.
 * @param audioTokens number of audio tokens.
 * @param imageTokens number of image tokens.
 */
data class CompletionTokensDetails(
    @SerializedName("reasoning_tokens") val reasoningTokens: Int? = null,
    @SerializedName("audio_tokens") val audioTokens: Int? = null,
    @SerializedName("image_tokens") val imageTokens: Int? = null
)

/**
 * Response option for streaming.
 *
 * @param index option index.
 * @param delta partial response content.
 * @param finishReason completion reason (if response is complete).
 */
data class StreamingChoice(
    val index: Int,
    val delta: Delta,
    @SerializedName("finish_reason") val finishReason: String? = null
)

/**
 * Partial response content for streaming.
 *
 * @param role sender role ("assistant").
 * @param content partial text content.
 * @param reasoning partial reasoning chain.
 * @param toolCalls tool calls (if used).
 */
data class Delta(
    val role: String? = null,
    val content: String? = null,
    val reasoning: String? = null,
    @SerializedName("tool_calls") val toolCalls: List<ToolCall>? = null
)
