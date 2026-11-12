package com.zenith.domain.portfolio

import com.zenith.domain.money.Exact
import com.zenith.domain.money.fromQuanta

internal fun accountVnd(
    book: Portfolio,
    account: AccountRecord,
): Exact {
    val minor = if (account.setAside) 0L else balance(book, account.id)
    return if (minor == 0L) {
        Exact.zero
    } else {
        fromQuanta(minor) * rate(book, account.currency)
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

internal fun vndTotal(amounts: List<Exact>): Exact {
    var sum = Exact.zero
    for (amount in amounts) sum += amount
    return sum
}

internal fun vndSum(amounts: List<Exact>): Long = vndTotal(amounts).roundToIntChecked()

internal fun signedVnd(
    book: Portfolio,
    positive: Boolean,
): Exact {
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
    return sum
}

internal fun sumSide(
    book: Portfolio,
    positive: Boolean,
): Long = signedVnd(book, positive).roundToIntChecked()
