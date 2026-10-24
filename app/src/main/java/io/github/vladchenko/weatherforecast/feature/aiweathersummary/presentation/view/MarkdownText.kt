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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vladchenko.weatherforecast.core.domain.model.MarkdownSegment
import io.github.vladchenko.weatherforecast.core.ui.utils.MarkdownInlineParser

/**
 * Markdown renderer for Jetpack Compose using AnnotatedString.
 * Supports: headings (#-######), bold (**text**), italic (*text*), 
 * links [text](url), unordered lists (- item), ordered lists (1. item), 
 * code blocks (```), inline code (`code`)
 */
@Composable
fun MarkdownText(
    segments: List<MarkdownSegment>,
    modifier: Modifier = Modifier,
    lineHeight: TextUnit = 24.sp,
    color: Color? = null,
) {
    val resolvedColor = color ?: MaterialTheme.colorScheme.onSurface
    
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
                        text = MarkdownInlineParser.parse(segment.content),
                        style = textStyle,
                        color = resolvedColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
                
                is MarkdownSegment.Paragraph -> {
                    Text(
                        text = MarkdownInlineParser.parse(segment.content),
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
                                text = buildAnnotatedString {
                                    append("\u2022 ")
                                    append(MarkdownInlineParser.parse(item))
                                },
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
                        segment.items.forEachIndexed { index, item ->
                            Text(
                                text = buildAnnotatedString {
                                    append("${index + 1}. ")
                                    append(MarkdownInlineParser.parse(item))
                                },
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
