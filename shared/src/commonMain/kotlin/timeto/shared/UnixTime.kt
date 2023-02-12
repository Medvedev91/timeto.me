package timeto.shared

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class UnixTime(
    val time: Int = time(),
) {

    companion object {

        val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val monthNames3 = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val dayOfWeekNames3 = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        const val MAX_DAY = 22571 // 19 October 2031...
        const val MAX_TIME = 1_950_134_400 // ...40 years old

        fun byLocalDay(localDay: Int) = UnixTime((localDay * 86_400) - utcOffset)

        fun byUtcTime(utcTime: Int) = UnixTime(utcTime - utcOffset)
    }

    val localDay = (time + utcOffset) / 86_400

    fun localDayStartTime() = (localDay * 86_400) - utcOffset

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

    fun inDays(days: Int) = UnixTime(time + (days * 86_400))

    fun inSeconds(seconds: Int) = UnixTime(time + seconds)

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
        return byLocalDay(localDay + lastDayOfMonth - dayOfMonth())
    }

    fun getStringByComponents(
        components: List<StringComponent>,
    ): String {
        val dayTime = Instant.fromEpochSeconds(localDay * 86_400L).toLocalDateTime(TimeZone.UTC)
        return components.joinToString("") { component ->
            when (component) {
                StringComponent.month -> monthNames[dayTime.monthNumber - 1]
                StringComponent.month3 -> monthNames3[dayTime.monthNumber - 1]
                StringComponent.dayOfMonth -> dayTime.dayOfMonth.toString()
                StringComponent.dayOfWeek3 -> dayOfWeekNames3[dayOfWeek()]
                StringComponent.hhmm24 -> {
                    val hms = (utcTime() % 86_400).toHms()
                    "${hms[0]}".padStart(2, '0') + ":" + "${hms[1]}".padStart(2, '0')
                }
                StringComponent.space -> " "
                StringComponent.comma -> ","
            }
        }
    }

    enum class StringComponent {
        month, month3, dayOfMonth, dayOfWeek3, hhmm24, space, comma
    }
}
