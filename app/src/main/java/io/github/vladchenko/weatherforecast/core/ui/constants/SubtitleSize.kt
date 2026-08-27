package io.github.vladchenko.weatherforecast.core.ui.constants

import io.github.vladchenko.weatherforecast.core.ui.constants.SubtitleSize.Large
import io.github.vladchenko.weatherforecast.core.ui.constants.SubtitleSize.Normal
import io.github.vladchenko.weatherforecast.core.ui.constants.SubtitleSize.Small

/**
 * Represents the logical size of a subtitle text, independent of platform-specific units.
 *
 * This enum is used to decouple UI logic (e.g., determining font size in `sp`) from business or presentation logic.
 *
 * The size is determined based on the length of the subtitle text:
 * - [Small]: for long subtitles (more than 50 characters)
 * - [Normal]: for medium-length subtitles (31–50 characters)
 * - [Large]: for short subtitles (30 characters or fewer)
 */
enum class SubtitleSize {
    /** Small font size, intended for long subtitles that may need to fit in limited space. */
    Small,

    /** Normal font size, for moderately long subtitles. */
    Normal,

    /** Large font size, suitable for short and prominent subtitles. */
    Large;

    companion object {
        /**
         * Determines the appropriate [SubtitleSize] based on the length of the given subtitle string.
         *
         * @param subtitle the subtitle text to evaluate
         * @return the corresponding [SubtitleSize]
         */
        fun fromSubtitle(subtitle: String): SubtitleSize {
            return when {
                subtitle.length > 50 -> Small
                subtitle.length > 30 -> Normal
                else -> Large
            }
        }
    }
}