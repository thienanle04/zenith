package com.zenith.app

import android.app.Application
import com.zenith.data.DriverFactory
import com.zenith.data.SqlDelightPortfolioRepository
import com.zenith.data.database
import com.zenith.domain.portfolio.PortfolioRepository

class ZenithApplication : Application() {
    lateinit var portfolioRepository: PortfolioRepository
        private set

    override fun onCreate() {
        super.onCreate()
        portfolioRepository = SqlDelightPortfolioRepository(database(DriverFactory(this)))
    }
}
