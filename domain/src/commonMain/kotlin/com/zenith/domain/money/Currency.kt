package com.zenith.domain.money

/** Minor-unit scale for a currency. VND's minor unit is the đồng. */
fun minorDigits(currency: String): Int =
    when (currency) {
        "VND", "JPY", "KRW" -> 0
        else -> 2
    }

private fun scale(currency: String): Exact = Exact.parse("1" + "0".repeat(minorDigits(currency)))

/** [minor] units as a major amount. 150 USD cents is 1.5. */
fun majorAmount(
    minor: Long,
    currency: String,
): Exact = Exact.fromLong(minor) / scale(currency)

/** A major amount rounded to the currency's minor unit. */
fun minorAmount(
    major: Exact,
    currency: String,
): Long = (major * scale(currency)).roundToIntChecked()
