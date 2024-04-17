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
            historyItemsUi = prepHistoryItemsUi(),
        )
    )

    ///

    companion object {

        fun prepHistoryItemsUi(): List<HistoryItemUi> = listOf(
            HistoryItemUi(488, 19823, "Checklist Sorting"),
            HistoryItemUi(480, 19766, "New Calendar"),
        )
    }

    ///

    data class HistoryItemUi(
        val build: Int,
        val unixDay: Int,
        val text: String,
    ) {

        val title: String
        val timeAgoText: String

        init {
            val unixTime = UnixTime.byLocalDay(unixDay)
            val today = UnixTime().localDay
            title = unixTime.getStringByComponents(
                UnixTime.StringComponent.dayOfMonth,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.month3,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.year,
            )
            val daysAgo = today - unixDay
            timeAgoText = when {
                daysAgo == 0 -> "Today"
                daysAgo > 365 -> "${daysAgo / 365}y"
                daysAgo > 30 -> "${daysAgo / 30}mo"
                else -> "${daysAgo}d"
            }
        }
    }
}
