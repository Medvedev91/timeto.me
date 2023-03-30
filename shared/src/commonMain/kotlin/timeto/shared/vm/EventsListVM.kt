package timeto.shared.vm

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timeto.shared.*
import timeto.shared.db.EventModel
import timeto.shared.db.eventUiString

class EventsListVM : __VM<EventsListVM.State>() {

    class UiEvent(
        val event: EventModel,
    ) {
        val deletionNote = "Are you sure you want to delete \"${event.text}\" event?"
        val dayLeftString = "${event.getLocalTime().localDay - UnixTime().localDay}d"

        val textFeatures = event.text.textFeatures()
        val dateString = event.getLocalTime().eventListDateString()
        val listText = textFeatures.textUi()

        fun delete() {
            launchExDefault {
                event.delete()
            }
        }
    }

    data class State(
        val curTimeString: String,
        val uiEvents: List<UiEvent>,
    )

    override val state = MutableStateFlow(
        State(
            curTimeString = getCurTimeString(),
            uiEvents = DI.events.toUiList()
        )
    )

    override fun onAppear() {
        scopeVM().launch {
            while (true) {
                delay(1_000)
                state.update { it.copy(curTimeString = getCurTimeString()) }
            }
        }
        EventModel.getAscByTimeFlow()
            .onEachExIn(scopeVM()) { list ->
                state.update { it.copy(uiEvents = list.toUiList()) }
            }
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

private fun getCurTimeString() =
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

private fun List<EventModel>.toUiList() = map { EventsListVM.UiEvent(it) }
