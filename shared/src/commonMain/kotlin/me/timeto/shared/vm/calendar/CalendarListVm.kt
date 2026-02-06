package me.timeto.shared.vm.calendar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.TextFeatures
import me.timeto.shared.TimeFlows
import me.timeto.shared.UnixTime
import me.timeto.shared.db.EventDb
import me.timeto.shared.onEachExIn
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.Vm

class CalendarListVm : Vm<CalendarListVm.State>() {

    data class State(
        val curTimeString: String,
        val eventsUi: List<EventUi>,
    )

    override val state = MutableStateFlow(
        State(
            curTimeString = getCurTimeString(),
            eventsUi = Cache.eventsDb.toUiList()
        )
    )

    init {
        val scopeVm = scopeVm()
        TimeFlows.eachMinuteSecondsFlow.onEachExIn(scopeVm) {
            state.update { it.copy(curTimeString = getCurTimeString()) }
        }
        EventDb.selectAscByTimeFlow().onEachExIn(scopeVm) { eventsDb ->
            state.update { it.copy(eventsUi = eventsDb.toUiList()) }
        }
    }

    ///

    class EventUi(
        val eventDb: EventDb,
    ) {

        val textFeatures: TextFeatures =
            eventDb.text.textFeatures()

        val dayLeftString: String =
            "${eventDb.getLocalTime().localDay - UnixTime().localDay}d"

        val dateString: String =
            eventDb.getLocalTime().eventListDateString()

        val listText: String =
            textFeatures.textUi()
    }
}

private fun UnixTime.eventListDateString(): String =
    this.getStringByComponents(
        UnixTime.StringComponent.dayOfMonth,
        UnixTime.StringComponent.space,
        UnixTime.StringComponent.month3,
        UnixTime.StringComponent.comma,
        UnixTime.StringComponent.space,
        UnixTime.StringComponent.dayOfWeek3,
        UnixTime.StringComponent.space,
        UnixTime.StringComponent.hhmm24,
    )

private fun getCurTimeString(): String =
    UnixTime().getStringByComponents(
        UnixTime.StringComponent.dayOfMonth,
        UnixTime.StringComponent.space,
        UnixTime.StringComponent.month3,
        UnixTime.StringComponent.comma,
        UnixTime.StringComponent.space,
        UnixTime.StringComponent.dayOfWeek3,
        UnixTime.StringComponent.space,
        UnixTime.StringComponent.hhmm24
    )

private fun List<EventDb>.toUiList() =
    map { CalendarListVm.EventUi(it) }
