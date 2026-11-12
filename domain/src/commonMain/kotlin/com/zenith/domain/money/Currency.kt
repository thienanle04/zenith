package com.zenith.domain.money

/** Digits stored for every amount. A major unit is this many quanta. */
const val AMOUNT_SCALE = 4

/** Highest transaction amount decimal precision, and the highest percentage precision. */
const val PRECISION_LIMIT = 4

/** Percentage precision before the owner chooses one. */
const val PERCENTAGE_PRECISION_START = 2

/** Minor-unit scale for a currency. VND's minor unit is the đồng. */
fun minorDigits(currency: String): Int =
    when (currency) {
        "VND", "JPY", "KRW" -> 0
        else -> 2
    }

/** Where a currency's transaction amount decimal precision starts. */
fun startingPrecision(currency: String): Int = minorDigits(currency)

private fun scale(digits: Int): Exact = Exact.parse("1" + "0".repeat(digits))

/** [minor] units as a major amount. 150 USD cents is 1.5. */
fun majorAmount(
    minor: Long,
    currency: String,
): Exact = Exact.fromLong(minor) / scale(minorDigits(currency))

/** A major amount rounded to the currency's minor unit. */
fun minorAmount(
    major: Exact,
    currency: String,
): Long = (major * scale(minorDigits(currency))).roundToIntChecked()

/** Stored quanta as a major amount. */
fun fromQuanta(quanta: Long): Exact = Exact.fromLong(quanta) / scale(AMOUNT_SCALE)

/** A major amount as stored quanta, rounded half away from zero onto [digits]. */
fun toQuanta(
    major: Exact,
    digits: Int,
): Long = (roundDigits(major, digits) * scale(AMOUNT_SCALE)).roundToIntChecked()

/** Half rounds away from zero. */
fun roundDigits(
    amount: Exact,
    digits: Int,
): Exact {
    if (digits <= 0) {
        return Exact.fromLong(amount.roundToIntChecked())
    }
    val factor = scale(digits)
    return Exact.fromLong((amount * factor).roundToIntChecked()) / factor
}
