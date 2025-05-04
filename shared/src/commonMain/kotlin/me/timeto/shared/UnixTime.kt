package me.timeto.shared

import kotlinx.datetime.*
import me.timeto.shared.misc.time

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

        fun byLocalDay(localDay: Int, utcOffset: Int = localUtcOffset): UnixTime =
            UnixTime(time = (localDay * 86_400) - utcOffset, utcOffset = utcOffset)

        fun byUtcTime(utcTime: Int, utcOffset: Int = localUtcOffset): UnixTime =
            UnixTime(time = utcTime - utcOffset, utcOffset = utcOffset)
    }

    val localDay: Int =
        (time + utcOffset) / 86_400

    fun localDayStartTime(): Int =
        (localDay * 86_400) - utcOffset

    fun isToday(): Boolean =
        localDay == UnixTime().localDay

    fun utcTime(): Int =
        time + utcOffset

    // 0 - Mon. 1 Jan 1970 - Thu.
    fun dayOfWeek(): Int = when (localDay % 7) {
        0 -> 3
        1 -> 4
        2 -> 5
        3 -> 6
        4 -> 0
        5 -> 1
        6 -> 2
        else -> throw Exception()
    }

    fun inDays(days: Int): UnixTime =
        copy(time = time + (days * 86_400))

    fun inSeconds(seconds: Int): UnixTime =
        copy(time = time + seconds)

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
        val dateTime = Instant.fromEpochSeconds(localDay * 86_400L).toLocalDateTime(TimeZone.UTC)
        return components.joinToString("") { component ->
            when (component) {
                StringComponent.space -> " "
                StringComponent.comma -> ","
                StringComponent.hhmm24 -> {
                    val dayTime = utcTime() % 86_400
                    val (h, m) = listOf(dayTime / 3600, (dayTime % 3_600) / 60)
                    "$h".padStart(2, '0') + ":" + "$m".padStart(2, '0')
                }
                StringComponent.dayOfWeek -> dayOfWeekNames[dayOfWeek()]
                StringComponent.dayOfWeek2 -> dayOfWeekNames2[dayOfWeek()]
                StringComponent.dayOfWeek3 -> dayOfWeekNames3[dayOfWeek()]
                StringComponent.dayOfMonth -> dateTime.dayOfMonth.toString()
                StringComponent.month -> monthNames[dateTime.monthNumber - 1]
                StringComponent.month3 -> monthNames3[dateTime.monthNumber - 1]
                StringComponent.year -> dateTime.year.toString()
            }
        }
    }

    fun getStringByComponents(components: List<StringComponent>): String =
        getStringByComponents(*components.toTypedArray())

    enum class StringComponent {
        space, comma,
        hhmm24,
        dayOfWeek, dayOfWeek2, dayOfWeek3,
        dayOfMonth,
        month, month3,
        year,
    }
}
