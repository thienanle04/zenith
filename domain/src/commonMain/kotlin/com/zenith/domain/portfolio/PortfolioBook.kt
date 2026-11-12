package com.zenith.domain.portfolio

import com.zenith.domain.money.Exact
import com.zenith.domain.money.PERCENTAGE_PRECISION_START
import com.zenith.domain.money.PRECISION_LIMIT
import com.zenith.domain.money.fromQuanta
import com.zenith.domain.money.minorAmount

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
    val language: AppLanguage = AppLanguage.english,
    val primaryCurrency: String = "VND",
    val assetsExact: Exact = Exact.zero,
    val liabilitiesExact: Exact = Exact.zero,
    val netWorthExact: Exact = Exact.zero,
    val datePickerStyle: DatePickerStyle = DatePickerStyle.calendar,
    val timePickerStyle: TimePickerStyle = TimePickerStyle.clock,
    val clockFormat: ClockFormat = ClockFormat.system,
    val dateFormat: DateFormat = DateFormat.full,
    val dateOrder: DateOrder = DateOrder.defaultOrder,
    val firstWeekday: FirstWeekday = FirstWeekday.monday,
    val amountPrecisions: Map<String, Int> = emptyMap(),
    val percentagePrecision: Int = PERCENTAGE_PRECISION_START,
    val rates: Map<String, Exact> = emptyMap(),
)

