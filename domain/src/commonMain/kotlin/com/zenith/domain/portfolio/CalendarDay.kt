package com.zenith.domain.portfolio

data class CalendarDay(
    val year: Int,
    val month: Int,
    val day: Int,
)

fun resolveDateOrder(
    order: DateOrder,
    language: AppLanguage,
    phone: DateOrder,
): DateOrder =
    when (order) {
        DateOrder.defaultOrder -> if (language == AppLanguage.vietnamese) DateOrder.dayMonthYear else DateOrder.monthDayYear
        DateOrder.system -> phone
        else -> order
    }

fun formatCalendarDay(
    day: CalendarDay,
    today: CalendarDay,
    format: DateFormat,
    order: DateOrder,
    language: AppLanguage,
): String {
    val resolved = if (order == DateOrder.defaultOrder || order == DateOrder.system) resolveDateOrder(order, language, DateOrder.dayMonthYear) else order
    val numeric = format == DateFormat.numeric || format == DateFormat.numericOnly
    val body = if (numeric) numericDate(day, resolved) else spelledDate(day, today, resolved, language)
    val withDayWord = format == DateFormat.full || format == DateFormat.numeric
    val word = if (withDayWord) dayWord(day, today, language) else null
    return if (word == null) body else "$word, $body"
}

private fun dayWord(
    day: CalendarDay,
    today: CalendarDay,
    language: AppLanguage,
): String? {
    val previous = shiftDay(today, -1)
    return when (day) {
        today -> if (language == AppLanguage.vietnamese) "Hôm nay" else "Today"
        previous -> if (language == AppLanguage.vietnamese) "Hôm qua" else "Yesterday"
        else -> weekdayName(weekday(day), language)
    }
}

private fun spelledDate(
    day: CalendarDay,
    today: CalendarDay,
    order: DateOrder,
    language: AppLanguage,
): String {
    val month = monthName(day.month, language)
    val year = if (day.year == today.year) null else day.year.toString()
    val dayText = day.day.toString()
    return when (order) {
        DateOrder.monthDayYear -> join(month, dayText, year)
        DateOrder.yearMonthDay -> join(year, month, dayText)
        DateOrder.yearDayMonth -> join(year, dayText, month)
        else -> join(dayText, month, year)
    }
}

private fun numericDate(
    day: CalendarDay,
    order: DateOrder,
): String {
    val month = day.month.toString().padStart(2, '0')
    val dayText = day.day.toString().padStart(2, '0')
    val year = day.year.toString()
    return when (order) {
        DateOrder.monthDayYear -> "$month/$dayText/$year"
        DateOrder.yearMonthDay -> "$year/$month/$dayText"
        DateOrder.yearDayMonth -> "$year/$dayText/$month"
        else -> "$dayText/$month/$year"
    }
}

private fun join(vararg parts: String?): String = parts.filterNotNull().joinToString(" ")

private fun monthName(
    month: Int,
    language: AppLanguage,
): String {
    val english =
        listOf(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December",
        )
    return if (language == AppLanguage.vietnamese) "tháng $month" else english[month - 1]
}

private fun weekdayName(
    weekday: Int,
    language: AppLanguage,
): String {
    val english = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val vietnamese = listOf("Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật")
    return if (language == AppLanguage.vietnamese) vietnamese[weekday] else english[weekday]
}

/** Monday is 0. */
private fun weekday(day: CalendarDay): Int {
    val sundayBased =
        intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
    val year = if (day.month < 3) day.year - 1 else day.year
    val leaps = year / 4 - year / 100 + year / 400
    val sunday = (year + leaps + sundayBased[day.month - 1] + day.day) % 7
    return (sunday + 6) % 7
}

private fun shiftDay(
    day: CalendarDay,
    delta: Int,
): CalendarDay {
    val lengths = intArrayOf(31, if (isLeap(day.year)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var dayNumber = day.day + delta
    var month = day.month
    var year = day.year
    while (dayNumber < 1) {
        month -= 1
        if (month < 1) {
            month = 12
            year -= 1
        }
        dayNumber += lengths[month - 1]
    }
    var length = lengths[month - 1]
    while (dayNumber > length) {
        dayNumber -= length
        month += 1
        if (month > 12) {
            month = 1
            year += 1
        }
        length = if (month == 2 && isLeap(year)) 29 else lengths[month - 1]
    }
    return CalendarDay(year, month, dayNumber)
}

private fun isLeap(year: Int): Boolean = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
