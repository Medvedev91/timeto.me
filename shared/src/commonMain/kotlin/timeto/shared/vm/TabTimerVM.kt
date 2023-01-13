package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.*
import timeto.shared.db.ActivityModel
import timeto.shared.db.IntervalModel
import timeto.shared.vm.ui.TimerHintUI

class TabTimerVM : __VM<TabTimerVM.State>() {

    inner class ActivityUI(
        val activity: ActivityModel,
    ) {
        val timerHints = TimerHintUI.buildList(
            activity,
            isShort = true,
            historyLimit = 2,
            customLimit = 5
        ) { seconds ->
            val last = DI.lastInterval
            val note  = if (last.activity_id == activity.id) last.note else null
            IntervalModel.addWithValidation(seconds, activity, note)
        }

        val deletionHint: String
        val deletionConfirmation: String

        val listText: String
        val triggers: List<Trigger>

        init {
            val textFeatures = TextFeatures.parse(activity.name)
            listText = textFeatures.textNoFeatures
            triggers = textFeatures.triggers

            val nameWithEmojiNoTriggers = TextFeatures.parse(activity.nameWithEmoji()).uiText()
            deletionHint = nameWithEmojiNoTriggers
            deletionConfirmation = "Are you sure you want to delete \"$nameWithEmojiNoTriggers\" activity?"
        }

        fun delete() {
            scopeVM().launchEx {
                try {
                    activity.delete()
                } catch (e: UIException) {
                    showUiAlert(e.uiMessage)
                }
            }
        }
    }

    data class State(
        val activitiesUI: List<ActivityUI>,
    )

    override val state: MutableStateFlow<State>

    init {
        state = MutableStateFlow(
            State(
                activitiesUI = DI.activitiesSorted.toUiList()
            )
        )
    }

    override fun onAppear() {
        ActivityModel.getAscSortedFlow()
            .onEachExIn(scopeVM()) { list ->
                state.update { it.copy(activitiesUI = list.toUiList()) }
            }
    }

    private fun List<ActivityModel>.toUiList() = this
        .sortedWith(compareBy({ it.sort }, { it.id }))
        .map { ActivityUI(it) }
}
