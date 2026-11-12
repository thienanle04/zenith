package com.zenith.app.core.utils

import com.zenith.domain.money.Exact
import com.zenith.domain.money.abs
import com.zenith.domain.money.majorAmount
import com.zenith.domain.money.roundDigits
import com.zenith.domain.portfolio.SeparatorPair

fun formatMajor(
    amount: Exact,
    separators: SeparatorPair,
): String {
    val plain = amount.abs().formatPlain()
    val parts = plain.split('.')
    val grouped = group(parts[0], if (separators == SeparatorPair.commaPoint) "," else ".")
    if (parts.size == 1) return grouped
    val decimal = if (separators == SeparatorPair.commaPoint) "." else ","
    return grouped + decimal + parts[1]
}

fun moneyPrefix(currency: String): String =
    when (currency) {
        "VND" -> ""
        "USD" -> "$ "
        "EUR" -> "€ "
        "GBP" -> "£ "
        "JPY" -> "¥ "
        else -> "$currency "
    }

fun moneySuffix(currency: String): String = if (currency == "VND") "đ" else ""

fun formatMoney(
    amount: Exact,
    currency: String,
    separators: SeparatorPair,
): String = moneyPrefix(currency) + formatMajor(amount, separators) + moneySuffix(currency)

/** Digits and one decimal mark, in the portfolio's separators, with leading zeros removed. */
fun canonicalAmount(
    input: String,
    separators: SeparatorPair,
): String {
    val digits = amountDigits(input, separators)
    if (digits.isEmpty()) return ""
    val decimal = separatorChars(separators).first
    val parts = digits.split(decimal, limit = 2)
    val whole = parts[0].trimStart('0').ifEmpty { "0" }
    return if (parts.size == 1) whole else whole + decimal + parts[1]
}

/** Grouped amount for display. An empty input stays empty so no suffix is shown alone. */
fun groupedAmount(
    input: String,
    separators: SeparatorPair,
): String {
    val digits = amountDigits(input, separators)
    if (digits.isEmpty()) return ""
    val (decimal, grouping) = separatorChars(separators)
    val parts = digits.split(decimal, limit = 2)
    val grouped = group(parts[0], grouping.toString())
    return if (parts.size == 1) grouped else grouped + decimal + parts[1]
}

private fun amountDigits(
    input: String,
    separators: SeparatorPair,
): String {
    val decimal = separatorChars(separators).first
    val kept = StringBuilder()
    var seenDecimal = false
    for (char in input) {
        when {
            char.isDigit() -> kept.append(char)
            char == decimal && !seenDecimal -> {
                seenDecimal = true
                kept.append(char)
            }
        }
    }
    return kept.toString()
}

private fun separatorChars(separators: SeparatorPair): Pair<Char, Char> = if (separators == SeparatorPair.commaPoint) '.' to ',' else ',' to '.'

fun formatShown(
    amount: Exact,
    currency: String,
    separators: SeparatorPair,
    precision: Int,
): String {
    val rounded = roundDigits(amount, precision)
    val text = formatMoney(rounded.abs(), currency, separators)
    return if (rounded.isNegative) "-$text" else text
}

fun formatMinor(
    minor: Long,
    currency: String,
    separators: SeparatorPair,
): String = formatMoney(majorAmount(minor, currency), currency, separators)

fun formatQuantity(
    quantity: Exact,
    unit: String,
    separators: SeparatorPair,
): String = formatMajor(quantity, separators) + " " + unit

private fun group(
    digits: String,
    separator: String,
): String =
    buildString {
        digits.forEachIndexed { index, char ->
            if (index > 0 && (digits.length - index) % 3 == 0) append(separator)
            append(char)
        }
    }
