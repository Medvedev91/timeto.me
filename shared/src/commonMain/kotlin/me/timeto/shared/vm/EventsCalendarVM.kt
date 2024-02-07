package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import me.timeto.shared.UnixTime
import me.timeto.shared.db.EventDb
import me.timeto.shared.onEachExIn

class EventsCalendarVM : __VM<EventsCalendarVM.State>() {

    data class State(
        val months: List<Month>,
    )

    override val state = MutableStateFlow(
        State(
            months = listOf(), // todo preload
        )
    )

    override fun onAppear() {

        val scope = scopeVM()

        EventDb.getAscByTimeFlow().onEachExIn(scope) { newEvents ->

            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val initDay = LocalDate(now.year, now.monthNumber, 1)

            // todo unlimited years up and down
            val months: List<Month> = (0..(5 * 12)).map { inMonths ->

                val firstDay = initDay.plus(DatePeriod(months = inMonths))
                val lastDay = firstDay.plus(DatePeriod(months = 1, days = -1))

                val firstDayUnixDay = firstDay.toEpochDays()

                val days: List<Month.Day> = (firstDay.dayOfMonth..lastDay.dayOfMonth)
                    .mapIndexed { idx, dayOfMonth ->
                        val unixDay = firstDayUnixDay + idx
                        val previews = mutableListOf("ew") // todo
                        Month.Day(
                            title = "$dayOfMonth",
                            previews = previews,
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

    //

    data class Month(
        val title: String,
        val weeks: List<List<Day?>>,
        val emptyStartDaysCount: Int,
        val emptyEndDaysCount: Int,
    ) {
        data class Day(
            val title: String,
            val previews: List<String>,
        )
    }
}
