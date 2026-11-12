package com.zenith.domain.portfolio

import com.zenith.domain.money.Exact
import com.zenith.domain.money.fromQuanta
import com.zenith.domain.money.minorAmount

private val kindOrder =
    listOf(
        AccountKind.cash,
        AccountKind.bank,
        AccountKind.eWallet,
        AccountKind.creditCard,
        AccountKind.loan,
        AccountKind.receivable,
        AccountKind.stock,
    )

internal fun sections(book: Portfolio): List<PortfolioSection> = mergedSectionOrder(book, presentSectionKeys(book)).map { key -> section(book, key) }

internal fun presentSectionKeys(book: Portfolio): Set<String> {
    val keys = mutableSetOf<String>()
    for (account in book.accounts.filter { !it.setAside }) {
        keys += account.kind.name
    }
    for (holding in book.holdings) {
        val stock = holding.stockAccountId
        keys += if (stock == null) "physical" else shareKey(holding.instrumentId)
    }
    return keys
}

internal fun mergedSectionOrder(
    book: Portfolio,
    present: Set<String>,
): List<String> {
    val defaults = defaultSectionOrder(book, present)
    val saved =
        if (book.sectionOrder.any { it == "assets" || it == "liabilities" || it.startsWith("shares:") }) {
            emptyList()
        } else {
            book.sectionOrder
        }
    if (saved.none { it in present }) {
        return defaults
    }
    val result = saved.filter { it in present }.toMutableList()
    for (key in defaults) {
        if (key !in result) result += key
    }
    return result
}

internal fun section(
    book: Portfolio,
    key: String,
): PortfolioSection {
    val kind = kindOrder.firstOrNull { it.name == key }
    return if (kind != null) {
        val accounts = book.accounts.filter { !it.setAside && it.kind == kind }
        val ordered = orderedAccounts(book, kind, accounts)
        val vnd = vndTotal(ordered.map { accountVnd(book, it) })
        PortfolioSection(
            key = key,
            kind = kind.toSectionKind(),
            rows =
                ordered.map { account ->
                    val major = fromQuanta(balance(book, account.id))
                    PortfolioRow.account(
                        account.id,
                        account.name,
                        minorAmount(major, account.currency),
                        account.currency,
                        stock = account.kind == AccountKind.stock,
                    ).copy(stored = major)
                },
            subtotalMinor = vnd.roundToIntChecked(),
            subtotalExact = primaryAmount(book, vnd),
        )
    } else if (key == "physical") {
        val holdings = book.holdings.filter { it.stockAccountId == null }
        val rows = holdingRows(book, key, holdings) { instrument, _, _ -> instrument.name }
        val vnd = vndTotal(holdings.map { holdingVnd(book, it) })
        PortfolioSection(
            key = key,
            kind = SectionKind.physical,
            rows = rows,
            subtotalMinor = vnd.roundToIntChecked(),
            subtotalExact = primaryAmount(book, vnd),
        )
    } else {
        val instrumentId = key.removePrefix("share:")
        val instrument = instrument(book, instrumentId)
        val holdings = book.holdings.filter { it.instrumentId == instrumentId && it.stockAccountId != null }
        val rows = shareRows(book, key, holdings)
        PortfolioSection(
            key = key,
            kind = SectionKind.shares,
            heading = instrument.name,
            rows = rows,
            subtotalQuantity = rows.fold(Exact.zero) { sum, row -> sum + (row.quantity ?: Exact.zero) },
            subtotalUnit = instrument.unit,
        )
    }
}

private fun defaultSectionOrder(
    book: Portfolio,
    present: Set<String>,
): List<String> {
    val keys = mutableListOf<String>()
    for (kind in kindOrder) {
        val key = kind.name
        if (key in present) keys += key
    }
    val shares =
        book.holdings
            .filter { it.stockAccountId != null && shareKey(it.instrumentId) in present }
            .groupBy { it.instrumentId }
            .entries
            .sortedBy { (_, holdings) -> holdings.minOf { it.createdOrder } }
    for ((instrumentId, _) in shares) {
        keys += shareKey(instrumentId)
    }
    if ("physical" in present) keys += "physical"
    return keys
}

private fun orderedAccounts(
    book: Portfolio,
    kind: AccountKind,
    accounts: List<AccountRecord>,
): List<AccountRecord> {
    val saved = book.rowOrder[kind.name]
    val assets = book.rowOrder["assets"]
    val liabilities = book.rowOrder["liabilities"]
    val ranked =
        if (saved != null) {
            saved
        } else if (assets == null && liabilities == null) {
            emptyList()
        } else {
            val assetIds = assets.orEmpty()
            val liabilityIds = liabilities.orEmpty()
            val fromAssets =
                order(
                    accounts.filter { it.id in assetIds },
                    assetIds,
                    { it.id },
                    { it.createdOrder },
                )
            val fromLiabilities =
                order(
                    accounts.filter { it.id in liabilityIds && it.id !in assetIds },
                    liabilityIds,
                    { it.id },
                    { it.createdOrder },
                )
            (fromAssets + fromLiabilities).map { it.id }
        }
    return order(accounts, ranked, { it.id }, { it.createdOrder })
}

private fun shareRows(
    book: Portfolio,
    key: String,
    holdings: List<HoldingRecord>,
): List<PortfolioRow> {
    val ordered = order(holdings, book.rowOrder[key].orEmpty(), { it.id }, { stockCreated(book, it) })
    return ordered.map { holding ->
        val instrument = instrument(book, holding.instrumentId)
        val quantity = quantity(book, holding.id)
        PortfolioRow(
            id = holding.id,
            kind = RowKind.holding,
            title = account(book, holding.stockAccountId ?: refuse(PortfolioFailure.StockAccountRequired)).name,
            minor = null,
            currency = null,
            quantity = quantity,
            unit = instrument.unit,
            value = quantity * instrument.price,
            valueCurrency = instrument.priceCurrency,
        )
    }
}

private fun holdingRows(
    book: Portfolio,
    key: String,
    holdings: List<HoldingRecord>,
    title: (InstrumentRecord, HoldingRecord, Exact) -> String,
): List<PortfolioRow> {
    val ordered = order(holdings, book.rowOrder[key].orEmpty(), { it.id }, { it.createdOrder })
    return ordered.map { holding ->
        val instrument = instrument(book, holding.instrumentId)
        val quantity = quantity(book, holding.id)
        PortfolioRow(
            id = holding.id,
            kind = RowKind.holding,
            title = title(instrument, holding, quantity),
            minor = null,
            currency = null,
            quantity = quantity,
            unit = instrument.unit,
            value = quantity * instrument.price,
            valueCurrency = instrument.priceCurrency,
        )
    }
}

private fun stockCreated(
    book: Portfolio,
    holding: HoldingRecord,
): Int = account(book, holding.stockAccountId ?: refuse(PortfolioFailure.StockAccountRequired)).createdOrder

private fun shareKey(instrumentId: String) = "share:$instrumentId"

private fun AccountKind.toSectionKind(): SectionKind =
    when (this) {
        AccountKind.cash -> SectionKind.cash
        AccountKind.bank -> SectionKind.bank
        AccountKind.eWallet -> SectionKind.eWallet
        AccountKind.creditCard -> SectionKind.creditCard
        AccountKind.loan -> SectionKind.loan
        AccountKind.receivable -> SectionKind.receivable
        AccountKind.stock -> SectionKind.stock
    }
