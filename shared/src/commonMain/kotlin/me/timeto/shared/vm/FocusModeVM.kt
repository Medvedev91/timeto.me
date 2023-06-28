package me.timeto.shared.vm

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.timeto.shared.*
import me.timeto.shared.db.ChecklistItemModel
import me.timeto.shared.db.ChecklistModel
import me.timeto.shared.db.IntervalModel
import me.timeto.shared.db.TaskModel
import me.timeto.shared.vm.ui.ChecklistStateUI
import me.timeto.shared.vm.ui.TimerDataUI

class FocusModeVM : __VM<FocusModeVM.State>() {

    companion object {
        val menuColor = ColorRgba(255, 255, 255, 128)
    }

    data class State(
        val interval: IntervalModel,
        val allChecklistItems: List<ChecklistItemModel>,
        val isPurple: Boolean,
        val tasksToday: List<TaskModel>,
        val isTabTasksVisible: Boolean,
        val idToUpdate: Long,
    ) {

        val cancelTaskText: String? = if (interval.note != null) "return to tasks" else null
        val timerData = TimerDataUI(interval, !isPurple, ColorNative.white)

        val activity = interval.getActivityDI()

        // todo or use interval.getTriggers()
        val textFeatures = (interval.note ?: activity.name).textFeatures()
        val title = textFeatures.textUi(withActivityEmoji = false, withTimer = false)

        val checklistUI: ChecklistUI? = textFeatures.checklists.firstOrNull()?.let { checklist ->
            val items = allChecklistItems.filter { it.list_id == checklist.id }
            ChecklistUI(checklist, items)
        }

        val triggers = textFeatures.triggers.filter {
            val clt = (it as? TextFeatures.Trigger.Checklist) ?: return@filter true
            val clUI = checklistUI ?: return@filter true
            return@filter clt.checklist.id != clUI.checklist.id
        }

        val timeOfTheDay: String =
            UnixTime().getStringByComponents(UnixTime.StringComponent.hhmm24)

        val importantTasks: List<ImportantTask> = tasksToday
            .mapNotNull { task ->
                val taskTextFeatures = task.text.textFeatures()
                val timeData = taskTextFeatures.timeData ?: return@mapNotNull null
                if (!timeData.isImportant)
                    return@mapNotNull null
                ImportantTask(task, timeData, taskTextFeatures)
            }
            .sortedBy { it.timeData.unixTime.time }

        val tasksText = when (val size = tasksToday.size) {
            0 -> "No Tasks"
            else -> size.toStringEnding(true, "task", "tasks")
        }

        val batteryText = "${batteryLevelOrNull ?: "--"}"
        val batteryTextColor: ColorRgba
        val batteryBackground: ColorNative

        init {
            when {
                isBatteryChargingOrNull == true -> {
                    batteryTextColor = ColorRgba.white
                    batteryBackground = if (batteryLevelOrNull == 100) ColorNative.green else ColorNative.blue
                }
                batteryLevelOrNull in 0..20 -> {
                    batteryTextColor = ColorRgba.white
                    batteryBackground = ColorNative.red
                }
                else -> {
                    batteryTextColor = menuColor
                    batteryBackground = ColorNative.transparent
                }
            }
        }
    }

    override val state = MutableStateFlow(
        State(
            interval = DI.lastInterval,
            allChecklistItems = DI.checklistItems,
            isPurple = false,
            tasksToday = DI.tasks.filter { it.isToday },
            isTabTasksVisible = false,
            idToUpdate = 0,
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        IntervalModel.getLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scope) { interval ->
                state.update {
                    val isNewInterval = it.interval.id != interval.id
                    it.copy(
                        interval = interval,
                        isPurple = if (isNewInterval) false else it.isPurple,
                        isTabTasksVisible = if (isNewInterval) false else it.isTabTasksVisible,
                    )
                }
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
                state.update { it.copy(tasksToday = tasks) }
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

    fun toggleIsPurple() {
        state.update { it.copy(isPurple = !it.isPurple) }
    }

    fun toggleIsTabTasksVisible() {
        state.update { it.copy(isTabTasksVisible = it.isTabTasksVisible.not()) }
    }

    fun cancelTask() {
        launchExDefault {
            IntervalModel.cancelCurrentInterval()
        }
    }

    //////

    class ChecklistUI(
        val checklist: ChecklistModel,
        val items: List<ChecklistItemModel>,
    ) {

        val stateUI = ChecklistStateUI.build(checklist, items)
        val itemsUI = items.map { ItemUI(it) }

        val titleToExpand = "${checklist.name} ${items.count { it.isChecked }}/${items.size}"

        class ItemUI(
            val item: ChecklistItemModel,
        ) {
            fun toggle() {
                defaultScope().launchEx {
                    item.toggle()
                }
            }
        }
    }

    class ImportantTask(
        val task: TaskModel,
        val timeData: TextFeatures.TimeData,
        textFeatures: TextFeatures,
    ) {
        val type = timeData.type
        val text: String
        val borderColor: ColorRgba
        val backgroundColor: ColorRgba
        val timerContext = ActivityTimerSheetVM.TimerContext.Task(task)

        init {
            val dateText = timeData.unixTime.getStringByComponents(
                UnixTime.StringComponent.dayOfMonth,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.month3,
                UnixTime.StringComponent.comma,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.hhmm24,
            )
            text = "$dateText ${textFeatures.textNoFeatures} - ${timeData.timeLeftText()}"
            backgroundColor = when (timeData.status) {
                TextFeatures.TimeData.STATUS.IN -> ColorRgba.black
                TextFeatures.TimeData.STATUS.NEAR -> AppleColors.Palettes.blue.default
                TextFeatures.TimeData.STATUS.OVERDUE -> AppleColors.Palettes.red.default
            }
            borderColor = when (timeData.status) {
                TextFeatures.TimeData.STATUS.IN -> ColorRgba.white
                TextFeatures.TimeData.STATUS.NEAR,
                TextFeatures.TimeData.STATUS.OVERDUE -> backgroundColor
            }
        }
    }
}
