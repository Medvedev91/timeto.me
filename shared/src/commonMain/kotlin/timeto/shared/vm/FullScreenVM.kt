package timeto.shared.vm

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timeto.shared.*
import timeto.shared.db.ChecklistItemModel
import timeto.shared.db.IntervalModel
import timeto.shared.vm.ui.TimerDataUI
import timeto.shared.vm.ui.toChecklistUI

class FullScreenVM : __VM<FullScreenVM.State>() {

    data class State(
        val interval: IntervalModel,
        val allChecklistItems: List<ChecklistItemModel>,
        val idToUpdate: Long,
    ) {
        val timerData = TimerDataUI(interval, ColorNative.white)

        val activity = interval.getActivityDI()
        val textFeatures = (interval.note ?: activity.name).textFeatures()
        val title = textFeatures.textUi(withActivityEmoji = false, withTimer = false)

        val checklistUI = textFeatures.checklists.firstOrNull()?.let { checklist ->
            val items = allChecklistItems.filter { it.list_id == checklist.id }
            checklist.toChecklistUI(items)
        }

        val triggers = textFeatures.triggers.filter {
            val clt = (it as? TextFeatures.Trigger.Checklist) ?: return@filter true
            val clUI = checklistUI ?: return@filter true
            return@filter clt.checklist.id != clUI.checklist.id
        }

        val timeOfTheDay: String =
            UnixTime().getStringByComponents(listOf(UnixTime.StringComponent.hhmm24))

        val battery = "${batteryPrc ?: "--"}"
        val batteryColor: ColorNative = when (batteryPrc) {
            null -> ColorNative.white
            100 -> ColorNative.green
            in 0..20 -> ColorNative.red
            else -> ColorNative.white
        }
    }

    override val state = MutableStateFlow(
        State(
            interval = DI.lastInterval,
            allChecklistItems = DI.checklistItems,
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
                        idToUpdate = it.idToUpdate + 1, // Force update
                    )
                }
                delay(1_000L)
            }
        }
        if (batteryPrc == null)
            reportApi("batteryPrc null")
    }

    fun restart() {
        launchExDefault {
            IntervalModel.restartActualInterval()
        }
    }
}
