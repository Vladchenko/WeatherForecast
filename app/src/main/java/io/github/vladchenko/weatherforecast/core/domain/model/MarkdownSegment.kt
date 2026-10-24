package io.github.vladchenko.weatherforecast.core.domain.model

/**
 * Sealed interface representing a parsed segment of markdown content.
 * Used as a domain model between the data layer (parser) and presentation layer (renderer).
 */
sealed interface MarkdownSegment {
    data class Heading(val level: Int, val content: String) : MarkdownSegment
    data class Paragraph(val content: String) : MarkdownSegment
    data class UnorderedList(val items: List<String>) : MarkdownSegment
    data class OrderedList(val items: List<String>) : MarkdownSegment
    data class CodeBlock(val content: String) : MarkdownSegment
    data object Divider : MarkdownSegment
}
