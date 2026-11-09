package com.zenith.data

import com.zenith.data.db.ZenithDatabase
import com.zenith.domain.portfolio.Portfolio
import com.zenith.domain.portfolio.PortfolioRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SqlDelightPortfolioRepository(
    private val database: ZenithDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PortfolioRepository {
    override suspend fun load(): Portfolio =
        withContext(dispatcher) {
            val json = database.portfolioQueries.selectJson().executeAsOneOrNull()
            if (json.isNullOrEmpty()) Portfolio.empty else PortfolioJson.decode(json)
        }

    override suspend fun save(portfolio: Portfolio) {
        withContext(dispatcher) {
            database.portfolioQueries.upsert(PortfolioJson.encode(portfolio))
        }
    }
}

fun database(factory: DriverFactory): ZenithDatabase = ZenithDatabase(factory.create())
