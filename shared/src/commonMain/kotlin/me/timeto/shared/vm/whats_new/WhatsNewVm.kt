package me.timeto.shared.vm.whats_new

import kotlinx.coroutines.flow.MutableStateFlow
import me.timeto.shared.UnixTime
import me.timeto.shared.db.KvDb
import me.timeto.shared.launchExIo
import me.timeto.shared.vm.Vm

class WhatsNewVm : Vm<WhatsNewVm.State>() {

    data class State(
        val tmp: Int = 1,
    ) {
        val title = "What's New"
        val historyItemsUi: List<HistoryItemUi> =
            WhatsNewVm.historyItemsUi
    }

    override val state = MutableStateFlow(State())

    init {
        launchExIo {
            KvDb.KEY.WHATS_NEW_CHECK_UNIX_DAY.upsertInt(
                historyItemsUi.first().unixDay,
            )
        }
    }

    ///

    companion object {

        val historyItemsUi: List<HistoryItemUi> = listOf(
            HistoryItemUi(20247, "Move to Tasks From History"),
            HistoryItemUi(20044, "New Goals"),
            HistoryItemUi(19939, "Today on Home Screen", text = "Can be disabled in settings."),
            HistoryItemUi(19912, "New Pomodoro", buttonUi = HistoryItemUi.ButtonUi.pomodoro),
            HistoryItemUi(19870, "Calendar Templates"),
            HistoryItemUi(19858, "Optional Pomodoro"),
            HistoryItemUi(19844, "New Calendar Form"),
            HistoryItemUi(19831, "What's New Changelog"),
            HistoryItemUi(19823, "Checklist Sorting"),
            HistoryItemUi(19766, "New Calendar"),
        )
    }

    ///

    data class HistoryItemUi(
        val unixDay: Int,
        val title: String,
        val text: String? = null,
        val buttonUi: ButtonUi? = null,
    ) {

        val dateText: String
        val timeAgoText: String

        init {
            val unixTime = UnixTime.byLocalDay(unixDay)
            val today = UnixTime().localDay
            dateText = unixTime.getStringByComponents(
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

        ///

        enum class ButtonUi(val text: String) {
            pomodoro("Read how it works"),
        }
    }
}
