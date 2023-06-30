package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.ActivityModel
import me.timeto.shared.db.IntervalModel
import me.timeto.shared.ui.TimerHintUI

class TabTimerVM : __VM<TabTimerVM.State>() {

    class ActivityUI(
        val activity: ActivityModel,
        val lastInterval: IntervalModel,
        val withTopDivider: Boolean,
    ) {

        val isActive = activity.id == lastInterval.activity_id

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

        val listText: String
        val triggers: List<TextFeatures.Trigger>

        val isPauseEnabled = isActive && !activity.isOther()

        init {
            val nameWithEmojiNoTriggers = activity.nameWithEmoji().textFeatures().textUi()
            deletionHint = nameWithEmojiNoTriggers
            deletionConfirmation = "Are you sure you want to delete \"$nameWithEmojiNoTriggers\" activity?"

            // listText/triggers
            val tfActivity = activity.name.textFeatures()
            val note = lastInterval.note
            if (isActive && note != null) {
                val tfNote = note.textFeatures()
                listText = tfNote.textNoFeatures
                triggers = (tfNote.triggers + tfActivity.triggers).distinctBy { it.id }
            } else {
                listText = tfActivity.textNoFeatures
                triggers = tfActivity.triggers
            }
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

        fun pauseLastInterval() {
            launchExDefault {
                IntervalModel.pauseLastInterval()
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
        TabTimerVM.ActivityUI(
            activity = activity,
            lastInterval = lastInterval,
            withTopDivider = (idx != 0) && !isActive && (activeIdx != idx - 1),
        )
    }
}
