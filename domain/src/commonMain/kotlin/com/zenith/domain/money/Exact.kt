package com.zenith.domain.money

import com.ionspin.kotlin.bignum.integer.BigInteger

/** An exact decimal. The domain never uses binary floating point. */
class Exact private constructor(
    private val numerator: BigInteger,
    private val denominator: BigInteger,
) : Comparable<Exact> {
    val isZero: Boolean get() = numerator == BigInteger.ZERO
    val isNegative: Boolean get() = numerator < BigInteger.ZERO
    val isPositive: Boolean get() = numerator > BigInteger.ZERO
    val sign: Int get() = numerator.signum()

    operator fun plus(other: Exact): Exact = Exact(numerator * other.denominator + other.numerator * denominator, denominator * other.denominator).reduced

    operator fun unaryMinus(): Exact = Exact(-numerator, denominator)

    operator fun minus(other: Exact): Exact = this + -other

    operator fun times(other: Exact): Exact = Exact(numerator * other.numerator, denominator * other.denominator).reduced

    operator fun div(other: Exact): Exact {
        check(!other.isZero) { "division by zero" }
        val denominator = this.denominator * other.numerator.abs()
        var numerator = this.numerator * other.denominator
        if (other.isNegative) {
            numerator = -numerator
        }
        return Exact(numerator, denominator).reduced
    }

    /** Nearest integer, with halves away from zero. */
    fun roundToInt(): BigInteger {
        val negative = isNegative
        val numerator = this.numerator.abs()
        var whole = numerator / this.denominator
        val remainder = numerator % this.denominator
        if (remainder * BigInteger.TWO >= this.denominator) {
            whole += BigInteger.ONE
        }
        return if (negative) -whole else whole
    }

    fun roundToIntChecked(): Long {
        val rounded = roundToInt()
        check(rounded <= maxSafe && rounded >= -maxSafe) { "amount does not fit" }
        return rounded.longValue()
    }

    /** Digits after the point. A repeating expansion stops after 24 digits. */
    fun formatPlain(): String {
        if (isZero) {
            return "0"
        }
        val negative = isNegative
        val numerator = this.numerator.abs()
        val whole = numerator / denominator
        var remainder = numerator % denominator
        val digits = StringBuilder()
        while (remainder != BigInteger.ZERO && digits.length < MAX_FRACTION_DIGITS) {
            remainder *= BigInteger.TEN
            digits.append((remainder / denominator).toString())
            remainder %= denominator
        }
        val body = if (digits.isEmpty()) whole.toString() else "$whole.$digits"
        return if (negative) "-$body" else body
    }

    override fun compareTo(other: Exact): Int = (numerator * other.denominator).compareTo(other.numerator * denominator)

    override fun equals(other: Any?): Boolean = other is Exact && compareTo(other) == 0

    override fun hashCode(): Int {
        val reduced = reduced
        return 31 * reduced.numerator.hashCode() + reduced.denominator.hashCode()
    }

    private val reduced: Exact
        get() {
            if (denominator == BigInteger.ONE) {
                return this
            }
            var divisor = numerator.abs().gcd(denominator)
            if (divisor == BigInteger.ZERO) {
                divisor = BigInteger.ONE
            }
            return Exact(numerator / divisor, denominator / divisor)
        }

    companion object {
        val zero: Exact = Exact(BigInteger.ZERO, BigInteger.ONE)
        val one: Exact = Exact(BigInteger.ONE, BigInteger.ONE)

        private const val MAX_FRACTION_DIGITS = 24
        private val maxSafe = BigInteger.parseString("9007199254740991")

        fun fromInt(value: Int): Exact = fromLong(value.toLong())

        fun fromLong(value: Long): Exact = Exact(BigInteger.fromLong(value), BigInteger.ONE)

        fun parse(input: String): Exact {
            val text = input.trim()
            require(text.isNotEmpty()) { "empty amount" }
            var body = text
            var negative = false
            if (body.startsWith("-")) {
                negative = true
                body = body.substring(1)
            } else if (body.startsWith("+")) {
                body = body.substring(1)
            }
            val parts = body.split('.')
            require(parts.size <= 2 && parts.none { part -> part.isEmpty() || !part.all { it.isDigit() } }) {
                "invalid amount"
            }
            val scale = BigInteger.TEN.pow((if (parts.size == 2) parts[1].length else 0).toLong())
            var numerator = BigInteger.parseString(parts[0]) * scale
            if (parts.size == 2) {
                numerator += BigInteger.parseString(parts[1])
            }
            if (negative) {
                numerator = -numerator
            }
            return Exact(numerator, scale).reduced
        }

        fun fromJson(value: String): Exact = parse(value)
    }
}

fun Exact.abs(): Exact = if (isNegative) -this else this

fun Exact.toJson(): String = formatPlain()
