package com.zenith.domain.portfolio

import com.zenith.domain.money.Exact
import com.zenith.domain.money.fromQuanta
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class PortfolioBookTest {
    @Test
    fun netWorthIsAssetsPlusLiabilitiesAndANegativeBankIsALiability() {
        var book = Portfolio.empty
        book = book.apply(PortfolioCommand.AddAccount("Cash", AccountKind.cash, "VND"))
        book = book.apply(PortfolioCommand.AddAccount("Bank", AccountKind.bank, "VND"))
        book = book.apply(PortfolioCommand.AddAccount("Card", AccountKind.creditCard, "VND"))
        book = book.apply(PortfolioCommand.AddAccount("Loan", AccountKind.loan, "VND"))
        book = book.apply(PortfolioCommand.SaveAccount("1", "Cash", AccountKind.cash, "VND", "10000000", null))
        book = book.apply(PortfolioCommand.SaveAccount("2", "Bank", AccountKind.bank, "VND", "-100000", null))
        book = book.apply(PortfolioCommand.SaveAccount("3", "Card", AccountKind.creditCard, "VND", "-2000000", null))
        book = book.apply(PortfolioCommand.SaveAccount("4", "Loan", AccountKind.loan, "VND", "-1000000", null))
        book = book.apply(PortfolioCommand.AddAccount("SSI", AccountKind.stock, "VND"))
        book =
            book.apply(
                PortfolioCommand.AddShares("FPT", "share", "VND", "50000", "100", stockAccountId = "9"),
            )
        book = book.apply(PortfolioCommand.AddPhysicalHolding("Gold", "lượng", "VND", "3000000", "1"))

        val view = PortfolioBook.present(book)

        assertEquals(18000000L, view.assets)
        assertEquals(-3100000L, view.liabilities)
        assertEquals(14900000L, view.netWorth)
        assertEquals(
            listOf(
                SectionKind.cash,
                SectionKind.bank,
                SectionKind.creditCard,
                SectionKind.loan,
                SectionKind.stock,
                SectionKind.shares,
                SectionKind.physical,
            ),
            view.sections.map { it.kind },
        )
        assertEquals(-100_000L, view.sections.single { it.kind == SectionKind.bank }.subtotalMinor)
        val shares = view.sections.single { it.kind == SectionKind.shares }
        assertEquals("FPT", shares.heading)
        assertEquals("SSI", shares.rows.single().title)
        assertEquals(Exact.parse("100"), shares.subtotalQuantity)
        assertEquals("share", shares.subtotalUnit)
    }

    @Test
    fun settingAStockAccountAsideRemovesItsShareHoldingsAndKeepsItsCash() {
        var book = Portfolio.empty
        book = book.apply(PortfolioCommand.AddAccount("SSI", AccountKind.stock, "VND"))
        book = book.apply(PortfolioCommand.SaveAccount("1", "SSI", AccountKind.stock, "VND", "5000000", null))
        book = book.apply(PortfolioCommand.AddShares("FPT", "share", "VND", "50000", "100", stockAccountId = "1"))

        book = book.apply(PortfolioCommand.SetAside("1"))
        val view = PortfolioBook.present(book)

        assertEquals(0L, view.assets)
        assertEquals(0L, view.liabilities)
        assertEquals(0L, view.netWorth)
        assertTrue(view.sections.isEmpty())
        assertTrue(book.holdings.isEmpty())
        assertTrue(book.instruments.isEmpty())
        assertTrue(book.adjustments.isEmpty())
        assertTrue(book.movements.isNotEmpty())
        assertTrue(book.accounts.single().setAside)
    }

    @Test
    fun settingAsideOneStockAccountLeavesTheOtherHolding() {
        val book =
            Portfolio(
                accounts =
                    listOf(
                        AccountRecord("1", "SSI", AccountKind.stock, "VND", 1),
                        AccountRecord("2", "VPS", AccountKind.stock, "VND", 2),
                    ),
                instruments = listOf(InstrumentRecord("i", "FPT", "share", "VND", Exact.parse("50000"))),
                holdings =
                    listOf(
                        HoldingRecord("h1", "i", 1, "1"),
                        HoldingRecord("h2", "i", 2, "2"),
                    ),
                adjustments =
                    listOf(
                        AdjustmentRecord("a1", "h1", Exact.parse("10")),
                        AdjustmentRecord("a2", "h2", Exact.parse("4")),
                    ),
                nextId = 10,
            ).apply(PortfolioCommand.SetAside("1"))

        assertEquals(listOf("h2"), book.holdings.map { it.id })
        assertEquals(listOf("i"), book.instruments.map { it.id })
        assertEquals(listOf("a2"), book.adjustments.map { it.id })
        assertTrue(book.accounts.first { it.id == "1" }.setAside)
        val shares = PortfolioBook.present(book).sections.single { it.kind == SectionKind.shares }
        assertEquals("FPT", shares.heading)
        assertEquals("VPS", shares.rows.single().title)
        assertEquals(Exact.parse("4"), shares.subtotalQuantity)
    }

    @Test
    fun typingTheSameBalanceRecordsNothingAndANewAccountStartsAtZero() {
        var book = Portfolio.empty.apply(PortfolioCommand.AddAccount("Cash", AccountKind.cash, "VND"))
        assertEquals(0L, PortfolioBook.present(book).assets)
        assertTrue(book.movements.isEmpty())

        book = book.apply(PortfolioCommand.SaveAccount("1", "Cash", AccountKind.cash, "VND", "46", null))
        book = book.apply(PortfolioCommand.SaveAccount("1", "Cash", AccountKind.cash, "VND", "46", null))

        assertEquals(1, book.movements.size)
    }

    @Test
    fun removingAHoldingRemovesItsQuantity() {
        var book = Portfolio.empty.apply(PortfolioCommand.AddAccount("SSI", AccountKind.stock, "VND"))
        book = book.apply(PortfolioCommand.AddShares("FPT", "share", "VND", "50000", "100", stockAccountId = "1"))

        book = book.apply(PortfolioCommand.RemoveHolding("3"))

        assertTrue(book.holdings.isEmpty())
        assertTrue(book.adjustments.isEmpty())
        assertTrue(book.instruments.isEmpty())
        assertEquals(0L, PortfolioBook.present(book).assets)
    }

    @Test
    fun aBalanceThatCrossesZeroStaysInItsKind() {
        var book = Portfolio.empty
        book = book.apply(PortfolioCommand.AddAccount("Cash", AccountKind.cash, "VND"))
        book = book.apply(PortfolioCommand.AddAccount("Wallet", AccountKind.cash, "VND"))
        book = book.apply(PortfolioCommand.SaveAccount("1", "Cash", AccountKind.cash, "VND", "10", null))
        book = book.apply(PortfolioCommand.SaveAccount("2", "Wallet", AccountKind.cash, "VND", "20", null))
        book = book.apply(PortfolioCommand.SaveAccount("1", "Cash", AccountKind.cash, "VND", "-5", null))

        val cash = PortfolioBook.present(book).sections.single { it.kind == SectionKind.cash }
        assertEquals(listOf("Cash", "Wallet"), cash.rows.map { it.title })
        assertEquals(-5L, cash.rows.first().minor)
        assertEquals(15L, cash.subtotalMinor)
    }

    @Test
    fun changingKindMovesTheAccountToTheEndOfThatGroup() {
        var book = Portfolio.empty
        book = book.apply(PortfolioCommand.AddAccount("Cash", AccountKind.cash, "VND"))
        book = book.apply(PortfolioCommand.AddAccount("Wallet", AccountKind.cash, "VND"))
        book = book.apply(PortfolioCommand.AddAccount("VCB", AccountKind.bank, "VND"))
        book = book.apply(PortfolioCommand.SaveAccount("1", "Cash", AccountKind.bank, "VND", "10", null))

        val view = PortfolioBook.present(book)
        assertEquals(
            listOf("Wallet"),
            view.sections
                .single { it.kind == SectionKind.cash }
                .rows
                .map { it.title },
        )
        assertEquals(
            listOf("VCB", "Cash"),
            view.sections
                .single { it.kind == SectionKind.bank }
                .rows
                .map { it.title },
        )
    }

    @Test
    fun anOldSignOrderKeepsAssetsBeforeLiabilitiesInsideTheKind() {
        val book =
            Portfolio(
                accounts =
                    listOf(
                        AccountRecord("v", "VCB", AccountKind.bank, "VND", 5),
                        AccountRecord("u", "USD", AccountKind.bank, "VND", 1),
                        AccountRecord("t", "TCB", AccountKind.bank, "VND", 3),
                    ),
                rowOrder = mapOf("assets" to listOf("v"), "liabilities" to listOf("u")),
            )

        val bank = PortfolioBook.present(book).sections.single { it.kind == SectionKind.bank }
        assertEquals(listOf("VCB", "USD", "TCB"), bank.rows.map { it.title })
    }

    @Test
    fun anOldSectionOrderDropsSignKeysAndReturnsToTheDefault() {
        val book =
            Portfolio(
                accounts = listOf(AccountRecord("1", "Cash", AccountKind.cash, "VND", 1)),
                holdings = listOf(HoldingRecord("h", "i", 1, null)),
                instruments = listOf(InstrumentRecord("i", "Gold", "lượng", "VND", Exact.one)),
                sectionOrder = listOf("physical", "assets", "liabilities"),
            )

        assertEquals(
            listOf(SectionKind.cash, SectionKind.physical),
            PortfolioBook.present(book).sections.map { it.kind },
        )
    }

    @Test
    fun aNewSectionTakesItsDefaultPlaceUntilASectionIsMoved() {
        var book = Portfolio.empty
        book = book.apply(PortfolioCommand.AddAccount("Cash", AccountKind.cash, "VND"))
        book = book.apply(PortfolioCommand.AddAccount("Loan", AccountKind.loan, "VND"))
        book = book.apply(PortfolioCommand.AddAccount("Bank", AccountKind.bank, "VND"))

        assertEquals(
            listOf(SectionKind.cash, SectionKind.bank, SectionKind.loan),
            PortfolioBook.present(book).sections.map { it.kind },
        )

        book = book.apply(PortfolioCommand.MoveSection("loan", -1))
        book = book.apply(PortfolioCommand.AddAccount("Card", AccountKind.creditCard, "VND"))

        assertEquals(
            listOf(SectionKind.cash, SectionKind.loan, SectionKind.bank, SectionKind.creditCard),
            PortfolioBook.present(book).sections.map { it.kind },
        )
    }

    @Test
    fun sharesOfOneInstrumentAddUpAcrossStockAccounts() {
        val book =
            Portfolio(
                accounts =
                    listOf(
                        AccountRecord("1", "SSI", AccountKind.stock, "VND", 1),
                        AccountRecord("2", "VPS", AccountKind.stock, "VND", 2),
                    ),
                instruments = listOf(InstrumentRecord("i", "FPT", "share", "VND", Exact.parse("50000"))),
                holdings =
                    listOf(
                        HoldingRecord("h1", "i", 1, "1"),
                        HoldingRecord("h2", "i", 2, "2"),
                    ),
                adjustments =
                    listOf(
                        AdjustmentRecord("a1", "h1", Exact.parse("10")),
                        AdjustmentRecord("a2", "h2", Exact.parse("4")),
                    ),
                nextId = 10,
            )

        val shares = PortfolioBook.present(book).sections.single { it.kind == SectionKind.shares }
        assertEquals("FPT", shares.heading)
        assertEquals(listOf("SSI", "VPS"), shares.rows.map { it.title })
        assertEquals(Exact.parse("14"), shares.subtotalQuantity)
        assertEquals(700_000L, PortfolioBook.present(book).assets)
    }

    @Test
    fun aForeignBalanceConvertsThroughTheRateList() {
        val book =
            Portfolio(rates = mapOf("USD" to Exact.parse("25000")))
                .apply(PortfolioCommand.AddAccount("Dollar", AccountKind.bank, "USD"))
                .apply(PortfolioCommand.SaveAccount("1", "Dollar", AccountKind.bank, "USD", "3", null))

        assertEquals(75000L, PortfolioBook.present(book).assets)
    }

    @Test
    fun aMissingRateRefusesThePreview() {
        val book = Portfolio.empty.apply(PortfolioCommand.AddAccount("Cash", AccountKind.cash, "VND"))

        assertEquals(
            PortfolioOutcome.Refused(PortfolioFailure.CurrencyNeedsRate),
            PortfolioBook.currencyPreview(book, "1", "USD"),
        )
    }

    @Test
    fun transactionsListBalanceAndQuantityDifferencesLatestFirst() {
        var book =
            Portfolio.empty
                .apply(PortfolioCommand.AddAccount("Cash", AccountKind.cash, "VND"))
                .apply(PortfolioCommand.SaveAccount("1", "Cash", AccountKind.cash, "VND", "10000000", null))
                .apply(PortfolioCommand.AddAccount("SSI", AccountKind.stock, "VND"))
                .apply(PortfolioCommand.SaveAccount("3", "SSI", AccountKind.stock, "VND", "5000000", null))
                .apply(PortfolioCommand.AddAccount("VPS", AccountKind.stock, "VND"))
                .apply(PortfolioCommand.AddShares("FPT", "share", "VND", "50000", "100", stockAccountId = "3"))
                .apply(PortfolioCommand.AddShares("FPT", "share", "VND", "50000", "40", stockAccountId = "5"))
                .apply(PortfolioCommand.AddPhysicalHolding("Gold", "lượng", "VND", "3000000", "1"))
        val gold = book.holdings.last().id
        book = book.apply(PortfolioCommand.SaveHolding(gold, "Gold", "3000000", "3"))
        book = book.apply(PortfolioCommand.SetAside("1"))

        val lines = PortfolioBook.present(book).transactions

        assertEquals(listOf("Gold", "Gold", "FPT", "FPT", "SSI", "Cash"), lines.map { it.title })
        assertEquals(listOf(Exact.parse("2"), Exact.parse("1")), lines.take(2).map { it.quantity })
        assertEquals(listOf("lượng", "lượng"), lines.take(2).map { it.unit })
        assertEquals(listOf(Exact.parse("40"), Exact.parse("100")), lines.drop(2).take(2).map { it.quantity })
        assertEquals(listOf("share", "share"), lines.drop(2).take(2).map { it.unit })
        assertEquals(listOf(5_000_000L, 10_000_000L), lines.takeLast(2).map { it.minor })
        assertEquals(listOf("VND", "VND"), lines.takeLast(2).map { it.currency })
    }

    @Test
    fun aCurrencyPreviewUsesTheRateList() {
        val book =
            Portfolio(rates = mapOf("USD" to Exact.parse("25000")))
                .apply(PortfolioCommand.AddAccount("Dollar", AccountKind.bank, "VND"))
                .apply(PortfolioCommand.SaveAccount("1", "Dollar", AccountKind.bank, "VND", "25000", null))

        assertEquals(
            PortfolioOutcome.Ready(CurrencyPreview(rate = Exact.parse("25000"), balance = Exact.one)),
            PortfolioBook.currencyPreview(book, "1", "USD"),
        )
    }

    @Test
    fun anUnreadableBalanceIsInvalidAmount() {
        val book = Portfolio.empty.apply(PortfolioCommand.AddAccount("Cash", AccountKind.cash, "VND"))

        assertEquals(
            PortfolioOutcome.Refused(PortfolioFailure.InvalidAmount),
            PortfolioBook.apply(book, PortfolioCommand.SaveAccount("1", "Cash", AccountKind.cash, "VND", "12a", null)),
        )
    }

    @Test
    fun aBalanceUsesThePortfoliosSeparators() {
        val book =
            Portfolio(separators = SeparatorPair.pointComma, rates = mapOf("USD" to Exact.one))
                .apply(PortfolioCommand.AddAccount("Dollar", AccountKind.bank, "USD"))
                .apply(PortfolioCommand.SaveAccount("1", "Dollar", AccountKind.bank, "USD", "1.000,50", null))

        assertEquals(10_005_000L, book.movements.single().amountMinor)
    }

    @Test
    fun aMissingAccountIsNotFound() {
        assertEquals(
            PortfolioOutcome.Refused(PortfolioFailure.NotFound),
            PortfolioBook.accountEditor(Portfolio.empty, "1"),
        )
    }

    @Test
    fun precisionRoundsTheScreenAndKeepsTheSavedAmount() {
        var book =
            Portfolio(rates = mapOf("USD" to Exact.one))
                .apply(PortfolioCommand.AddAccount("Dollar", AccountKind.bank, "USD"))
                .apply(PortfolioCommand.SaveAccount("1", "Dollar", AccountKind.bank, "USD", "10.25", null))
        val saved = book.movements.single().amountMinor
        book = book.apply(PortfolioCommand.SetAmountPrecision("USD", 0))

        assertEquals(saved, book.movements.single().amountMinor)
        assertEquals(Exact.parse("10"), readyEditor(book).balance)
        assertEquals(Exact.zero, shownAmount(book, Exact.parse("0.40"), "USD"))
        assertEquals(Exact.parse("1"), shownTotal(book, listOf(Exact.parse("0.40"), Exact.parse("0.40")), "USD"))

        val shown = book.apply(PortfolioCommand.SaveAccount("1", "Dollar", AccountKind.bank, "USD", "10", null))
        assertEquals(saved, shown.movements.single().amountMinor)

        val replaced = book.apply(PortfolioCommand.SaveAccount("1", "Dollar", AccountKind.bank, "USD", "11", null))
        assertEquals(Exact.parse("0.75"), fromQuanta(replaced.movements.last().amountMinor))
        assertEquals(Exact.parse("11"), readyEditor(replaced).balance)
    }

    @Test
    fun netWorthUsesTheStoredAmountThenRoundsInThePrimaryCurrency() {
        val book =
            Portfolio(rates = mapOf("USD" to Exact.parse("25000")), primaryCurrency = "USD")
                .apply(PortfolioCommand.AddAccount("Dollar", AccountKind.bank, "USD"))
                .apply(PortfolioCommand.SaveAccount("1", "Dollar", AccountKind.bank, "USD", "3.25", null))
                .apply(PortfolioCommand.SetAmountPrecision("USD", 0))

        val view = PortfolioBook.present(book)
        assertEquals(81250L, view.assets)
        assertEquals(Exact.parse("3"), view.netWorthExact)
    }

    @Test
    fun aFullDateNamesTodayAndAPastWeekday() {
        val today = CalendarDay(2026, 11, 10)
        assertEquals(
            "Today, 10 November",
            formatCalendarDay(today, today, DateFormat.full, DateOrder.dayMonthYear, AppLanguage.english),
        )
        assertEquals(
            "10 November",
            formatCalendarDay(today, today, DateFormat.dateOnly, DateOrder.dayMonthYear, AppLanguage.english),
        )
        assertEquals(
            "Today, 10/11/2026",
            formatCalendarDay(today, today, DateFormat.numeric, DateOrder.dayMonthYear, AppLanguage.english),
        )
        assertEquals(
            "10/11/2026",
            formatCalendarDay(today, today, DateFormat.numericOnly, DateOrder.dayMonthYear, AppLanguage.english),
        )
        val past = CalendarDay(2025, 11, 10)
        assertEquals(
            "Monday, 10 November 2025",
            formatCalendarDay(past, today, DateFormat.full, DateOrder.dayMonthYear, AppLanguage.english),
        )
        assertEquals(
            "2026/10/11",
            formatCalendarDay(today, today, DateFormat.numericOnly, DateOrder.yearDayMonth, AppLanguage.english),
        )
        assertEquals(
            DateOrder.dayMonthYear,
            resolveDateOrder(DateOrder.defaultOrder, AppLanguage.vietnamese, DateOrder.monthDayYear),
        )
        assertEquals(
            DateOrder.monthDayYear,
            resolveDateOrder(DateOrder.system, AppLanguage.vietnamese, DateOrder.monthDayYear),
        )
    }

    private fun readyEditor(book: Portfolio): AccountEditor =
        when (val outcome = PortfolioBook.accountEditor(book, "1")) {
            is PortfolioOutcome.Ready -> outcome.value
            is PortfolioOutcome.Refused -> fail("refused: ${outcome.failure}")
        }

    private fun Portfolio.apply(command: PortfolioCommand): Portfolio =
        when (val outcome = PortfolioBook.apply(this, command)) {
            is PortfolioOutcome.Ready -> outcome.value
            is PortfolioOutcome.Refused -> fail("refused: ${outcome.failure}")
        }
}
