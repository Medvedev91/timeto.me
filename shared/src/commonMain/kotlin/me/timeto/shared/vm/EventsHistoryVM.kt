package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.KVModel

class EventsHistoryVM : __VM<EventsHistoryVM.State>() {

    data class UiItem(
        val historyItem: EventsHistory.Item,
        val defTime: Int,
        val note: String,
    )

    data class State(
        val uiItems: List<UiItem>
    )

    override val state: MutableStateFlow<State>

    init {
        val history = EventsHistory.buildFromDI()
        state = MutableStateFlow(
            State(
                uiItems = history.items.toUiItems()
            )
        )
    }

    override fun onAppear() {
        KVModel.KEY.EVENTS_HISTORY
            .getOrNullFlow()
            .filterNotNull()
            .onEachExIn(scopeVM()) { kv ->
                state.update {
                    it.copy(uiItems = EventsHistory.buildFromJString(kv.value).items.toUiItems())
                }
            }
    }

    fun delItem(item: EventsHistory.Item) {
        showUiConfirmation(
            UIConfirmationData(
                text = "Remove \"${item.raw_title}\" from Recent?",
                buttonText = "Remove",
                isRed = true,
            ) {
                scopeVM().launchEx {
                    EventsHistory.delete(item)
                }
            }
        )
    }

    private fun List<EventsHistory.Item>.toUiItems() = this.map { historyItem ->
        val dayTimeString = run {
            if (historyItem.daytime == 0)
                return@run ""
            val hms = historyItem.daytime.toHms()
            " " + ("${hms[0]}".padStart(2, '0')) + ":" + ("${hms[1]}".padStart(2, '0'))
        }

        val rawTitleSized = run {
            if (historyItem.raw_title.length <= 12)
                historyItem.raw_title
            else
                historyItem.raw_title.substring(0..9) + ".."
        }
        UiItem(
            historyItem = historyItem,
            defTime = UnixTime().localDayStartTime() + historyItem.daytime,
            note = rawTitleSized + dayTimeString,
        )
    }
}
