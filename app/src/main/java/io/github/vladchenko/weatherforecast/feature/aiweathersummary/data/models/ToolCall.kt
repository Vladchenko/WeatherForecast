package io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models

/**
 * Tool call (Function Calling).
 *
 * Used in AI model requests and responses to invoke external functions.
 *
 * @param id call identifier.
 * @param type call type (default: "function").
 * @param function called function definition.
 */
data class ToolCall(
    val id: String,
    val type: String = "function",
    val function: FunctionCall
)

/**
 * Called function definition.
 *
 * @param name function name.
 * @param arguments arguments in JSON string format.
 */
data class FunctionCall(
    val name: String,
    val arguments: String // JSON-строка
)
