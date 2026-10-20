package io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models

import com.google.gson.annotations.SerializedName

/**
 * Request object for AI API (OpenRouter).
 *
 * Generic request model that can be used with any AI model supporting the OpenRouter API format.
 * Contains information about the model, messages, generation parameters,
 * and additional settings (tools, reasoning mode, response format).
 *
 * @param model AI model identifier (e.g., "google/gemma-4-31b-it:free", "qwen/qwen-2.5-vl-72b-instruct:free").
 * @param messages list of messages for dialog context.
 * @param stream flag for streaming response (default: `false`).
 * @param maxTokens maximum number of tokens in the response.
 * @param temperature randomness coefficient for generation.
 * @param topP nucleus sampling coefficient.
 * @param stop stop words lists for ending generation.
 * @param reasoning model reasoning mode configuration.
 * @param responseFormat format of the returned response.
 * @param tools list of available tools (function calling).
 * @param toolChoice selection of a specific tool.
 */
data class AISummaryRequest(
    val model: String,
    val messages: List<Message>,
    val stream: Boolean = false,
    @SerializedName("max_tokens") val maxTokens: Int? = null,
    val temperature: Double? = null,
    @SerializedName("top_p") val topP: Double? = null,
    val stop: List<String>? = null,
    // Для управления режимом размышления (если поддерживается провайдером)
    val reasoning: ReasoningConfig? = null,
    @SerializedName("response_format") val responseFormat: ResponseFormat? = null,
    val tools: List<Tool>? = null,
    @SerializedName("tool_choice") val toolChoice: Any? = null // Может быть String или Object
)

/**
 * Message for the AI model.
 *
 * @param role sender role ("system", "user", "assistant").
 * @param content message text or array of parts for multimodal requests.
 * @param name message author name (for system-message).
 * @param toolCalls tool calls (for assistant responses).
 * @param toolCallId tool call identifier (for execution results).
 */
data class Message(
    val role: String, // "system", "user", "assistant"
    val content: Any, // String или List<ContentPart>
    val name: String? = null,
    @SerializedName("tool_calls") val toolCalls: List<ToolCall>? = null,
    @SerializedName("tool_call_id") val toolCallId: String? = null
)

/**
 * Content part for multimodal requests (text + image).
 *
 * @param type part type ("text" or "image_url").
 * @param text text content.
 * @param imageUrl object with image URL or base64 data.
 */
data class ContentPart(
    val type: String, // "text" или "image_url"
    val text: String? = null,
    @SerializedName("image_url") val imageUrl: ImageUrl? = null
)

/**
 * Image URL for multimodal requests.
 *
 * @param url image URL or data URI (data:image/...;base64,...).
 * @param detail detail level ("auto", "low", "high").
 */
data class ImageUrl(
    val url: String, // URL или data:image/...;base64,...
    val detail: String? = null // "auto", "low", "high"
)

/**
 * Model reasoning mode configuration (Reasoning).
 *
 * @param effort reasoning effort level ("low", "medium", "high").
 * @param maxTokens maximum number of tokens for reasoning.
 */
data class ReasoningConfig(
    val effort: String? = null, // "low", "medium", "high"
    val maxTokens: Int? = null
)

/**
 * Format of the returned response.
 *
 * @param type format type ("json_object" or "json_schema").
 */
data class ResponseFormat(
    val type: String // "json_object" или "json_schema"
)

/**
 * Tool for function calling.
 *
 * @param type tool type (default: "function").
 * @param function function definition.
 */
data class Tool(
    val type: String = "function",
    val function: FunctionDefinition
)

/**
 * Function definition for function calling.
 *
 * @param name function name.
 * @param description function description.
 * @param parameters parameter schema in JSON Schema format.
 */
data class FunctionDefinition(
    val name: String,
    val description: String? = null,
    val parameters: Map<String, Any>? = null // JSON Schema
)
