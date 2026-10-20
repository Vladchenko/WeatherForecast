package io.github.vladchenko.weatherforecast.feature.aiweathersummary.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Markdown renderer for Jetpack Compose using AnnotatedString.
 * Supports: headings (#-######), bold (**text**), italic (*text*), 
 * links [text](url), unordered lists (- item), ordered lists (1. item), 
 * code blocks (```), inline code (`code`)
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    lineHeight: TextUnit = 24.sp,
    color: Color? = null,
) {
    val resolvedColor = color ?: MaterialTheme.colorScheme.onSurface
    
    val segments = parseMarkdown(markdown)
    
    Column(modifier = modifier) {
        segments.forEach { segment ->
            when (segment) {
                is MarkdownSegment.Heading -> {
                    val textStyle = when (segment.level) {
                        1 -> MaterialTheme.typography.headlineMedium
                        2 -> MaterialTheme.typography.titleLarge
                        3 -> MaterialTheme.typography.titleMedium
                        else -> MaterialTheme.typography.titleSmall
                    }
                    Text(
                        text = segment.annotatedText,
                        style = textStyle,
                        color = resolvedColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
                
                is MarkdownSegment.Paragraph -> {
                    Text(
                        text = segment.annotatedText,
                        style = LocalTextStyle.current.copy(
                            lineHeight = lineHeight
                        ),
                        color = resolvedColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
                
                is MarkdownSegment.UnorderedList -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        segment.items.forEach { item ->
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyLarge,
                                color = resolvedColor,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
                
                is MarkdownSegment.OrderedList -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        segment.items.forEach { item ->
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyLarge,
                                color = resolvedColor,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
                
                is MarkdownSegment.CodeBlock -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = segment.content,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = resolvedColor
                        )
                    }
                }
                
                is MarkdownSegment.Divider -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        }
    }
}

// MARK: Parser

sealed interface MarkdownSegment {
    val annotatedText: AnnotatedString
    data class Heading(val level: Int, val content: String) : MarkdownSegment {
        override val annotatedText = parseInlineToAnnotatedString(content)
    }
    data class Paragraph(val content: String) : MarkdownSegment {
        override val annotatedText = parseInlineToAnnotatedString(content)
    }
    data class UnorderedList(val items: List<AnnotatedString>) : MarkdownSegment {
        override val annotatedText = AnnotatedString("")
    }
    data class OrderedList(val items: List<AnnotatedString>) : MarkdownSegment {
        override val annotatedText = AnnotatedString("")
    }
    data class CodeBlock(val content: String) : MarkdownSegment {
        override val annotatedText = AnnotatedString(content)
    }
    data object Divider : MarkdownSegment {
        override val annotatedText = AnnotatedString("")
    }
}

private fun parseMarkdown(markdown: String): List<MarkdownSegment> {
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
                val items = mutableListOf<AnnotatedString>()
                while (i < lines.size && (lines[i].trim().startsWith("- ") || lines[i].trim().startsWith("* "))) {
                    val itemText = lines[i].trim().substring(2).trim()
                    val annotatedItem = parseInlineToAnnotatedString(itemText)
                    items.add(
                        buildAnnotatedString {
                            append("• ")
                            append(annotatedItem)
                        }
                    )
                    i++
                }
                segments.add(MarkdownSegment.UnorderedList(items))
                continue
            }
            
            // Ordered list
            line.matches(Regex("^\\d+\\..*")) -> {
                val items = mutableListOf<AnnotatedString>()
                while (i < lines.size && lines[i].trim().matches(Regex("^\\d+\\..*"))) {
                    val itemContent = lines[i].trim().substringAfter('.', "").trim()
                    val annotatedItem = parseInlineToAnnotatedString(itemContent)
                    items.add(
                        buildAnnotatedString {
                            append("${items.size + 1}. ")
                            append(annotatedItem)
                        }
                    )
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
                segments.add(MarkdownSegment.Paragraph(
                    paragraphLines.joinToString(" ")
                ))
            }
        }
        
        i++
    }
    
    return segments
}

/**
 * Parse inline markdown formatting to AnnotatedString with SpanStyles
 */
private fun parseInlineToAnnotatedString(text: String): AnnotatedString {
    return buildAnnotatedString {
        var index = 0

        while (index < text.length) {
            when {
                // Bold: **text**
                text.startsWith("**", index) -> {
                    val end = text.indexOf("**", startIndex = index + 2)

                    if (end != -1 && end > index + 2) {
                        withStyle(
                            SpanStyle(fontWeight = FontWeight.Bold)
                        ) {
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
                        withStyle(
                            SpanStyle(fontFamily = FontFamily.Monospace)
                        ) {
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

                            pushStringAnnotation(
                                tag = "URL",
                                annotation = url
                            )
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
                        withStyle(
                            SpanStyle(fontStyle = FontStyle.Italic)
                        ) {
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

private data class InlineAnnotation(
    val start: Int,
    val end: Int,
    val style: SpanStyle
)
