package com.zenith.app.core.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.zenith.app.core.utils.groupedAmount
import com.zenith.domain.portfolio.SeparatorPair

private fun decimalChar(separators: SeparatorPair): Char = if (separators == SeparatorPair.commaPoint) '.' else ','

/** Groups digits and draws a prefix and suffix without putting them in the stored text. */
class AmountTransformation(
    private val separators: SeparatorPair,
    private val prefix: String,
    private val suffix: String,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val grouped = groupedAmount(text.text, separators)
        if (grouped.isEmpty()) {
            return TransformedText(AnnotatedString(""), emptyMapping)
        }
        val display = prefix + grouped + suffix
        val mapping =
            AmountOffsetMapping(
                original = text.text,
                grouped = grouped,
                prefixLength = prefix.length,
                decimal = decimalChar(separators),
            )
        return TransformedText(AnnotatedString(display), mapping)
    }
}

private val emptyMapping =
    object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int = 0

        override fun transformedToOriginal(offset: Int): Int = 0
    }

private class AmountOffsetMapping(
    original: String,
    grouped: String,
    private val prefixLength: Int,
    decimal: Char,
) : OffsetMapping {
    private val originalMarks = markIndexes(original, decimal)
    private val groupedMarks = markIndexes(grouped, decimal)
    private val groupedLength = grouped.length

    override fun originalToTransformed(offset: Int): Int {
        val marksBefore = originalMarks.count { it < offset.coerceAtLeast(0) }
        if (marksBefore == 0 || groupedMarks.isEmpty()) return prefixLength
        val mark = groupedMarks[(marksBefore - 1).coerceAtMost(groupedMarks.lastIndex)]
        return prefixLength + mark + 1
    }

    override fun transformedToOriginal(offset: Int): Int {
        if (originalMarks.isEmpty()) return 0
        val numberOffset = (offset - prefixLength).coerceIn(0, groupedLength)
        val marksBefore = groupedMarks.count { it < numberOffset }
        if (marksBefore == 0) return 0
        val mark = originalMarks[(marksBefore - 1).coerceAtMost(originalMarks.lastIndex)]
        return mark + 1
    }
}

private fun markIndexes(
    text: String,
    decimal: Char,
): List<Int> {
    val indexes = mutableListOf<Int>()
    var seenDecimal = false
    text.forEachIndexed { index, char ->
        when {
            char.isDigit() -> indexes += index
            char == decimal && !seenDecimal -> {
                seenDecimal = true
                indexes += index
            }
        }
    }
    return indexes
}
