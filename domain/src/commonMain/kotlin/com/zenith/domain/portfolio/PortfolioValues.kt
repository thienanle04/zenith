package com.zenith.domain.portfolio

import com.zenith.domain.money.Exact
import com.zenith.domain.money.majorAmount

internal fun accountVnd(
    book: Portfolio,
    account: AccountRecord,
): Exact {
    val minor = if (account.setAside) 0L else balance(book, account.id)
    return if (minor == 0L) {
        Exact.zero
    } else {
        majorAmount(minor, account.currency) * rate(book, account.currency)
    }
}

internal fun holdingVnd(
    book: Portfolio,
    holding: HoldingRecord,
): Exact {
    val instrument = instrument(book, holding.instrumentId)
    val value = quantity(book, holding.id) * instrument.price
    if (value.isZero) return Exact.zero
    return value * rate(book, instrument.priceCurrency)
}

internal fun vndSum(amounts: List<Exact>): Long {
    var sum = Exact.zero
    for (amount in amounts) sum += amount
    return sum.roundToIntChecked()
}

internal fun sumSide(
    book: Portfolio,
    positive: Boolean,
): Long {
    var sum = Exact.zero
    for (account in book.accounts) {
        val contribution = accountVnd(book, account)
        if (positive && contribution.isPositive) sum += contribution
        if (!positive && contribution.isNegative) sum += contribution
    }
    for (holding in book.holdings) {
        val contribution = holdingVnd(book, holding)
        if (positive && contribution.isPositive) sum += contribution
        if (!positive && contribution.isNegative) sum += contribution
    }
    return sum.roundToIntChecked()
}
