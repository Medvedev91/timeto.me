package timeto.shared.vm

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timeto.shared.*
import timeto.shared.db.ChecklistItemModel
import timeto.shared.db.IntervalModel
import timeto.shared.vm.ui.TimerDataUI
import timeto.shared.vm.ui.toChecklistUI

class FullscreenVM(
    defColor: ColorNative,
) : __VM<FullscreenVM.State>() {

    data class State(
        val interval: IntervalModel,
        val allChecklistItems: List<ChecklistItemModel>,
        val defColor: ColorNative,
        val idToUpdate: Long
    ) {
        val timerData = TimerDataUI(interval, defColor)

        val activity = interval.getActivityDI()
        val textFeatures = (interval.note ?: activity.name).textFeatures()
        val title = textFeatures.textUi(withActivityEmoji = false, withTimer = false)

        val checklistUI = textFeatures.checklists.firstOrNull()?.let { checklist ->
            val items = allChecklistItems.filter { it.list_id == checklist.id }
            checklist.toChecklistUI(items)
        }
    }

    override val state = MutableStateFlow(
        State(
            interval = DI.lastInterval,
            allChecklistItems = DI.checklistItems,
            defColor = defColor,
            idToUpdate = 0,
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        IntervalModel.getLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scope) { interval ->
                state.update { it.copy(interval = interval) }
            }
        ChecklistItemModel
            .getAscFlow()
            .onEachExIn(scope) { items ->
                state.update { it.copy(allChecklistItems = items) }
            }
        scope.launch {
            while (true) {
                state.update {
                    it.copy(
                        interval = DI.lastInterval,
                        idToUpdate = timeMls(), // Force update
                    )
                }
                delay(1_000L)
            }
        }
    }

    fun restart() {
        launchExDefault {
            IntervalModel.restartActualInterval()
        }
    }
}
