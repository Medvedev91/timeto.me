package me.timeto.shared.vm

import kotlinx.coroutines.flow.*

class WhatsNewVm : __VM<WhatsNewVm.State>() {

    data class State(
        val historyItems: List<HistoryItem>,
    ) {
        val headerTitle = "What's New"
    }

    override val state = MutableStateFlow(
        State(
            historyItems = prepHistoryItems(),
        )
    )

    ///

    data class HistoryItem(
        val build: Int,
        val unixDay: Int,
        val title: String,
        val text: String,
    )
}

private fun prepHistoryItems(): List<WhatsNewVm.HistoryItem> = listOf(
)
