package com.zenith.domain.portfolio

/** The phone's copy of the portfolio. */
interface PortfolioRepository {
    suspend fun load(): Portfolio

    suspend fun save(portfolio: Portfolio)
}
