package com.zenith.domain.portfolio

import com.zenith.domain.money.Exact
import com.zenith.domain.money.majorAmount
import com.zenith.domain.money.minorAmount

/** The balance a currency change would produce, in the new currency's major units. */
internal fun proposedBalance(
    book: Portfolio,
    id: String,
    newRate: Exact,
): Exact {
    val account = account(book, id)
    if (newRate.isZero || newRate.isNegative) {
        refuse(PortfolioFailure.RatePositive)
    }
    val minor = balance(book, id)
    if (minor == 0L) {
        return Exact.zero
    }
    val vnd = majorAmount(minor, account.currency) * rate(book, account.currency)
    return vnd / newRate
}

internal fun addAccount(
    book: Portfolio,
    name: String,
    kind: AccountKind,
    currency: String,
): Portfolio {
    requireName(name)
    requireCurrency(book, currency)
    val id = id(book)
    return book.copy(
        accounts =
            book.accounts +
                AccountRecord(
                    id = id,
                    name = name.trim(),
                    kind = kind,
                    currency = currency,
                    createdOrder = book.nextId,
                ),
        nextId = book.nextId + 1,
    )
}

internal fun saveAccount(
    book: Portfolio,
    saved: SavedAccount,
): Portfolio {
    val id = saved.id
    val name = saved.name
    val kind = saved.kind
    val currency = saved.currency
    val balance = saved.balance
    val rate = saved.rate
    requireName(name)
    requireCurrency(book, currency)
    val account = account(book, id)
    if (account.kind == AccountKind.stock && kind != AccountKind.stock) {
        refuse(PortfolioFailure.StockKindFixed)
    }
    if (account.kind != AccountKind.stock && kind == AccountKind.stock) {
        refuse(PortfolioFailure.StockKindFixed)
    }
    var next = book
    next =
        if (currency != account.currency) {
            val appliedRate = rate ?: rate(book, currency)
            if (appliedRate.isZero || appliedRate.isNegative) {
                refuse(PortfolioFailure.RatePositive)
            }
            rescale(next, account, currency, balance)
        } else {
            setBalance(next, id, minorAmount(balance, currency))
        }
    val updated =
        next.copy(
            accounts =
                next.accounts.map { item ->
                    if (item.id == id) item.copy(name = name.trim(), kind = kind, currency = currency) else item
                },
        )
    return if (account.kind == kind) updated else appendToGroup(updated, id, kind)
}

internal fun setAside(
    book: Portfolio,
    id: String,
): Portfolio {
    val account = account(book, id)
    val removed =
        if (account.kind == AccountKind.stock) {
            book.holdings.filter { it.stockAccountId == id }.fold(book) { current, holding ->
                removeHolding(current, holding.id)
            }
        } else {
            book
        }
    return removed.copy(
        accounts = removed.accounts.map { item -> if (item.id == id) item.copy(setAside = true) else item },
    )
}

private fun setBalance(
    book: Portfolio,
    id: String,
    targetMinor: Long,
): Portfolio {
    val current = balance(book, id)
    if (current == targetMinor) {
        return book
    }
    val account = account(book, id)
    return book.copy(
        movements =
            book.movements +
                MovementRecord(
                    id = id(book),
                    accountId = id,
                    amountMinor = targetMinor - current,
                    vndPerUnit = rate(book, account.currency),
                ),
        nextId = book.nextId + 1,
    )
}

private fun appendToGroup(
    book: Portfolio,
    id: String,
    kind: AccountKind,
): Portfolio {
    val key = kind.name
    val ids = section(book, key).rows.map { it.id }.filter { it != id } + id
    return book.copy(rowOrder = book.rowOrder + (key to ids))
}

private fun rescale(
    book: Portfolio,
    account: AccountRecord,
    currency: String,
    balance: Exact,
): Portfolio {
    val current = balance(book, account.id)
    val target = minorAmount(balance, currency)
    if (current == 0L) {
        return book
    }
    val movements = book.movements.filter { it.accountId == account.id }
    var allocated = 0L
    val rewritten =
        movements.mapIndexed { index, movement ->
            val amount =
                if (index == movements.lastIndex) {
                    target - allocated
                } else {
                    val rounded =
                        (
                            Exact.fromLong(movement.amountMinor) * Exact.fromLong(target) / Exact.fromLong(current)
                        ).roundToIntChecked()
                    allocated += rounded
                    rounded
                }
            MovementRecord(movement.id, movement.accountId, amount, rate(book, currency))
        }
    return book.copy(
        movements =
            book.movements.map { movement ->
                if (movement.accountId == account.id) rewritten.first { it.id == movement.id } else movement
            },
    )
}
