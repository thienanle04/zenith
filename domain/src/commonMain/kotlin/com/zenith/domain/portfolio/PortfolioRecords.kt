package com.zenith.domain.portfolio

import com.zenith.domain.money.Exact

internal fun account(
    book: Portfolio,
    id: String,
): AccountRecord = book.accounts.firstOrNull { it.id == id } ?: refuse(PortfolioFailure.NotFound)

internal fun holding(
    book: Portfolio,
    id: String,
): HoldingRecord = book.holdings.firstOrNull { it.id == id } ?: refuse(PortfolioFailure.NotFound)

internal fun instrument(
    book: Portfolio,
    id: String,
): InstrumentRecord = book.instruments.firstOrNull { it.id == id } ?: refuse(PortfolioFailure.NotFound)

internal fun id(book: Portfolio): String = "${book.nextId}"

internal fun balance(
    book: Portfolio,
    id: String,
): Long = book.movements.filter { it.accountId == id }.fold(0L) { sum, movement -> sum + movement.amountMinor }

internal fun quantity(
    book: Portfolio,
    id: String,
): Exact = book.adjustments.filter { it.holdingId == id }.fold(Exact.zero) { sum, adjustment -> sum + adjustment.quantity }

internal fun rate(
    book: Portfolio,
    currency: String,
): Exact {
    if (currency == "VND") return Exact.one
    return book.rates[currency] ?: refuse(PortfolioFailure.CurrencyNeedsRate)
}

internal fun currencies(book: Portfolio): List<String> = listOf("VND") + book.rates.keys

internal fun requireCurrency(
    book: Portfolio,
    currency: String,
) {
    if (currency != "VND" && currency !in book.rates) {
        refuse(PortfolioFailure.CurrencyNeedsRate)
    }
}

internal fun requireName(name: String) {
    if (name.trim().isEmpty()) {
        refuse(PortfolioFailure.NameRequired)
    }
}

internal fun parseAmount(
    book: Portfolio,
    input: String,
): Exact {
    val grouping = if (book.separators == SeparatorPair.commaPoint) "," else "."
    val decimal = if (book.separators == SeparatorPair.commaPoint) "." else ","
    val normalized = input.trim().replace(grouping, "").replace(decimal, ".")
    return try {
        Exact.parse(normalized)
    } catch (_: IllegalArgumentException) {
        refuse(PortfolioFailure.InvalidAmount)
    }
}
