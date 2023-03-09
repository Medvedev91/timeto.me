package timeto.shared.vm

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timeto.shared.*
import timeto.shared.db.IntervalModel
import timeto.shared.vm.ui.ChecklistUI
import timeto.shared.vm.ui.toChecklistUI

class FullscreenVM(
    private val defColor: ColorNative,
) : __VM<FullscreenVM.State>() {

    data class State(
        val title: String,
        val triggers: List<Trigger>,
        val timerData: TimerData,
    ) {

        val checklistUI: ChecklistUI?

        init {
            val checklist = triggers.filterIsInstance<Trigger.Checklist>().firstOrNull()?.checklist
            checklistUI = if (checklist == null) null else {
                val checklistItems = DI.checklistItems.filter { it.list_id == checklist.id }
                checklist.toChecklistUI(checklistItems)
            }
        }
    }

    override val state = MutableStateFlow(prepState(DI.lastInterval, defColor))

    override fun onAppear() {
        val scope = scopeVM()
        IntervalModel.getLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scope) {
                upState(it)
            }
        scope.launch {
            while (true) {
                upState(DI.lastInterval)
                delay(1_000L)
            }
        }
    }

    fun restart() {
        launchExDefault {
            IntervalModel.restartActualInterval()
        }
    }

    private fun upState(interval: IntervalModel) {
        state.update { prepState(interval, defColor) }
    }
}

private fun prepState(
    lastInterval: IntervalModel,
    defColor: ColorNative,
): FullscreenVM.State {
    val titlePlain = lastInterval.note ?: lastInterval.getActivityDI().nameWithEmoji()
    val textFeatures = TextFeatures.parse(titlePlain)
    return FullscreenVM.State(
        title = textFeatures.textUI(),
        triggers = textFeatures.triggers,
        timerData = TimerData(lastInterval, defColor),
    )
}
