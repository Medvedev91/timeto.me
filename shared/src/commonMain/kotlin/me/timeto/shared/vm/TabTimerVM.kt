package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.ActivityModel
import me.timeto.shared.db.IntervalModel
import me.timeto.shared.ui.IntervalNoteUI
import me.timeto.shared.ui.TimerHintUI

class TabTimerVM : __VM<TabTimerVM.State>() {

    class ActivityUI(
        val activity: ActivityModel,
        val noteUI: IntervalNoteUI?,
        val isActive: Boolean,
        val withTopDivider: Boolean,
    ) {

        val timerHints = TimerHintUI.buildList(
            activity,
            isShort = true,
            historyLimit = 2,
            customLimit = 5
        ) { seconds ->
            activity.startInterval(seconds)
        }

        val deletionHint: String
        val deletionConfirmation: String

        val textFeatures = activity.name.textFeatures()
        val listText = textFeatures.textUi()

        init {
            val nameWithEmojiNoTriggers = activity.nameWithEmoji().textFeatures().textUi()
            deletionHint = nameWithEmojiNoTriggers
            deletionConfirmation = "Are you sure you want to delete \"$nameWithEmojiNoTriggers\" activity?"
        }

        fun delete() {
            launchExDefault {
                try {
                    activity.delete()
                } catch (e: UIException) {
                    showUiAlert(e.uiMessage)
                }
            }
        }
    }

    data class State(
        val activities: List<ActivityModel>,
        val lastInterval: IntervalModel,
    ) {
        val newActivityText = "New Activity"
        val sortActivitiesText = "Sort"
        val settingsText = "Settings"

        val activitiesUI = activities.toUiList(lastInterval)
    }

    override val state = MutableStateFlow(
        State(
            activities = DI.activitiesSorted,
            lastInterval = DI.lastInterval,
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        ActivityModel.getAscSortedFlow()
            .onEachExIn(scope) { activities ->
                state.update { it.copy(activities = activities) }
            }
        IntervalModel.getLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scope) { interval ->
                state.update { it.copy(lastInterval = interval) }
            }
    }
}

private fun List<ActivityModel>.toUiList(
    lastInterval: IntervalModel
): List<TabTimerVM.ActivityUI> {
    val sorted = this.sortedWith(compareBy({ it.sort }, { it.id }))
    val activeIdx = sorted.indexOfFirst { it.id == lastInterval.activity_id }
    return sorted.mapIndexed { idx, activity ->
        val isActive = (idx == activeIdx)
        val noteUI = if (isActive && lastInterval.note != null)
            IntervalNoteUI(lastInterval.note, checkLeadingEmoji = true)
        else null
        TabTimerVM.ActivityUI(
            activity = activity,
            noteUI = noteUI,
            isActive = isActive,
            withTopDivider = (idx != 0) && !isActive && (activeIdx != idx - 1),
        )
    }
}
