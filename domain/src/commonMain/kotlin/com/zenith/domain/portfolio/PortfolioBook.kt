package com.zenith.domain.portfolio

import com.zenith.domain.money.Exact
import com.zenith.domain.money.majorAmount

enum class SectionKind { cash, bank, eWallet, creditCard, loan, receivable, stock, shares, physical }

enum class RowKind { account, holding }

data class PortfolioView(
    /** Signed VND đồng, already rounded. */
    val assets: Long,
    val liabilities: Long,
    val netWorth: Long,
    val sections: List<PortfolioSection>,
    val separators: SeparatorPair,
    val currencies: List<String>,
    val stockAccounts: List<StockAccountChoice>,
    /** Latest transaction first. */
    val transactions: List<TransactionLine> = emptyList(),
)

/** One stored balance difference or quantity difference. */
data class TransactionLine(
    val id: String,
    val title: String,
    val minor: Long? = null,
    val currency: String? = null,
    val quantity: Exact? = null,
    val unit: String? = null,
)

data class StockAccountChoice(
    val id: String,
    val name: String,
)

data class PortfolioSection(
    val key: String,
    val kind: SectionKind,
    val rows: List<PortfolioRow>,
    val heading: String? = null,
    /** Signed VND đồng for a group or for physical holdings. */
    val subtotalMinor: Long? = null,
    /** Share-section total, in [subtotalUnit]. */
    val subtotalQuantity: Exact? = null,
    val subtotalUnit: String? = null,
)

data class PortfolioRow(
    val id: String,
    val kind: RowKind,
    val title: String,
    val minor: Long?,
    val currency: String?,
    val quantity: Exact?,
    val unit: String?,
    val value: Exact?,
    val valueCurrency: String?,
    val stock: Boolean = false,
) {
    companion object {
        fun account(
            id: String,
            title: String,
            minor: Long,
            currency: String,
            stock: Boolean = false,
        ) = PortfolioRow(
            id = id,
            kind = RowKind.account,
            title = title,
            minor = minor,
            currency = currency,
            quantity = null,
            unit = null,
            value = null,
            valueCurrency = null,
            stock = stock,
        )
    }
}

data class AccountEditor(
    val id: String,
    val name: String,
    val kind: AccountKind,
    val currency: String,
    val balance: Exact,
    val currencies: List<String>,
    val rate: Exact,
    val separators: SeparatorPair,
)

data class HoldingEditor(
    val holdingId: String,
    val instrumentId: String,
    val name: String,
    val unit: String,
    val priceCurrency: String,
    val price: Exact,
    val quantity: Exact,
    val separators: SeparatorPair,
)

data class CurrencyPreview(
    val rate: Exact,
    val balance: Exact,
)

internal data class SavedAccount(
    val id: String,
    val name: String,
    val kind: AccountKind,
    val currency: String,
    val balance: Exact,
    val rate: Exact? = null,
)

internal data class NewHolding(
    val name: String,
    val unit: String,
    val priceCurrency: String,
    val price: Exact,
    val quantity: Exact,
    val stockAccountId: String? = null,
)

/** The portfolio commands and the read model the page shows. */
object PortfolioBook {
    fun present(book: Portfolio): PortfolioView {
        val sections = sections(book)
        val assets = sumSide(book, positive = true)
        val liabilities = sumSide(book, positive = false)
        return PortfolioView(
            assets = assets,
            liabilities = liabilities,
            netWorth = assets + liabilities,
            sections = sections,
            separators = book.separators,
            currencies = currencies(book),
            stockAccounts =
                book.accounts
                    .filter { it.kind == AccountKind.stock && !it.setAside }
                    .map { StockAccountChoice(it.id, it.name) },
            transactions = transactions(book),
        )
    }

    fun accountEditor(
        book: Portfolio,
        id: String,
    ): PortfolioOutcome<AccountEditor> =
        caught {
            val account = account(book, id)
            AccountEditor(
                id = account.id,
                name = account.name,
                kind = account.kind,
                currency = account.currency,
                balance = majorAmount(balance(book, id), account.currency),
                currencies = currencies(book),
                rate = rate(book, account.currency),
                separators = book.separators,
            )
        }

