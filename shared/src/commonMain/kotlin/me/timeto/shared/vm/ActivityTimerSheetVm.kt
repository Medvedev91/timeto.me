package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.db.TaskDb

class ActivityTimerSheetVm(
    val activity: ActivityDb,
    private val timerContext: TimerContext?,
) : __Vm<ActivityTimerSheetVm.State>() {

    data class State(
        val title: String,
        val note: String?,
        val formTimeItemIdx: Int,
        val timeItems: List<TimerPickerItem>,
        // Like inner data class
        private val activity: ActivityDb,
        private val timerContext: TimerContext?,
    ) {

        val timerHints = activity.data.timer_hints.getTimerHintsUI(
            historyLimit = 6,
            customLimit = 6,
            onSelect = { hintUI ->
                startIntervalByContext(timerContext, activity, hintUI.seconds)
            }
        )
    }

    override val state: MutableStateFlow<State>

    init {
        val note = when (timerContext) {
            is TimerContext.Task -> timerContext.task.text
            is TimerContext.Interval -> timerContext.interval.note
            null -> null
        }

        val defSeconds = TimerPickerItem.calcDefSeconds(activity, note)
        val timeItems = TimerPickerItem.buildList(defSeconds)

        state = MutableStateFlow(
            State(
                title = activity.nameWithEmoji().textFeatures().textUi(),
                note = note,
                formTimeItemIdx = timeItems.indexOfFirst { it.seconds == defSeconds },
                timeItems = timeItems,
                activity = activity,
                timerContext = timerContext,
            )
        )
    }

    fun start(
        onSuccess: () -> Unit,
    ) = scopeVm().launchEx {
        try {
            val timer = state.value.timeItems[state.value.formTimeItemIdx].seconds
            startIntervalByContext(timerContext, activity, timer)
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
        class Task(val task: TaskDb) : TimerContext()
        class Interval(val interval: IntervalDb) : TimerContext()
    }

    companion object {

        suspend fun startIntervalByContext(
            timerContext: TimerContext?,
            activity: ActivityDb,
            timer: Int,
        ) {
            when (timerContext) {
                is TimerContext.Task ->
                    timerContext.task.startInterval(timer, activity)
                is TimerContext.Interval ->
                    IntervalDb.addWithValidation(timer, activity, timerContext.interval.note)
                null ->
                    activity.startInterval(timer)
            }
        }
    }
}
