package io.github.vladchenko.weatherforecast.core.data.mapper

import io.github.vladchenko.weatherforecast.core.domain.model.MarkdownSegment
import javax.inject.Singleton

/**
 * Parses raw markdown text into a list of [MarkdownSegment].
 * Stateless utility class, provided as a singleton via Hilt.
 *
 * @see io.github.vladchenko.weatherforecast.core.ui.utils.MarkdownInlineParser
 */
@Singleton
class MarkdownParser {

    /**
     * Parses a markdown string into structured [MarkdownSegment] list.
     */
    fun parse(markdown: String): List<MarkdownSegment> {
        if (markdown.isBlank()) return emptyList()

        val segments = mutableListOf<MarkdownSegment>()
        val lines = markdown.lines()

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()

            when {
                // Code block (```...)
                line.startsWith("```") -> {
                    val codeContent = mutableListOf<String>()
                    i++
                    while (i < lines.size && !lines[i].trim().startsWith("```")) {
                        codeContent.add(lines[i])
                        i++
                    }
                    if (i < lines.size) i++ // skip closing ```
                    segments.add(MarkdownSegment.CodeBlock(codeContent.joinToString("\n")))
                }

                // Heading
                line.startsWith("#") -> {
                    val level = line.takeWhile { it == '#' }.length
                    val content = line.substring(level).trim()
                    segments.add(MarkdownSegment.Heading(level, content))
                    i++
                }

                // Unordered list
                line.startsWith("- ") || line.startsWith("* ") -> {
                    val items = mutableListOf<String>()
                    while (i < lines.size && (lines[i].trim().startsWith("- ") || lines[i].trim().startsWith("* "))) {
                        val itemText = lines[i].trim().substring(2).trim()
                        items.add(itemText)
                        i++
                    }
                    segments.add(MarkdownSegment.UnorderedList(items))
                    continue
                }

                // Ordered list
                line.matches(Regex("^\\d+\\..*")) -> {
                    val items = mutableListOf<String>()
                    while (i < lines.size && lines[i].trim().matches(Regex("^\\d+\\..*"))) {
                        val itemContent = lines[i].trim().substringAfter('.', "").trim()
                        items.add(itemContent)
                        i++
                    }
                    segments.add(MarkdownSegment.OrderedList(items))
                    continue
                }

                // Empty line
                line.isEmpty() -> {
                    i++
                    continue
                }

                // Paragraph (collect consecutive non-empty lines)
                else -> {
                    val paragraphLines = mutableListOf(line)
                    while (i + 1 < lines.size && lines[i + 1].trim().isNotEmpty() &&
                        !lines[i + 1].trim().startsWith("#") &&
                        !lines[i + 1].trim().startsWith("- ") &&
                        !lines[i + 1].trim().startsWith("* ") &&
                        !lines[i + 1].trim().matches(Regex("^\\d+\\..*"))) {
                        i++
                        paragraphLines.add(lines[i].trim())
                    }
                    segments.add(MarkdownSegment.Paragraph(paragraphLines.joinToString(" ")))
                }
            }

            i++
        }

        return segments
    }
}
