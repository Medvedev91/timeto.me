package timeto.shared.vm

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timeto.shared.*
import timeto.shared.db.ChecklistItemModel
import timeto.shared.db.IntervalModel
import timeto.shared.db.TaskModel
import timeto.shared.vm.ui.TimerDataUI
import timeto.shared.vm.ui.sortedByFolder
import timeto.shared.vm.ui.toChecklistUI

class FullScreenVM : __VM<FullScreenVM.State>() {

    data class State(
        val interval: IntervalModel,
        val allChecklistItems: List<ChecklistItemModel>,
        val isTaskCancelVisible: Boolean,
        val isCountdown: Boolean,
        val allTasksUI: List<TaskUI>,
        val idToUpdate: Long,
    ) {
        val timerData = TimerDataUI(interval, isCountdown, ColorNative.white)

        val activityTimerContext = if (interval.note != null)
            ActivityTimerSheetVM.TimerContext.Note(interval.note)
        else null

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
            UnixTime().getStringByComponents(UnixTime.StringComponent.hhmm24)

        val battery = "${batteryLevelOrNull ?: "--"}"
        val batteryBackground: ColorNative? = when {
            isBatteryChargingOrNull == true -> {
                if (batteryLevelOrNull == 100) ColorNative.green else ColorNative.blue
            }
            batteryLevelOrNull in 0..20 -> ColorNative.red
            else -> null
        }

        val visibleTasksUI: List<TaskUI> = run {
            val importantTasksUI = allTasksUI.filterIsInstance<TaskUI.ImportantTaskUI>()
            val firstTaskUI = allTasksUI.firstOrNull() ?: return@run importantTasksUI
            if (importantTasksUI.any { it.task.id == firstTaskUI.task.id })
                return@run importantTasksUI
            val list = mutableListOf(firstTaskUI)
            list.addAll(importantTasksUI)
            return@run list
        }
    }

    override val state = MutableStateFlow(
        State(
            interval = DI.lastInterval,
            allChecklistItems = DI.checklistItems,
            isTaskCancelVisible = false,
            isCountdown = true,
            allTasksUI = listOf(), // todo
            idToUpdate = 0,
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        IntervalModel.getLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scope) { interval ->
                val isCountdown = if (interval.id == state.value.interval.id)
                    state.value.isCountdown else true
                state.update { it.copy(interval = interval, isCountdown = isCountdown) }
            }
        ChecklistItemModel
            .getAscFlow()
            .onEachExIn(scope) { items ->
                state.update { it.copy(allChecklistItems = items) }
            }
        TaskModel
            .getAscFlow()
            .map { it.filter { task -> task.isToday } }
            .onEachExIn(scope) { tasks ->
                state.update {
                    it.copy(allTasksUI = tasks
                        .sortedByFolder(DI.getTodayFolder())
                        .map { task -> TaskUI.prepTask(task) }
                    )
                }
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
        if (batteryLevelOrNull == null)
            reportApi("batteryLevelOrNull null")
    }

    fun restart() {
        launchExDefault {
            IntervalModel.restartActualInterval()
        }
    }

    fun toggleIsCountdown() {
        state.update { it.copy(isCountdown = it.isCountdown.not()) }
    }

    ///
    /// Cancel

    fun toggleIsTaskCancelVisible() {
        state.update { it.copy(isTaskCancelVisible = !it.isTaskCancelVisible) }
    }

    fun cancelTask() {
        launchExDefault {
            IntervalModel.cancelCurrentInterval()
            state.update { it.copy(isTaskCancelVisible = false) }
        }
    }

    //////

    sealed class TaskUI(
        val task: TaskModel,
    ) {

        companion object {

            fun prepTask(task: TaskModel): TaskUI {
                val textFeatures = task.text.textFeatures()
                val timeData = textFeatures.timeData
                val grayRgba = ColorRgba(150, 150, 150, 255) // todo

                if (timeData == null) {
                    return RegularTaskUI(
                        task = task,
                        text = textFeatures.textNoFeatures,
                        textColor = grayRgba,
                    )
                }

                val timeLeftText = timeData.timeLeftText()

                if (timeData.isImportant) {
                    val dateText = timeData.unixTime.getStringByComponents(
                        UnixTime.StringComponent.dayOfMonth,
                        UnixTime.StringComponent.space,
                        UnixTime.StringComponent.month3,
                        UnixTime.StringComponent.comma,
                        UnixTime.StringComponent.space,
                        UnixTime.StringComponent.hhmm24,
                    )
                    val backgroundColor = when (timeData.status) {
                        TextFeatures.TimeData.STATUS.IN, // todo?
                        TextFeatures.TimeData.STATUS.NEAR -> ColorRgba(0, 122, 255, 255) // todo
                        TextFeatures.TimeData.STATUS.OVERDUE -> ColorRgba(255, 59, 48) // todo
                    }
                    return ImportantTaskUI(
                        task = task,
                        type = timeData.type,
                        text = "$dateText ${textFeatures.textNoFeatures} - $timeLeftText",
                        backgroundColor = backgroundColor,
                    )
                }
                val textColor = when (timeData.status) {
                    TextFeatures.TimeData.STATUS.IN -> grayRgba
                    TextFeatures.TimeData.STATUS.NEAR -> ColorRgba(0, 122, 255, 180) // todo
                    TextFeatures.TimeData.STATUS.OVERDUE -> ColorRgba(255, 59, 48, 160) // todo
                }
                return RegularTaskUI(
                    task = task,
                    text = "${textFeatures.textNoFeatures} - $timeLeftText",
                    textColor = textColor,
                )
            }
        }

        class RegularTaskUI(
            task: TaskModel,
            val text: String,
            val textColor: ColorRgba,
        ) : TaskUI(task)

        class ImportantTaskUI(
            task: TaskModel,
            val type: TextFeatures.TimeData.TYPE,
            val text: String,
            val backgroundColor: ColorRgba,
        ) : TaskUI(task)
    }
}
