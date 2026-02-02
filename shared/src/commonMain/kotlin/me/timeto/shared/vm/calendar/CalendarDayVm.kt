package me.timeto.shared.vm.calendar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.UnixTime
import me.timeto.shared.db.EventDb
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.vm.Vm
import me.timeto.shared.vm.tasks.tab.repeatings.TasksTabRepeatingsVm

class CalendarDayVm(
    private val unixDay: Int,
) : Vm<CalendarDayVm.State>() {

    data class State(
        val initTime: Int,
        val itemsUi: List<ItemUi>,
        val inNote: String,
    ) {
        val newEventText = "New Event"
    }

    override val state = MutableStateFlow(
        State(
            initTime = UnixTime.byLocalDay(unixDay).time + (12 * 3_600),
            itemsUi = buildItemsUi(unixDay, Cache.eventsDb, Cache.repeatingsDb),
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
        combine(
            EventDb.selectAscByTimeFlow(),
            RepeatingDb.selectAscFlow(),
        ) { eventsDb, repeatingsDb ->
            state.update {
                it.copy(itemsUi = buildItemsUi(unixDay, eventsDb, repeatingsDb))
            }
        }.launchIn(scopeVm)
    }

    ///

    sealed class ItemUi {

        data class EventUi(
            val calendarListEventUi: CalendarListVm.EventUi,
        ) : ItemUi()

        data class RepeatingUi(
            val repeatingsListRepeatingUi: TasksTabRepeatingsVm.RepeatingUi,
        ) : ItemUi()
    }
}

private fun buildItemsUi(
    unixDay: Int,
    allEventsDb: List<EventDb>,
    allRepeatingsDb: List<RepeatingDb>,
): List<CalendarDayVm.ItemUi> = listOf(
    allEventsDb.filter { it.getLocalTime().localDay == unixDay }.map { eventDb ->
        CalendarDayVm.ItemUi.EventUi(CalendarListVm.EventUi(eventDb))
    },
    allRepeatingsDb.filter { it.inCalendar && it.isInDay(unixDay) }.map { repeatingDb ->
        CalendarDayVm.ItemUi.RepeatingUi(TasksTabRepeatingsVm.RepeatingUi(repeatingDb))
    },
).flatten()