/** One stored balance difference or quantity difference. */
data class TransactionLine(
    val id: String,
    val title: String,
    val minor: Long? = null,
    val currency: String? = null,
    val quantity: Exact? = null,
    val unit: String? = null,
    /** Unrounded major amount. The screen rounds this to the currency's precision. */
    val amount: Exact? = null,
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
    /** The same subtotal in the primary currency, rounded for the screen. */
    val subtotalExact: Exact? = null,
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
    /** Unrounded major balance. The screen rounds this to the currency's precision. */
    val stored: Exact? = null,
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
        val current = book.withCurrentAmountScale()
        val sections = sections(current)
        val assetsVnd = signedVnd(current, positive = true)
        val liabilitiesVnd = signedVnd(current, positive = false)
        val assets = assetsVnd.roundToIntChecked()
        val liabilities = liabilitiesVnd.roundToIntChecked()
        return PortfolioView(
            assets = assets,
            liabilities = liabilities,
            netWorth = assets + liabilities,
            sections = sections,
            separators = current.separators,
            currencies = currencies(current),
            stockAccounts =
                current.accounts
                    .filter { it.kind == AccountKind.stock && !it.setAside }
                    .map { StockAccountChoice(it.id, it.name) },
            transactions = transactions(current),
            language = current.language,
            primaryCurrency = current.primaryCurrency,
            assetsExact = primaryAmount(current, assetsVnd),
            liabilitiesExact = primaryAmount(current, liabilitiesVnd),
            netWorthExact = primaryAmount(current, assetsVnd + liabilitiesVnd),
            datePickerStyle = current.datePickerStyle,
            timePickerStyle = current.timePickerStyle,
            clockFormat = current.clockFormat,
            dateFormat = current.dateFormat,
            dateOrder = current.dateOrder,
            firstWeekday = current.firstWeekday,
            amountPrecisions = currencies(current).associateWith { amountPrecision(current, it) },
            percentagePrecision = current.percentagePrecision,
            rates = current.rates,
        )
    }

    fun accountEditor(
        book: Portfolio,
        id: String,
    ): PortfolioOutcome<AccountEditor> =
        caught {
            val current = book.withCurrentAmountScale()
            val account = account(current, id)
            val major = fromQuanta(balance(current, id))
            AccountEditor(
                id = account.id,
                name = account.name,
                kind = account.kind,
                currency = account.currency,
                balance = shownAmount(current, major, account.currency),
                currencies = currencies(current),
                rate = rate(current, account.currency),
                separators = current.separators,
            )
        }

    fun holdingEditor(
        book: Portfolio,
        id: String,
    ): PortfolioOutcome<HoldingEditor> =
        caught {
            val current = book.withCurrentAmountScale()
            val holding = holding(current, id)
            val instrument = instrument(current, holding.instrumentId)
            HoldingEditor(
                holdingId = holding.id,
                instrumentId = instrument.id,
                name = instrument.name,
                unit = instrument.unit,
                priceCurrency = instrument.priceCurrency,
                price = instrument.price,
                quantity = quantity(current, holding.id),
                separators = current.separators,
            )
        }

    fun currencyPreview(
        book: Portfolio,
        id: String,
        currency: String,
    ): PortfolioOutcome<CurrencyPreview> =
        caught {
            val current = book.withCurrentAmountScale()
            account(current, id)
            val nextRate = rate(current, currency)
            val proposed = proposedBalance(current, id, nextRate)
            CurrencyPreview(rate = nextRate, balance = shownAmount(current, proposed, currency))
        }

    fun apply(
        book: Portfolio,
        command: PortfolioCommand,
    ): PortfolioOutcome<Portfolio> =
        caught {
            val current = book.withCurrentAmountScale()
            when (command) {
                is PortfolioCommand.AddAccount -> addAccount(current, command.name, command.kind, command.currency)
                is PortfolioCommand.SaveAccount ->
                    saveAccount(
                        current,
                        SavedAccount(
                            id = command.id,
                            name = command.name,
                            kind = command.kind,
                            currency = command.currency,
                            balance = parseAmount(current, command.balanceText),
                            rate = command.rateText?.let { parseAmount(current, it) },
                        ),
                    )
                is PortfolioCommand.SetAside -> setAside(current, command.id)
                is PortfolioCommand.AddShares ->
                    addInstrument(
                        current,
                        NewHolding(
                            name = command.name,
                            unit = command.unit,
                            priceCurrency = command.priceCurrency,
                            price = parseAmount(current, command.priceText),
                            quantity = parseAmount(current, command.quantityText),
                            stockAccountId = command.stockAccountId,
                        ),
                    )
                is PortfolioCommand.AddPhysicalHolding ->
                    addInstrument(
                        current,
                        NewHolding(
                            name = command.name,
                            unit = command.unit,
                            priceCurrency = command.priceCurrency,
                            price = parseAmount(current, command.priceText),
                            quantity = parseAmount(current, command.quantityText),
                        ),
                    )
                is PortfolioCommand.SaveHolding ->
                    saveHolding(
                        current,
                        command.holdingId,
                        command.name,
                        parseAmount(current, command.priceText),
                        parseAmount(current, command.quantityText),
                    )
                is PortfolioCommand.RemoveHolding -> removeHolding(current, command.holdingId)
                is PortfolioCommand.RemoveInstrument -> removeInstrument(current, command.instrumentId)
                is PortfolioCommand.MoveSection -> moveSection(current, command.key, command.delta)
                is PortfolioCommand.MoveRow -> moveRow(current, command.sectionKey, command.rowId, command.delta)
                else -> applySetting(current, command)
            }
        }

    private fun applySetting(
        book: Portfolio,
        command: PortfolioCommand,
    ): Portfolio =
        when (command) {
            is PortfolioCommand.SetLanguage -> book.copy(language = command.language)
            is PortfolioCommand.SetPrimaryCurrency -> setPrimaryCurrency(book, command.currency)
            is PortfolioCommand.SetRate -> setRate(book, command.currency, parseAmount(book, command.rateText))
            is PortfolioCommand.SetDatePickerStyle -> book.copy(datePickerStyle = command.style)
            is PortfolioCommand.SetTimePickerStyle -> book.copy(timePickerStyle = command.style)
            is PortfolioCommand.SetClockFormat -> book.copy(clockFormat = command.format)
            is PortfolioCommand.SetDateFormat -> book.copy(dateFormat = command.format)
            is PortfolioCommand.SetDateOrder -> book.copy(dateOrder = command.order)
            is PortfolioCommand.SetFirstWeekday -> book.copy(firstWeekday = command.weekday)
            is PortfolioCommand.SetAmountPrecision -> setAmountPrecision(book, command.currency, command.digits)
            is PortfolioCommand.SetPercentagePrecision -> book.copy(percentagePrecision = command.digits.coerceIn(0, PRECISION_LIMIT))
            else -> book
        }

    private fun transactions(book: Portfolio): List<TransactionLine> {
        val accounts = book.accounts.associateBy { it.id }
        val holdings = book.holdings.associateBy { it.id }
        val instruments = book.instruments.associateBy { it.id }
        val lines =
            buildList {
                for (movement in book.movements) {
                    val account = accounts[movement.accountId] ?: continue
                    val major = fromQuanta(movement.amountMinor)
                    add(
                        TransactionLine(
                            id = movement.id,
                            title = account.name,
                            minor = minorAmount(major, account.currency),
                            currency = account.currency,
                            amount = major,
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
