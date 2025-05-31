package me.timeto.shared.vm.calendar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.UnixTime
import me.timeto.shared.db.EventDb
import me.timeto.shared.onEachExIn
import me.timeto.shared.vm.Vm

class CalendarDayVm(
    private val unixDay: Int,
) : Vm<CalendarDayVm.State>() {

    data class State(
        val initTime: Int,
        val eventsUi: List<CalendarListVm.EventUi>,
        val inNote: String,
    ) {
        val newEventText = "New Event"
    }

    override val state = MutableStateFlow(
        State(
            initTime = UnixTime.byLocalDay(unixDay).time + (12 * 3_600),
            eventsUi = Cache.eventsDb.toFilterListUi(unixDay),
            inNote = run {
                val today = UnixTime().localDay
                val diff = unixDay - today
                if (diff == 0)
                    "Today"
                else if (diff == 1)
                    "Tomorrow"
                else if (diff > 1)
                    "In $diff days"
                else if (diff == -1)
                    "Yesterday"
                else // < -1
                    "${diff * -1} days ago"
            },
        )
    )

    init {
        val scopeVm = scopeVm()
        EventDb.selectAscByTimeFlow().onEachExIn(scopeVm) { list ->
            state.update {
                it.copy(eventsUi = list.toFilterListUi(unixDay))
            }
        }
    }
}

private fun List<EventDb>.toFilterListUi(
    unixDay: Int,
): List<CalendarListVm.EventUi> = this
    .filter {
        it.getLocalTime().localDay == unixDay
    }
    .map {
        CalendarListVm.EventUi(it)
    }
