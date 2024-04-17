package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.UnixTime

class WhatsNewVm : __VM<WhatsNewVm.State>() {

    data class State(
        val historyItemsUi: List<HistoryItemUi>,
    ) {
        val headerTitle = "What's New"
    }

    override val state = MutableStateFlow(
        State(
            historyItemsUi = prepHistoryItems(),
        )
    )

    ///

    data class HistoryItemUi(
        val build: Int,
        val unixDay: Int,
        val text: String,
    ) {

        val title: String

        init {
            val unixTime = UnixTime.byLocalDay(unixDay)
            title = unixTime.getStringByComponents(
                UnixTime.StringComponent.dayOfMonth,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.month3,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.year,
            )
        }
    }
}

private fun prepHistoryItems(): List<WhatsNewVm.HistoryItemUi> = listOf(
    WhatsNewVm.HistoryItemUi(488, 19823, "Checklist Sorting"),
    WhatsNewVm.HistoryItemUi(480, 19766, "New Calendar"),
)
