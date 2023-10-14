package me.timeto.shared

import kotlinx.datetime.*

data class UnixTime(
    val time: Int = time(),
    val utcOffset: Int = localUtcOffset,
) {

    companion object {

        val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val monthNames3 = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val dayOfWeekNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val dayOfWeekNames1 = listOf("M", "T", "W", "T", "F", "S", "S")
        val dayOfWeekNames2 = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
        val dayOfWeekNames3 = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        const val MAX_DAY = 22571 // 19 October 2031...
        const val MAX_TIME = 1_950_134_400 // ...40 years old

        fun byLocalDay(localDay: Int, utcOffset: Int = localUtcOffset) =
            UnixTime(time = (localDay * 86_400) - utcOffset, utcOffset = utcOffset)

        fun byUtcTime(utcTime: Int, utcOffset: Int = localUtcOffset) =
            UnixTime(time = utcTime - utcOffset, utcOffset = utcOffset)
    }

    val localDay = (time + utcOffset) / 86_400

    fun localDayStartTime() = (localDay * 86_400) - utcOffset

    fun isToday() = localDay == UnixTime().localDay

    fun utcTime() = time + utcOffset

    // 0 - Mon. 1 Jan 1970 - Thu.
    fun dayOfWeek() = when (localDay % 7) {
        0 -> 3
        1 -> 4
        2 -> 5
        3 -> 6
        4 -> 0
        5 -> 1
        6 -> 2
        else -> throw Exception()
    }

    fun inDays(days: Int): UnixTime = copy(time = time + (days * 86_400))

    fun inSeconds(seconds: Int): UnixTime = copy(time = time + seconds)

    fun year(): Int =
        Instant.fromEpochSeconds(localDay * 86_400L).toLocalDateTime(TimeZone.UTC).year

    // 1 - Jan
    fun month(): Int =
        Instant.fromEpochSeconds(localDay * 86_400L).toLocalDateTime(TimeZone.UTC).monthNumber

    // 1..31
    fun dayOfMonth(): Int =
        Instant.fromEpochSeconds(localDay * 86_400L).toLocalDateTime(TimeZone.UTC).dayOfMonth

    fun lastUnixDayOfMonth(): UnixTime {
        val year = year()
        val lastDayOfMonth = when (month()) {
            in setOf(4, 6, 9, 11) -> 30
            in setOf(1, 3, 5, 7, 8, 10, 12) -> 31
            2 -> when {
                (year % 400) == 0 -> 29
                (year % 100) == 0 -> 28
                (year % 4) == 0 -> 29
                else -> 28
            }
            else -> throw Exception()
        }
        return byLocalDay(localDay + lastDayOfMonth - dayOfMonth(), utcOffset)
    }

    fun getStringByComponents(
        vararg components: StringComponent,
    ): String {
        val dayTime = Instant.fromEpochSeconds(localDay * 86_400L).toLocalDateTime(TimeZone.UTC)
        return components.joinToString("") { component ->
            when (component) {
                StringComponent.month -> monthNames[dayTime.monthNumber - 1]
                StringComponent.month3 -> monthNames3[dayTime.monthNumber - 1]
                StringComponent.dayOfMonth -> dayTime.dayOfMonth.toString()
                StringComponent.dayOfWeek -> dayOfWeekNames[dayOfWeek()]
                StringComponent.dayOfWeek2 -> dayOfWeekNames2[dayOfWeek()]
                StringComponent.dayOfWeek3 -> dayOfWeekNames3[dayOfWeek()]
                StringComponent.hhmm24 -> {
                    val (h, m) = (utcTime() % 86_400).toHms()
                    "$h".padStart(2, '0') + ":" + "$m".padStart(2, '0')
                }
                StringComponent.space -> " "
                StringComponent.comma -> ","
            }
        }
    }

    fun getStringByComponents(components: List<StringComponent>): String =
        getStringByComponents(*components.toTypedArray())

    enum class StringComponent {
        month, month3, dayOfMonth, dayOfWeek, dayOfWeek2, dayOfWeek3, hhmm24, space, comma,
    }
}

//
// TODO

//val time = ymdToTime(2003, 12, 14)
//zlog(time)
//zlog(timeToYmd(time))

// todo hms
// todo time zone
// todo if year = 1970
// todo if year < 1970
// todo if year < 0
// todo m,d,h,m,s validation
private fun ymdToTime(y: Int, m: Int, d: Int): Int {
    val prevY = y - 1
    val daysJesusUntilYear = (prevY * 365) + (prevY / 400) - (prevY / 100) + (prevY / 4)
    val prevYearsDays = daysJesusUntilYear - daysJesusUntilUnix
    val daysInMonthInc = if (isLeapYear(y)) daysInMonthIncLeap else daysInMonthIncCommon
    val prevMonthsDays = daysInMonthInc[m - 1]
    return (prevYearsDays + prevMonthsDays + (d - 1)) * 86_400
}

// todo refactor + clean?
// todo hms
// todo time zone
// todo if time = 0
// todo if time < 0
private fun timeToYmd(time: Int): List<Int> {
    val daysUnixUntilNow = time / 86_400
    val daysJesusUntilNow = daysUnixUntilNow + daysJesusUntilUnix
    val prevY = (daysJesusUntilNow * 400) / 146_097 // 146_097 days in 400 years
    val daysJesusUntilYear = (prevY * 365) + (prevY / 400) - (prevY / 100) + (prevY / 4)
    val y = prevY + 1
    val daysInMonthInc = if (isLeapYear(y)) daysInMonthIncLeap else daysInMonthIncCommon
    val leftSeconds = time - ((daysJesusUntilYear - daysJesusUntilUnix) * 86_400)
    val leftDays = leftSeconds / 86_400
    val m = daysInMonthInc.indexOfFirst { it > leftDays }
    val d = leftDays - daysInMonthInc[m - 1]
    return listOf(y, m, d + 1)
}

private fun isLeapYear(y: Int) = when {
    (y % 400) == 0 -> true
    (y % 100) == 0 -> false
    (y % 4) == 0 -> true
    else -> false
}

private const val daysJesusUntilUnix = (1969 * 365) + (1969 / 400) - (1969 / 100) + (1969 / 4)
private val daysInMonthIncLeap = listOf(0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366)
private val daysInMonthIncCommon = listOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365)
