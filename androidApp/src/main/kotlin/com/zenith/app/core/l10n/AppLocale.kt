package com.zenith.app.core.l10n

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.text.format.DateFormat
import com.zenith.domain.portfolio.AppLanguage
import com.zenith.domain.portfolio.DateOrder
import java.util.Locale

private const val LANGUAGE_KEY = "language"

fun Context.withAppLanguage(language: AppLanguage): Context {
    val locale = language.locale()
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return createConfigurationContext(config)
}

fun Context.storedLanguage(): AppLanguage = AppLanguage.english.fromStored(preferences().getString(LANGUAGE_KEY, null))

fun Context.storeLanguage(language: AppLanguage) {
    preferences().edit().putString(LANGUAGE_KEY, language.name).apply()
}

fun Context.applyStoredLanguage(language: AppLanguage) {
    if (storedLanguage() == language) {
        return
    }
    storeLanguage(language)
    (this as? Activity)?.recreate()
}

fun Context.phoneDateOrder(): DateOrder {
    val order = DateFormat.getDateFormatOrder(this).concatToString()
    return when (order) {
        "Mdy" -> DateOrder.monthDayYear
        "yMd" -> DateOrder.yearMonthDay
        "ydM" -> DateOrder.yearDayMonth
        else -> DateOrder.dayMonthYear
    }
}

fun Context.phoneUses24HourClock(): Boolean = DateFormat.is24HourFormat(this)

private fun AppLanguage.locale(): Locale = if (this == AppLanguage.vietnamese) Locale.forLanguageTag("vi") else Locale.forLanguageTag("en")

private fun AppLanguage.fromStored(stored: String?): AppLanguage = if (stored == AppLanguage.vietnamese.name) AppLanguage.vietnamese else AppLanguage.english

private fun Context.preferences() = getSharedPreferences("zenith", Context.MODE_PRIVATE)
