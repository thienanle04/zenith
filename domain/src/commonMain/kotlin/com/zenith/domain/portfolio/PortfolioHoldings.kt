package com.zenith.domain.portfolio

import com.zenith.domain.money.Exact

internal fun addInstrument(
    book: Portfolio,
    created: NewHolding,
): Portfolio {
    val name = created.name
    val unit = created.unit
    val priceCurrency = created.priceCurrency
    val price = created.price
    val quantity = created.quantity
    val stockAccountId = created.stockAccountId
    requireName(name)
    requireName(unit)
    requireCurrency(book, priceCurrency)
    if (stockAccountId != null) {
        val account = account(book, stockAccountId)
        if (account.kind != AccountKind.stock) {
            refuse(PortfolioFailure.StockAccountRequired)
        }
    }
    val instrumentId = id(book)
    val holdingId = "${book.nextId + 1}"
    var next =
        book.copy(
            instruments =
                book.instruments +
                    InstrumentRecord(
                        id = instrumentId,
                        name = name.trim(),
                        unit = unit.trim(),
                        priceCurrency = priceCurrency,
                        price = price,
                    ),
            holdings =
                book.holdings +
                    HoldingRecord(
                        id = holdingId,
                        instrumentId = instrumentId,
                        stockAccountId = stockAccountId,
                        createdOrder = book.nextId + 1,
                    ),
            nextId = book.nextId + 2,
        )
    if (!quantity.isZero) {
        next =
            next.copy(
                adjustments = next.adjustments + AdjustmentRecord(id = id(next), holdingId = holdingId, quantity = quantity),
                nextId = next.nextId + 1,
            )
    }
    return next
}

internal fun saveHolding(
    book: Portfolio,
    holdingId: String,
    name: String,
    price: Exact,
    quantity: Exact,
): Portfolio {
    requireName(name)
    val holding = holding(book, holdingId)
    val instrument = instrument(book, holding.instrumentId)
    val next = setQuantity(book, holdingId, quantity)
    return next.copy(
        instruments =
            next.instruments.map { item ->
                if (item.id == instrument.id) item.copy(name = name.trim(), price = price) else item
            },
    )
}

internal fun removeHolding(
    book: Portfolio,
    holdingId: String,
): Portfolio {
    val holding = holding(book, holdingId)
    val next =
        book.copy(
            holdings = book.holdings.filter { it.id != holdingId },
            adjustments = book.adjustments.filter { it.holdingId != holdingId },
        )
    val stillHeld = next.holdings.any { it.instrumentId == holding.instrumentId }
    return if (stillHeld) next else next.copy(instruments = next.instruments.filter { it.id != holding.instrumentId })
}

internal fun removeInstrument(
    book: Portfolio,
    instrumentId: String,
): Portfolio {
    instrument(book, instrumentId)
    val holdingIds =
        book.holdings
            .filter { it.instrumentId == instrumentId }
            .map { it.id }
            .toSet()
    return book.copy(
        instruments = book.instruments.filter { it.id != instrumentId },
        holdings = book.holdings.filter { it.id !in holdingIds },
        adjustments = book.adjustments.filter { it.holdingId !in holdingIds },
    )
}

private fun setQuantity(
    book: Portfolio,
    holdingId: String,
    target: Exact,
): Portfolio {
    val current = quantity(book, holdingId)
    if (current == target) {
        return book
    }
    return book.copy(
        adjustments = book.adjustments + AdjustmentRecord(id = id(book), holdingId = holdingId, quantity = target - current),
        nextId = book.nextId + 1,
    )
}
