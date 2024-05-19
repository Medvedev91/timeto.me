package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.UnixTime
import me.timeto.shared.db.KvDb
import me.timeto.shared.launchExDefault

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

    override fun onAppear() {
        val lastUnixDay: Int = state.value.historyItemsUi.first().unixDay
        launchExDefault {
            KvDb.KEY.WHATS_NEW_CHECK_UNIX_DAY.upsertInt(lastUnixDay)
        }
    }

    ///

    companion object {

        fun prepHistoryItemsUi(): List<HistoryItemUi> = listOf(
            HistoryItemUi(19858, "Optional Pomodoro for Each Activity"),
            HistoryItemUi(19844, "New Calendar Event Form"),
            HistoryItemUi(19831, "What's New Changelog"),
            HistoryItemUi(19823, "Checklist Sorting"),
            HistoryItemUi(19766, "New Calendar"),
        )
    }

    ///

    data class HistoryItemUi(
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
