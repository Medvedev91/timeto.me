package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.*
import timeto.shared.db.ActivityModel
import timeto.shared.db.IntervalModel
import timeto.shared.db.TaskModel

class ActivityTimerSheetVM(
    val activity: ActivityModel,
    private val timerContext: TimerContext?,
) : __VM<ActivityTimerSheetVM.State>() {

    data class State(
        val title: String,
        val note: String?,
        val formTimeItemIdx: Int,
        val timeItems: List<TimerPickerItem>,
        /// Like inner data class
        private val activity: ActivityModel,
    )

    override val state: MutableStateFlow<State>

    init {
        val note = when (timerContext) {
            is TimerContext.Task -> timerContext.task.text
            else -> null
        }

        val defSeconds = TimerPickerItem.calcDefSeconds(activity, note)
        val timeItems = TimerPickerItem.buildList(defSeconds, stepMinutes = 10)

        state = MutableStateFlow(
            State(
                title = TextFeatures.parse(activity.nameWithEmoji()).uiText(),
                note = note,
                formTimeItemIdx = timeItems.indexOfFirst { it.seconds == defSeconds },
                timeItems = timeItems,
                activity = activity,
            )
        )
    }

    fun start(
        onSuccess: () -> Unit,
    ) = scopeVM().launchEx {
        try {
            val deadline = state.value.timeItems[state.value.formTimeItemIdx].seconds

            when (timerContext) {
                is TimerContext.Task -> {
                    timerContext.task.startInterval(deadline, activity)
                }
                else -> {
                    val lastInterval = IntervalModel.getLastOneOrNull()!!
                    val note = if (lastInterval.activity_id == activity.id) lastInterval.note else null
                    IntervalModel.addWithValidation(deadline, activity, note)
                }
            }

            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }

    fun setFormTimeItemIdx(newIdx: Int) {
        state.update { it.copy(formTimeItemIdx = newIdx) }
    }

    ///

    sealed class TimerContext {
        class Task(val task: TaskModel) : TimerContext()
        // todo simple note
    }
}