    fun holdingEditor(
        book: Portfolio,
        id: String,
    ): PortfolioOutcome<HoldingEditor> =
        caught {
            val holding = holding(book, id)
            val instrument = instrument(book, holding.instrumentId)
            HoldingEditor(
                holdingId = holding.id,
                instrumentId = instrument.id,
                name = instrument.name,
                unit = instrument.unit,
                priceCurrency = instrument.priceCurrency,
                price = instrument.price,
                quantity = quantity(book, holding.id),
                separators = book.separators,
            )
        }

    fun currencyPreview(
        book: Portfolio,
        id: String,
        currency: String,
    ): PortfolioOutcome<CurrencyPreview> =
        caught {
            account(book, id)
            val nextRate = rate(book, currency)
            CurrencyPreview(rate = nextRate, balance = proposedBalance(book, id, nextRate))
        }

    fun apply(
        book: Portfolio,
        command: PortfolioCommand,
    ): PortfolioOutcome<Portfolio> =
        caught {
            when (command) {
                is PortfolioCommand.AddAccount -> addAccount(book, command.name, command.kind, command.currency)
                is PortfolioCommand.SaveAccount ->
                    saveAccount(
                        book,
                        SavedAccount(
                            id = command.id,
                            name = command.name,
                            kind = command.kind,
                            currency = command.currency,
                            balance = parseAmount(book, command.balanceText),
                            rate = command.rateText?.let { parseAmount(book, it) },
                        ),
                    )
                is PortfolioCommand.SetAside -> setAside(book, command.id)
                is PortfolioCommand.AddShares ->
                    addInstrument(
                        book,
                        NewHolding(
                            name = command.name,
                            unit = command.unit,
                            priceCurrency = command.priceCurrency,
                            price = parseAmount(book, command.priceText),
                            quantity = parseAmount(book, command.quantityText),
                            stockAccountId = command.stockAccountId,
                        ),
                    )
                is PortfolioCommand.AddPhysicalHolding ->
                    addInstrument(
                        book,
                        NewHolding(
                            name = command.name,
                            unit = command.unit,
                            priceCurrency = command.priceCurrency,
                            price = parseAmount(book, command.priceText),
                            quantity = parseAmount(book, command.quantityText),
                        ),
                    )
                is PortfolioCommand.SaveHolding ->
                    saveHolding(
                        book,
                        command.holdingId,
                        command.name,
                        parseAmount(book, command.priceText),
                        parseAmount(book, command.quantityText),
                    )
                is PortfolioCommand.RemoveHolding -> removeHolding(book, command.holdingId)
                is PortfolioCommand.RemoveInstrument -> removeInstrument(book, command.instrumentId)
                is PortfolioCommand.MoveSection -> moveSection(book, command.key, command.delta)
                is PortfolioCommand.MoveRow -> moveRow(book, command.sectionKey, command.rowId, command.delta)
            }
        }

    private fun transactions(book: Portfolio): List<TransactionLine> {
        val accounts = book.accounts.associateBy { it.id }
        val holdings = book.holdings.associateBy { it.id }
        val instruments = book.instruments.associateBy { it.id }
        val lines =
            buildList {
                for (movement in book.movements) {
                    val account = accounts[movement.accountId] ?: continue
                    add(
                        TransactionLine(
                            id = movement.id,
                            title = account.name,
                            minor = movement.amountMinor,
                            currency = account.currency,
                        ),
                    )
                }
                for (adjustment in book.adjustments) {
                    val holding = holdings[adjustment.holdingId]
                    val instrument = holding?.let { instruments[it.instrumentId] }
                    if (holding != null && instrument != null) {
                        add(
                            TransactionLine(
                                id = adjustment.id,
                                title = instrument.name,
                                quantity = adjustment.quantity,
                                unit = instrument.unit,
                            ),
                        )
                    }
                }
            }
        return lines.sortedByDescending { it.id.toIntOrNull() ?: 0 }
    }

    private fun <T> caught(body: () -> T): PortfolioOutcome<T> =
        try {
            PortfolioOutcome.Ready(body())
        } catch (refusal: Refusal) {
            PortfolioOutcome.Refused(refusal.failure)
        }
}

internal fun refuse(failure: PortfolioFailure): Nothing = throw Refusal(failure)

private class Refusal(
    val failure: PortfolioFailure,
) : Exception()
