package com.example.weatherforecast.presentation

import com.example.weatherforecast.presentation.SubtitleSize.Companion.fromSubtitle
import com.example.weatherforecast.presentation.SubtitleSize.Large
import com.example.weatherforecast.presentation.SubtitleSize.Normal
import com.example.weatherforecast.presentation.SubtitleSize.Small


/**
 * Represents the logical size of a subtitle text, independent of platform-specific units.
 *
 * This enum is used to decouple UI logic (e.g., determining font size in `sp`) from business or presentation logic,
 * allowing components like [AppBarStateConverter] to remain free of Android/Compose dependencies.
 *
 * The size is determined based on the length of the subtitle text:
 * - [Small]: for long subtitles (more than 50 characters)
 * - [Normal]: for medium-length subtitles (31–50 characters)
 * - [Large]: for short subtitles (30 characters or fewer)
 *
 * @see fromSubtitle — companion function to derive size from actual text
 */
enum class SubtitleSize {
    /** Small font size, intended for long subtitles that may need to fit in limited space. */
    Small,

    /** Normal font size, for moderately long subtitles. */
    Normal,

    /** Large font size, suitable for short and prominent subtitles. */
    Large;

    /**
     * Determines the appropriate [SubtitleSize] based on the length of the given subtitle string.
     *
     * @param subtitle the subtitle text to evaluate
     * @return the corresponding [SubtitleSize]
     */
    companion object {
        fun fromSubtitle(subtitle: String): SubtitleSize {
            return when {
                subtitle.length > 50 -> Small
                subtitle.length > 30 -> Normal
                else -> Large
            }
        }
    }
}