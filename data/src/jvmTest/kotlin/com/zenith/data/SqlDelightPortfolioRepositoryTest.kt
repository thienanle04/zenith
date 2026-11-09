package com.zenith.data

import com.zenith.domain.money.Exact
import com.zenith.domain.portfolio.AccountKind
import com.zenith.domain.portfolio.Portfolio
import com.zenith.domain.portfolio.PortfolioBook
import com.zenith.domain.portfolio.PortfolioCommand
import com.zenith.domain.portfolio.PortfolioOutcome
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class SqlDelightPortfolioRepositoryTest {
    @Test
    fun saveAndLoadRoundTripThePortfolio() {
        val repository = SqlDelightPortfolioRepository(database(DriverFactory()))
        val portfolio = populated()
        runBlocking {
            assertEquals(Portfolio.empty, repository.load())
            repository.save(portfolio)
            assertEquals(portfolio, repository.load())
        }
    }

    @Test
    fun theDocumentRoundTripsAndKeepsEveryField() {
        val portfolio = populated()
        val raw = PortfolioJson.encode(portfolio)
        assertEquals(portfolio, PortfolioJson.decode(raw))
        assert(raw.contains("\"setAside\":false"))
        assert(raw.contains("\"stockAccountId\":null"))
        assert(raw.contains("\"nextId\":"))
    }

    @Test
    fun anUnknownKeyRefusesTheDocument() {
        val raw = PortfolioJson.encode(Portfolio.empty).dropLast(1) + ""","extra":true}"""
        assertFails { PortfolioJson.decode(raw) }
    }

    private fun populated(): Portfolio {
        var portfolio = Portfolio(rates = mapOf("USD" to Exact.parse("25000.5")))
        portfolio = ready(portfolio, PortfolioCommand.AddAccount("Dollar", AccountKind.bank, "USD"))
        portfolio = ready(portfolio, PortfolioCommand.SaveAccount("1", "Dollar", AccountKind.bank, "USD", "1.5", null))
        portfolio = ready(portfolio, PortfolioCommand.AddPhysicalHolding("Gold", "lượng", "VND", "3.5", "0.5"))
        return portfolio
    }

    private fun ready(
        portfolio: Portfolio,
        command: PortfolioCommand,
    ): Portfolio = (PortfolioBook.apply(portfolio, command) as PortfolioOutcome.Ready).value
}
