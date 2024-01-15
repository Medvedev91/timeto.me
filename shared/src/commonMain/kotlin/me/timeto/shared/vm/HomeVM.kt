package me.timeto.shared.vm

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.timeto.shared.*
import me.timeto.shared.db.*
import me.timeto.shared.vm.ui.ChecklistStateUI
import me.timeto.shared.vm.ui.DayIntervalsUI
import me.timeto.shared.vm.ui.TimerDataUI

class HomeVM : __VM<HomeVM.State>() {

    data class State(
        val interval: IntervalModel,
        val allChecklistItems: List<ChecklistItemModel>,
        val isPurple: Boolean,
        val tasksToday: List<TaskModel>,
        val isTasksVisible: Boolean,
        val todayIntervalsUI: DayIntervalsUI?,
        val idToUpdate: Long,
    ) {

        val timerData = TimerDataUI(interval, isPurple, ColorRgba.white)
        val timerButtonExpandSheetContext = ActivityTimerSheetVM.TimerContext.Interval(interval)

        val timerButtonsColor = if (timerData.status.isProcess() && !isPurple)
            ColorRgba(255, 255, 255, 180) else timerData.color

        val activity = interval.getActivityDI()

        val timerHints = activity.data.timer_hints.getTimerHintsUI(
            historyLimit = 6,
            customLimit = 6,
            onSelect = { hintUI ->
                val context = ActivityTimerSheetVM.TimerContext.Interval(interval)
                ActivityTimerSheetVM.startIntervalByContext(context, activity, hintUI.seconds)
            }
        )

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

        val goalsUI: List<GoalUI> = if (todayIntervalsUI == null)
            listOf()
        else DI.activitiesSorted
            .map { activity ->
                val activityName = activity.name.textFeatures().textNoFeatures
                activity.goals
                    .filter { it.period.isToday() }
                    .map { goal ->
                        var totalSeconds: Int = todayIntervalsUI.intervalsUI
                            .sumOf { if (it.activity?.id == activity.id) it.seconds else 0 }
                        val lastWithActivity = todayIntervalsUI.intervalsUI
                            .lastOrNull { it.activity != null }
                        if (lastWithActivity?.activity?.id == activity.id) {
                            val timeFinish = lastWithActivity.timeFinish()
                            val now = time()
                            if (now < timeFinish)
                                reportApi("MainActivity goal bad time $now $timeFinish")
                            totalSeconds += (now - timeFinish)
                        }

                        val timeDone = totalSeconds.limitMax(goal.seconds)
                        val timeLeft = goal.seconds - timeDone
                        val textRight = if (timeLeft > 0) timeLeft.toTimerHintNote(isShort = false) else "👍"
                        GoalUI(
                            textLeft = activityName + " " + goal.seconds.toTimerHintNote(isShort = false),
                            textRight = textRight,
                            ratio = timeDone.toFloat() / goal.seconds.toFloat(),
                            bgColor = activity.colorRgba,
                        )
                    }
            }
            .flatten()

        val menuNote: String = when (val count = DI.tasks.count { it.isToday }) {
            0 -> "No Tasks"
            1 -> "1 task"
            else -> "$count tasks"
        }

        val menuTime: String = UnixTime().getStringByComponents(UnixTime.StringComponent.hhmm24)

        val mainTasks: List<MainTask> = tasksToday
            .mapNotNull { task ->
                val taskTextFeatures = task.text.textFeatures()

                if (taskTextFeatures.paused != null)
                    return@mapNotNull MainTask(task, taskTextFeatures)

                if (taskTextFeatures.timeData?.type?.isEvent() == true)
                    return@mapNotNull MainTask(task, taskTextFeatures)

                if (taskTextFeatures.isImportant)
                    return@mapNotNull MainTask(task, taskTextFeatures)

                null
            }
            .sortedBy {
                val timeData = it.textFeatures.timeData
                if (timeData != null)
                    timeData.unixTime.time
                else
                    Int.MAX_VALUE
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
                    batteryTextColor = ColorRgba.homeFontSecondary
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
            todayIntervalsUI = null, // todo init data
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

        ////

        IntervalModel.anyChangeFlow()
            .onEachExIn(scope) {
                upTodayIntervalsUI()
            }
        scope.launch {
            while (true) {
                delayToNextMinute()
                upTodayIntervalsUI()
            }
        }

        ////

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

    private suspend fun upTodayIntervalsUI() {
        val utcOffset = localUtcOffsetWithDayStart
        val todayDS = UnixTime(utcOffset = utcOffset).localDay
        val todayIntervalsUI = DayIntervalsUI
            .buildList(
                dayStart = todayDS,
                dayFinish = todayDS,
                utcOffset = utcOffset,
            )
            .first()
        state.update { it.copy(todayIntervalsUI = todayIntervalsUI) }
    }

    //////

    class GoalUI(
        val textLeft: String,
        val textRight: String,
        val ratio: Float,
        val bgColor: ColorRgba,
    )

    class ChecklistUI(
        val checklist: ChecklistModel,
        val items: List<ChecklistItemModel>,
    ) {

        val stateUI = ChecklistStateUI.build(checklist, items)
        val itemsUI = items.map { ItemUI(it) }

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

    class MainTask(
        val task: TaskModel,
        val textFeatures: TextFeatures,
    ) {

        val text = textFeatures.textUi()
        val timerContext = ActivityTimerSheetVM.TimerContext.Task(task)
        val timeUI: TimeUI? = textFeatures.timeData?.let { timeData ->
            val bgColor = when (timeData.status) {
                TextFeatures.TimeData.STATUS.IN -> ColorRgba.homeFg
                TextFeatures.TimeData.STATUS.SOON -> ColorRgba.blue
                TextFeatures.TimeData.STATUS.OVERDUE -> ColorRgba.red
            }

            val noteColor = when (timeData.status) {
                TextFeatures.TimeData.STATUS.IN -> ColorRgba.textSecondary
                TextFeatures.TimeData.STATUS.SOON -> ColorRgba.blue
                TextFeatures.TimeData.STATUS.OVERDUE -> ColorRgba.red
            }

            TimeUI(
                text = timeData.timeText(),
                textBgColor = bgColor,
                note = timeData.timeLeftText(),
                noteColor = noteColor,
            )
        }

        class TimeUI(
            val text: String,
            val textBgColor: ColorRgba,
            val note: String,
            val noteColor: ColorRgba,
        )
    }
}
