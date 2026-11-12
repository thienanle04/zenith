package com.zenith.domain.portfolio

sealed interface PortfolioOutcome<out T> {
    data class Ready<T>(
        val value: T,
    ) : PortfolioOutcome<T>

    data class Refused(
        val failure: PortfolioFailure,
    ) : PortfolioOutcome<Nothing>
}

sealed interface PortfolioCommand {
    data class AddAccount(
        val name: String,
        val kind: AccountKind,
        val currency: String,
    ) : PortfolioCommand

    data class SaveAccount(
        val id: String,
        val name: String,
        val kind: AccountKind,
        val currency: String,
        val balanceText: String,
        val rateText: String?,
    ) : PortfolioCommand

    data class SetAside(
        val id: String,
    ) : PortfolioCommand

    data class AddShares(
        val name: String,
        val unit: String,
        val priceCurrency: String,
        val priceText: String,
        val quantityText: String,
        val stockAccountId: String,
    ) : PortfolioCommand

    data class AddPhysicalHolding(
        val name: String,
        val unit: String,
        val priceCurrency: String,
        val priceText: String,
        val quantityText: String,
    ) : PortfolioCommand

    data class SaveHolding(
        val holdingId: String,
        val name: String,
        val priceText: String,
        val quantityText: String,
    ) : PortfolioCommand

    data class RemoveHolding(
        val holdingId: String,
    ) : PortfolioCommand

    data class RemoveInstrument(
        val instrumentId: String,
    ) : PortfolioCommand

    data class MoveSection(
        val key: String,
        val delta: Int,
    ) : PortfolioCommand

    data class MoveRow(
        val sectionKey: String,
        val rowId: String,
        val delta: Int,
    ) : PortfolioCommand

    data class SetLanguage(
        val language: AppLanguage,
    ) : PortfolioCommand

    data class SetPrimaryCurrency(
        val currency: String,
    ) : PortfolioCommand

    data class SetRate(
        val currency: String,
        val rateText: String,
    ) : PortfolioCommand

    data class SetDatePickerStyle(
        val style: DatePickerStyle,
    ) : PortfolioCommand

    data class SetTimePickerStyle(
        val style: TimePickerStyle,
    ) : PortfolioCommand

    data class SetClockFormat(
        val format: ClockFormat,
    ) : PortfolioCommand

    data class SetDateFormat(
        val format: DateFormat,
    ) : PortfolioCommand

    data class SetDateOrder(
        val order: DateOrder,
    ) : PortfolioCommand

    data class SetFirstWeekday(
        val weekday: FirstWeekday,
    ) : PortfolioCommand

    data class SetAmountPrecision(
        val currency: String,
        val digits: Int,
    ) : PortfolioCommand

    data class SetPercentagePrecision(
        val digits: Int,
    ) : PortfolioCommand
}
