package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.*
import timeto.shared.db.ActivityModel
import timeto.shared.db.IntervalModel
import timeto.shared.ui.IntervalNoteUI
import timeto.shared.ui.TimerHintUI

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
            val last = DI.lastInterval
            val note = if (last.activity_id == activity.id) last.note else null
            IntervalModel.addWithValidation(seconds, activity, note)
        }

        val deletionHint: String
        val deletionConfirmation: String

        val listText: String
        val triggers: List<Trigger>

        init {
            val textFeatures = TextFeatures.parse(activity.name)
            listText = textFeatures.textUI()
            triggers = textFeatures.triggers

            val nameWithEmojiNoTriggers = TextFeatures.parse(activity.nameWithEmoji()).textUI()
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
    val activeIdx = this.indexOfFirst { it.id == lastInterval.activity_id }
    return sorted.mapIndexed { idx, activity ->
        val isActive = (idx == activeIdx)
        val noteUI = if (isActive && lastInterval.note != null)
            IntervalNoteUI(lastInterval.note)
        else null
        TabTimerVM.ActivityUI(
            activity = activity,
            noteUI = noteUI,
            isActive = isActive,
            withTopDivider = (idx != 0) && (activeIdx != idx - 1),
        )
    }
}
