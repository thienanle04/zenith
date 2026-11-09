package com.zenith.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.zenith.data.db.ZenithDatabase

actual class DriverFactory {
    actual fun create(): SqlDriver = NativeSqliteDriver(ZenithDatabase.Schema, "zenith.db")
}
