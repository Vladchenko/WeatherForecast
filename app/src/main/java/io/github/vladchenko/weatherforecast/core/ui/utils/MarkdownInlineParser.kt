package io.github.vladchenko.weatherforecast.core.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

/**
 * Utility for parsing inline markdown formatting to [AnnotatedString].
 * Supports: bold (**text**), italic (*text*), links [text](url), inline code (`code`).
 */
object MarkdownInlineParser {

    /**
     * Parses inline markdown formatting in [text] and returns an [AnnotatedString]
     * with appropriate SpanStyles for bold, italic, links, and inline code.
     */
    fun parse(text: String): AnnotatedString {
        return buildAnnotatedString {
            var index = 0

            while (index < text.length) {
                when {
                    // Bold: **text**
                    text.startsWith("**", index) -> {
                        val end = text.indexOf("**", startIndex = index + 2)

                        if (end != -1 && end > index + 2) {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(text.substring(index + 2, end))
                            }
                            index = end + 2
                        } else {
                            append("**")
                            index += 2
                        }
                    }

                    // Inline code: `code`
                    text[index] == '`' -> {
                        val end = text.indexOf('`', startIndex = index + 1)

                        if (end != -1 && end > index + 1) {
                            withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                                append(text.substring(index + 1, end))
                            }
                            index = end + 1
                        } else {
                            append(text[index])
                            index++
                        }
                    }

                    // Link: [text](url)
                    text[index] == '[' -> {
                        val textEnd = text.indexOf(']', startIndex = index + 1)
                        val urlStart = textEnd + 1

                        if (
                            textEnd != -1 &&
                            urlStart < text.length &&
                            text[urlStart] == '('
                        ) {
                            val urlEnd = text.indexOf(')', startIndex = urlStart + 1)

                            if (urlEnd != -1) {
                                val linkText = text.substring(index + 1, textEnd)
                                val url = text.substring(urlStart + 1, urlEnd)

                                pushStringAnnotation(tag = "URL", annotation = url)
                                withStyle(
                                    SpanStyle(
                                        color = Color.Blue,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ) {
                                    append(linkText)
                                }
                                pop()

                                index = urlEnd + 1
                            } else {
                                append(text[index])
                                index++
                            }
                        } else {
                            append(text[index])
                            index++
                        }
                    }

                    // Italic: *text*
                    text[index] == '*' -> {
                        val end = text.indexOf('*', startIndex = index + 1)

                        if (end != -1 && end > index + 1) {
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append(text.substring(index + 1, end))
                            }
                            index = end + 1
                        } else {
                            append(text[index])
                            index++
                        }
                    }

                    else -> {
                        append(text[index])
                        index++
                    }
                }
            }
        }
    }
}
