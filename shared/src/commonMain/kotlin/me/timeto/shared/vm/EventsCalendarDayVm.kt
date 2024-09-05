package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.UnixTime
import me.timeto.shared.db.EventDb
import me.timeto.shared.onEachExIn

class EventsCalendarDayVm(
    val unixDay: Int,
) : __Vm<EventsCalendarDayVm.State>() {

    data class State(
        val formDefTime: Int,
        val eventsUi: List<EventsListVm.EventUi>,
        val inNote: String,
    ) {
        val newEventBtnText = "New Event"
    }

    override val state = MutableStateFlow(
        State(
            formDefTime = UnixTime.byLocalDay(unixDay).time + (12 * 3_600),
            eventsUi = listOf(), // todo preload
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

    override fun onAppear() {
        val scope = scopeVM()
        EventDb.getAscByTimeFlow()
            .onEachExIn(scope) { list ->
                state.update {
                    it.copy(eventsUi = list.toFilterListUi(unixDay))
                }
            }
    }
}

private fun List<EventDb>.toFilterListUi(
    unixDay: Int,
) = filter { it.getLocalTime().localDay == unixDay }
    .map { EventsListVm.EventUi(it) }
