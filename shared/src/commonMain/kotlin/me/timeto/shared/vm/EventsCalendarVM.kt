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

                val dayNumbers: MutableList<Int?> = mutableListOf()
                val emptyStartDaysCount = firstDay.dayOfWeek.ordinal
                dayNumbers.addAll(arrayOfNulls<Int>(emptyStartDaysCount))
                dayNumbers.addAll((firstDay.dayOfMonth..lastDay.dayOfMonth))
                val emptyEndDaysCount = dayNumbers.size % 7
                if (emptyEndDaysCount > 0)
                    dayNumbers.addAll(arrayOfNulls<Int>(7 - emptyEndDaysCount))

                val days: List<Month.Day?> = dayNumbers.map { dayNumber ->
                    if (dayNumber == null) null
                    else {
                        Month.Day(
                            title = "$dayNumber",
                        )
                    }
                }
                val month = Month(
                    title = UnixTime.monthNames3[firstDay.monthNumber - 1],
                    weeks = days.chunked(7),
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
        )
    }
}
