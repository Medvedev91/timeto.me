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

class MainVM : __VM<MainVM.State>() {

    companion object {
        val menuColor = ColorRgba(255, 255, 255, 128)
    }

    data class State(
        val interval: IntervalModel,
        val allChecklistItems: List<ChecklistItemModel>,
        val isPurple: Boolean,
        val tasksToday: List<TaskModel>,
        val isTasksVisible: Boolean,
        val idToUpdate: Long,
    ) {

        val timerData = TimerDataUI(interval, isPurple, ColorRgba.white)
        val timerButtonExpandSheetContext = ActivityTimerSheetVM.TimerContext.Interval(interval)

        val activity = interval.getActivityDI()

        // todo or use interval.getTriggers()
        val textFeatures = (interval.note ?: activity.name).textFeatures()
        val title = textFeatures.textUi(
            withActivityEmoji = false,
            withTimer = false,
        )

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

                if (taskTextFeatures.paused != null)
                    return@mapNotNull ImportantTask(task, taskTextFeatures)

                if (taskTextFeatures.timeData?.isImportant == true)
                    return@mapNotNull ImportantTask(task, taskTextFeatures)

                null
            }
            .sortedBy {
                val timeData = it.textFeatures.timeData
                if (timeData != null)
                    timeData.unixTime.time
                else
                    Int.MAX_VALUE
            }

        val tasksText = when (val size = tasksToday.size) {
            0 -> "No Tasks"
            else -> size.toStringEnding(true, "task", "tasks")
        }

        val batteryText = "${batteryLevelOrNull ?: "--"}"
        val batteryTextColor: ColorRgba
        val batteryBackground: ColorRgba

        init {
            when {
                isBatteryChargingOrNull == true -> {
                    batteryTextColor = ColorRgba.white
                    batteryBackground = if (batteryLevelOrNull == 100) ColorRgba.green else ColorRgba.blue
                }
                batteryLevelOrNull in 0..20 -> {
                    batteryTextColor = ColorRgba.white
                    batteryBackground = ColorRgba.red
                }
                else -> {
                    batteryTextColor = menuColor
                    batteryBackground = ColorRgba.transparent
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
            isTasksVisible = false,
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
                        isTasksVisible = if (isNewInterval) false else it.isTasksVisible,
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

    fun toggleIsPurple() {
        state.update { it.copy(isPurple = !it.isPurple) }
    }

    fun toggleIsTasksVisible() {
        state.update { it.copy(isTasksVisible = !it.isTasksVisible) }
    }

    fun pauseTask() {
        launchExDefault {
            IntervalModel.pauseLastInterval()
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
        val textFeatures: TextFeatures,
    ) {

        val type: Type?
        val text: String
        val borderColor: ColorRgba
        val backgroundColor: ColorRgba
        val timerContext = ActivityTimerSheetVM.TimerContext.Task(task)

        init {

            val timeData = textFeatures.timeData

            type = if (timeData != null)
                when (timeData.type) {
                    TextFeatures.TimeData.TYPE.EVENT -> Type.event
                    TextFeatures.TimeData.TYPE.REPEATING -> Type.repeating
                }
            else if (textFeatures.paused != null)
                Type.paused
            else {
                reportApi("ImportantTask invalid type")
                null
            }

            text = if (timeData != null) {
                val dateText = timeData.unixTime.getStringByComponents(
                    UnixTime.StringComponent.dayOfMonth,
                    UnixTime.StringComponent.space,
                    UnixTime.StringComponent.month3,
                    UnixTime.StringComponent.comma,
                    UnixTime.StringComponent.space,
                    UnixTime.StringComponent.hhmm24,
                )
                "$dateText ${textFeatures.textNoFeatures} - ${timeData.timeLeftText()}"
            } else
                textFeatures.textNoFeatures

            backgroundColor = if (timeData != null)
                when (timeData.status) {
                    TextFeatures.TimeData.STATUS.IN -> ColorRgba.black
                    TextFeatures.TimeData.STATUS.NEAR -> AppleColors.Palettes.blue.dark
                    TextFeatures.TimeData.STATUS.OVERDUE -> AppleColors.Palettes.red.dark
                }
            else
                ColorRgba.black

            borderColor = if (timeData != null)
                when (timeData.status) {
                    TextFeatures.TimeData.STATUS.IN -> ColorRgba.white
                    TextFeatures.TimeData.STATUS.NEAR,
                    TextFeatures.TimeData.STATUS.OVERDUE -> backgroundColor
                }
            else
                ColorRgba.white
        }

        enum class Type {
            event, repeating, paused
        }
    }
}
