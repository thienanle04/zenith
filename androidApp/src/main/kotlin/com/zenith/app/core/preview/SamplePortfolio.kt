package com.zenith.app.core.preview

import com.zenith.domain.portfolio.AccountEditor
import com.zenith.domain.portfolio.AccountKind
import com.zenith.domain.portfolio.HoldingEditor
import com.zenith.domain.portfolio.Portfolio
import com.zenith.domain.portfolio.PortfolioBook
import com.zenith.domain.portfolio.PortfolioCommand
import com.zenith.domain.portfolio.PortfolioOutcome
import com.zenith.domain.portfolio.PortfolioView

/** One populated portfolio for screen previews. */
object SamplePortfolio {
    val book: Portfolio = sampleBook()
    val view: PortfolioView = PortfolioBook.present(book)
    val bankAccount: AccountEditor = ready(PortfolioBook.accountEditor(book, accountId(AccountKind.bank)))
    val holding: HoldingEditor = ready(PortfolioBook.holdingEditor(book, book.holdings.first().id))

    private fun accountId(kind: AccountKind): String = book.accounts.first { it.kind == kind }.id
}

private fun sampleBook(): Portfolio {
    var book =
        Portfolio.empty
            .applied(PortfolioCommand.SetRate("USD", "25000"))
            .applied(PortfolioCommand.SetAmountPrecision("USD", 0))
            .applied(PortfolioCommand.AddAccount("Cash", AccountKind.cash, "VND"))
            .applied(PortfolioCommand.AddAccount("Bank", AccountKind.bank, "USD"))
    val cash = book.accounts.first { it.kind == AccountKind.cash }
    book =
        book
            .applied(
                PortfolioCommand.SaveAccount(
                    id = cash.id,
                    name = cash.name,
                    kind = cash.kind,
                    currency = cash.currency,
                    balanceText = "-50000",
                    rateText = null,
                ),
            ).applied(PortfolioCommand.AddPhysicalHolding("Gold", "oz", "VND", "1000000", "0"))
    return book
}

private fun Portfolio.applied(command: PortfolioCommand): Portfolio =
    when (val outcome = PortfolioBook.apply(this, command)) {
        is PortfolioOutcome.Ready -> outcome.value
        is PortfolioOutcome.Refused -> error("Sample portfolio refused ${outcome.failure}")
    }

private fun <T> ready(outcome: PortfolioOutcome<T>): T =
    when (outcome) {
        is PortfolioOutcome.Ready -> outcome.value
        is PortfolioOutcome.Refused -> error("Sample portfolio refused ${outcome.failure}")
    }
