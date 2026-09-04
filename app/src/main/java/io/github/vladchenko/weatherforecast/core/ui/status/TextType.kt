package io.github.vladchenko.weatherforecast.core.ui.status

import androidx.annotation.StringRes

/**
 * Sealed interface representing different types of text content for UI status messages.
 */
sealed interface TextType {
    /**
     * Holds a pre-formatted string value ready for direct display.
     */
    data class Text(val value: String) : TextType

    /**
     * Holds a string resource ID with optional formatting arguments for localized text.
     *
     * @property id string resource ID to be resolved via `stringResource`
     * @property args optional formatting arguments for the string resource
     */
    data class ResId(
        @param:StringRes val id: Int,
        val args: Array<out Any>
    ) : TextType {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ResId

            if (id != other.id) return false
            if (!args.contentEquals(other.args)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + args.contentHashCode()
            return result
        }
    }

    /**
     * Represents an empty/no-op status state used for initial or hidden status.
     * UI components should render nothing when this type is present.
     */
    object Empty : TextType
}