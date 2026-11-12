package com.zenith.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.zenith.data.db.ZenithDatabase

actual class DriverFactory(
    private val context: Context,
) {
    actual fun create(): SqlDriver = AndroidSqliteDriver(ZenithDatabase.Schema, context, "zenith.db")
}
