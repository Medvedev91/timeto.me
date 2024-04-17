package me.timeto.shared.vm

import kotlinx.coroutines.flow.*

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
        val title: String,
        val text: String,
    )
}

private fun prepHistoryItems(): List<WhatsNewVm.HistoryItemUi> = listOf(
)
