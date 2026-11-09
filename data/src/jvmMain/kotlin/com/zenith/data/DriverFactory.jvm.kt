package com.zenith.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.zenith.data.db.ZenithDatabase

actual class DriverFactory {
    actual fun create(): SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also(ZenithDatabase.Schema::create)
}
