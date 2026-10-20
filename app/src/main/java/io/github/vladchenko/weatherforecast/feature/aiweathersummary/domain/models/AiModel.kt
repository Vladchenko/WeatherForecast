package io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.models

/**
 * Available AI models for generating weather forecast summaries.
 *
 * @param id model identifier for the API.
 * @param displayName display name of the model.
 * @param provider model provider.
 */
sealed class AiModel(
    val id: String,
    val displayName: String,
    val provider: String
) {
    data object Gemma4 : AiModel(
        id = "google/gemma-4-31b-it:free",
        displayName = "Gemma 4 31B",
        provider = "Google"
    )

    data object MiniMax : AiModel(
        id = "minimax/minimax-m3.1-flash:free",
        displayName = "MiniMax M3.1 Flash",
        provider = "MiniMax"
    )

    data object Qwen : AiModel(
        id = "qwen/qwen-2.5-vl-72b-instruct:free",
        displayName = "Qwen 2.5 VL 72B",
        provider = "Alibaba"
    )

    data object Flint : AiModel(
        id = "recursive/flint-v1.0:free",
        displayName = "Recursive: Flint",
        provider = "Recursive"
    )

    companion object {
        /**
         * List of all available models.
         */
        val all = listOf(Gemma4, MiniMax, Qwen, Flint)

        /**
         * Default model.
         */
        val default = Flint
    }
}
