package me.timeto.shared.vm.whats_new

import kotlinx.coroutines.flow.MutableStateFlow
import me.timeto.shared.SystemInfo
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
            HistoryItemUi(20499, "Checklists as Goals", text = h20260215Text),
            HistoryItemUi(20493, "Checklist Reset at Start of Day"),
            HistoryItemUi(20486, "Repeating Tasks in Calendar", text = h20260202Text),
            HistoryItemUi(20483, "Shortcuts for Checklist Items", text = h20260130Text),
            HistoryItemUi(20480, "Home Screen Goal's Context Menu", text = h20260127Text),
            HistoryItemUi(20474, "Custom Timer Hints", text = h20260121Text),
            HistoryItemUi(20467, "Goal Context Menu", text = h20260114Text),
            HistoryItemUi(20462, "Support the Developer", text = h20260109Text),
            HistoryItemUi(20416, "New Summary", text = h20251124Text),
            HistoryItemUi(20372, "New Goals", text = h20251011Text),
            HistoryItemUi(20330, "UI Improvements", text = h20250830Text),
            h20250809,
            HistoryItemUi(20265, "Goals Improvements", text = h20250626Text),
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

private val h20250626Text = """
    - many in row goals "Settings -> Home Screen",
    - track entire activity option for goals,
    - rest of bar option for goal's timer, 
    - attaching goal for repeating tasks,
    - auto-remove paused task on goal started,
    - goal bar context menu on long tap,
    - editing "Other" activity.
""".trimIndent()

private val h20250809: WhatsNewVm.HistoryItemUi = run {
    val isAndroid = SystemInfo.instance.os is SystemInfo.Os.Android
    val title: String =
        if (isAndroid) "Persistent Notification" else "Live Activity"
    val text: String = if (isAndroid) """
        - persistent notification with timer and description,
        - lock screen support,
        - [Android 16+] always on display support,
        - [Android 16+] chip in the status bar support.
        """.trimIndent()
    else """
        - live activity with timer and description,
        - lock screen support,
        - always on display support,
        - dynamic island support.
        """.trimIndent()
    WhatsNewVm.HistoryItemUi(20309, title = title, text = text)
}

private val h20250830Text = """
    - new checklists UI,
    - date on home screen.
""".trimIndent()

private val h20251011Text = """
    - activities and goals are merging,
    - new "Settings -> Goals" screen,
    - goals can have a parent goal to provide any level of nesting.
""".trimIndent()

private val h20251124Text = """
    - new summary UI,
    - accessing hidden goals.
""".trimIndent()

private val h20260109Text = """
    Two options on the settings screen:
    - Write a Review on ${if (SystemInfo.instance.isAndroid) "Google Play" else "App Store"},
    - Star on GitHub.
""".trimIndent()

private val h20260114Text = """
    Two new options for long tap on goal:
    - Set Timer,
    - Until Time.
""".trimIndent()

private val h20260121Text = """
    Set custom timer hints for goals and start from:
    - home screen context menu,
    - goals list.
""".trimIndent()

private val h20260127Text = """
    - child goals in the context menu,
    - "timer picker" option for the "timer on bar pressed" setting.
""".trimIndent()

private val h20260130Text = """
    Shortcuts and nested checklists for checklist items.
""".trimIndent()

private val h20260202Text = """
    Enable "Display in Calendar" option in the repeating task settings to display this task in calendar.
""".trimIndent()

private val h20260215Text = """
    Completing the checklist marks the goal as completed.
""".trimIndent()
