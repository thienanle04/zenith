package com.zenith.domain.portfolio

import com.zenith.domain.money.AMOUNT_SCALE
import com.zenith.domain.money.Exact
import com.zenith.domain.money.PRECISION_LIMIT
import com.zenith.domain.money.fromQuanta
import com.zenith.domain.money.minorDigits
import com.zenith.domain.money.roundDigits
import com.zenith.domain.money.startingPrecision
import com.zenith.domain.money.toQuanta

/** Reads an old book whose amounts are still minor units, then stores quanta. */
fun Portfolio.withCurrentAmountScale(): Portfolio {
    if (amountScale == AMOUNT_SCALE) {
        return this
    }
    val currencies = accounts.associate { it.id to it.currency }
    val quanta =
        movements.map { movement ->
            val digits = AMOUNT_SCALE - minorDigits(currencies[movement.accountId] ?: "USD")
            val factor = Exact.parse("1" + "0".repeat(digits))
            val scaled = (Exact.fromLong(movement.amountMinor) * factor).roundToIntChecked()
            movement.copy(amountMinor = scaled)
        }
    return copy(movements = quanta, amountScale = AMOUNT_SCALE)
}

internal fun amountPrecision(
    book: Portfolio,
    currency: String,
): Int = (book.amountPrecisions[currency] ?: startingPrecision(currency)).coerceIn(0, PRECISION_LIMIT)

internal fun shownAmount(
    book: Portfolio,
    amount: Exact,
    currency: String,
): Exact = roundDigits(amount, amountPrecision(book, currency))

/** The sum is taken first. Only the finished total rounds. */
internal fun shownTotal(
    book: Portfolio,
    amounts: List<Exact>,
    currency: String,
): Exact {
    var sum = Exact.zero
    for (amount in amounts) {
        sum += amount
    }
    return shownAmount(book, sum, currency)
}

internal fun primaryAmount(
    book: Portfolio,
    vnd: Exact,
): Exact {
    val major =
        if (book.primaryCurrency == "VND") {
            vnd
        } else {
            vnd / rate(book, book.primaryCurrency)
        }
    return shownAmount(book, major, book.primaryCurrency)
}

internal fun quantaFor(
    book: Portfolio,
    amount: Exact,
    currency: String,
): Long = toQuanta(amount, amountPrecision(book, currency))

internal fun setPrimaryCurrency(
    book: Portfolio,
    currency: String,
): Portfolio {
    requireCurrency(book, currency)
    return book.copy(primaryCurrency = currency)
}

internal fun setRate(
    book: Portfolio,
    currency: String,
    rate: Exact,
): Portfolio {
    val code = currency.trim().uppercase()
    if (code.isEmpty() || code == "VND") {
        refuse(PortfolioFailure.CurrencyNeedsRate)
    }
    if (!rate.isPositive) {
        refuse(PortfolioFailure.RatePositive)
    }
    return book.copy(rates = book.rates + (code to rate))
}

internal fun setAmountPrecision(
    book: Portfolio,
    currency: String,
    digits: Int,
): Portfolio {
    requireCurrency(book, currency)
    return book.copy(amountPrecisions = book.amountPrecisions + (currency to digits.coerceIn(0, PRECISION_LIMIT)))
}
