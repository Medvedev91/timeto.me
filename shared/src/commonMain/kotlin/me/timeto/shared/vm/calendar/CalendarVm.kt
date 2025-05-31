package me.timeto.shared.vm.calendar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import me.timeto.shared.UnixTime
import me.timeto.shared.db.EventDb
import me.timeto.shared.onEachExIn
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.__Vm

class CalendarVm : __Vm<CalendarVm.State>() {

    data class State(
        val months: List<Month>,
        val weekTitles: List<WeekTitle>,
    )

    override val state = MutableStateFlow(
        State(
            months = listOf(), // todo preload
            weekTitles = listOf(
                WeekTitle("MO", true),
                WeekTitle("TU", true),
                WeekTitle("WE", true),
                WeekTitle("TH", true),
                WeekTitle("FR", true),
                WeekTitle("SA", false),
                WeekTitle("SU", false),
            )
        )
    )

    init {

        val scope = scopeVm()

        EventDb.selectAscByTimeFlow().onEachExIn(scope) { newEvents ->

            val newEventsMapByDays = newEvents.groupBy { it.getLocalTime().localDay }

            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val initDay = LocalDate(now.year, now.monthNumber, 1)
            val todayUnixDay = UnixTime().localDay

            // todo unlimited years up and down
            val months: List<Month> = (0..(5 * 12)).map { inMonths ->

                val firstDay = initDay.plus(DatePeriod(months = inMonths))
                val lastDay = firstDay.plus(DatePeriod(months = 1, days = -1))

                val firstDayUnixDay = firstDay.toEpochDays()

                val days: List<Month.Day> = (firstDay.dayOfMonth..lastDay.dayOfMonth)
                    .mapIndexed { idx, dayOfMonth ->
                        val unixDay = firstDayUnixDay + idx
                        val dayEvents: List<EventDb> = newEventsMapByDays[unixDay] ?: listOf()
                        val previews: List<String> = if (dayEvents.size > 3) {
                            dayEvents.take(2).map { it.text.textFeatures().textNoFeatures } +
                            "+${dayEvents.size - 2}"
                        } else {
                            dayEvents.map { it.text.textFeatures().textNoFeatures } +
                            (0 until (3 - dayEvents.size)).map { "" }
                        }
                        val weekRem = unixDay % 7
                        Month.Day(
                            unixDay = unixDay,
                            title = "$dayOfMonth",
                            previews = previews,
                            isBusiness = !(weekRem == 2 || weekRem == 3),
                            isToday = todayUnixDay == unixDay,
                        )
                    }

                val monthDays: MutableList<Month.Day?> = mutableListOf()
                val emptyStartDaysCount = firstDay.dayOfWeek.ordinal
                monthDays.addAll(arrayOfNulls<Month.Day>(emptyStartDaysCount))
                monthDays.addAll(days)
                val emptyEndDaysCount = monthDays.size % 7
                if (emptyEndDaysCount > 0)
                    monthDays.addAll(arrayOfNulls<Month.Day>(7 - emptyEndDaysCount))

                val month = Month(
                    title = UnixTime.monthNames3[firstDay.monthNumber - 1],
                    weeks = monthDays.chunked(7),
                    emptyStartDaysCount = emptyStartDaysCount,
                    emptyEndDaysCount = 7 - 1 - emptyStartDaysCount,
                )
                month
            }

            state.update { it.copy(months = months) }
        }
    }

    ///

    data class Month(
        val title: String,
        val weeks: List<List<Day?>>,
        val emptyStartDaysCount: Int,
        val emptyEndDaysCount: Int,
    ) {
        data class Day(
            val unixDay: Int,
            val title: String,
            val previews: List<String>,
            val isBusiness: Boolean,
            val isToday: Boolean,
        )
    }

    data class WeekTitle(
        val title: String,
        val isBusiness: Boolean,
    )
}
