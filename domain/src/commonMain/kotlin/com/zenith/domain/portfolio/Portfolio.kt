package com.zenith.domain.portfolio

import com.zenith.domain.money.AMOUNT_SCALE
import com.zenith.domain.money.Exact
import com.zenith.domain.money.PERCENTAGE_PRECISION_START

enum class AccountKind { cash, bank, eWallet, creditCard, loan, receivable, stock }

enum class SeparatorPair { commaPoint, pointComma }

enum class AppLanguage { english, vietnamese }

enum class DatePickerStyle { calendar, wheels }

enum class TimePickerStyle { clock, wheels }

enum class ClockFormat { system, hour24, hour12 }

enum class DateFormat { full, dateOnly, numeric, numericOnly }

enum class DateOrder { defaultOrder, monthDayYear, dayMonthYear, yearMonthDay, yearDayMonth, system }

enum class FirstWeekday { monday, sunday, saturday }

/** The stored portfolio. */
data class Portfolio(
    val accounts: List<AccountRecord> = emptyList(),
    val movements: List<MovementRecord> = emptyList(),
    val instruments: List<InstrumentRecord> = emptyList(),
    val holdings: List<HoldingRecord> = emptyList(),
    val adjustments: List<AdjustmentRecord> = emptyList(),
    val rates: Map<String, Exact> = emptyMap(),
    val sectionOrder: List<String> = emptyList(),
    val rowOrder: Map<String, List<String>> = emptyMap(),
    val separators: SeparatorPair = SeparatorPair.commaPoint,
    val nextId: Int = 1,
    val language: AppLanguage = AppLanguage.english,
    val primaryCurrency: String = "VND",
    val datePickerStyle: DatePickerStyle = DatePickerStyle.calendar,
    val timePickerStyle: TimePickerStyle = TimePickerStyle.clock,
    val clockFormat: ClockFormat = ClockFormat.system,
    val dateFormat: DateFormat = DateFormat.full,
    val dateOrder: DateOrder = DateOrder.defaultOrder,
    val firstWeekday: FirstWeekday = FirstWeekday.monday,
    val amountPrecisions: Map<String, Int> = emptyMap(),
    val percentagePrecision: Int = PERCENTAGE_PRECISION_START,
    /** 0 means amounts are still in each currency's minor unit. [AMOUNT_SCALE] means quanta. */
    val amountScale: Int = AMOUNT_SCALE,
) {
    companion object {
        val empty = Portfolio()
    }
}

data class AccountRecord(
    val id: String,
    val name: String,
    val kind: AccountKind,
    val currency: String,
    val createdOrder: Int,
    val setAside: Boolean = false,
)

data class MovementRecord(
    val id: String,
    val accountId: String,
    val amountMinor: Long,
    val vndPerUnit: Exact,
)

data class InstrumentRecord(
    val id: String,
    val name: String,
    val unit: String,
    val priceCurrency: String,
    val price: Exact,
)

data class HoldingRecord(
    val id: String,
    val instrumentId: String,
    val createdOrder: Int,
    val stockAccountId: String? = null,
)

data class AdjustmentRecord(
    val id: String,
    val holdingId: String,
    val quantity: Exact,
)

/** A rule the portfolio refuses to break. */
sealed interface PortfolioFailure {
    data object NameRequired : PortfolioFailure

    data object CurrencyNeedsRate : PortfolioFailure

    data object StockKindFixed : PortfolioFailure

    data object StockAccountRequired : PortfolioFailure

    data object RatePositive : PortfolioFailure

    data object NotFound : PortfolioFailure

    data object InvalidAmount : PortfolioFailure
}
