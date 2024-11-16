package me.timeto.shared.vm

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.timeto.shared.*
import me.timeto.shared.db.*
import me.timeto.shared.models.*

class HomeVm : __Vm<HomeVm.State>() {

    data class State(
        val interval: IntervalDb,
        val isPurple: Boolean,
        val todayTasksUi: List<TaskUi>,
        val isTasksVisible: Boolean,
        val todayIntervalsUi: DayIntervalsUi?,
        val fdroidMessage: String?,
        val readmeMessage: String?,
        val whatsNewMessage: String?,
        val listsContainerSize: ListsContainerSize?,
        val idToUpdate: Long,
    ) {

        val timerData = TimerDataUi(interval, todayTasksUi.map { it.taskDb }, isPurple)

        val activeActivityDb: ActivityDb = interval.getActivityDbCached()

        // todo or use interval.getTriggers()
        val textFeatures = (interval.note ?: activeActivityDb.name).textFeatures()

        val checklistDb: ChecklistDb? = textFeatures.checklists.firstOrNull()

        val triggers = textFeatures.triggers.filter {
            val clt = (it as? TextFeatures.Trigger.Checklist) ?: return@filter true
            val clDb = checklistDb ?: return@filter true
            return@filter clt.checklist.id != clDb.id
        }

        // todo performance?
        val goalBarsUi: List<GoalBarUi> = if (todayIntervalsUi == null)
            listOf()
        else Cache.activitiesDbSorted
            .map { activityDb ->
                val activityName = activityDb.name.textFeatures().textNoFeatures
                activityDb.getGoalsDbCached()
                    .filter { it.buildPeriod().isToday() }
                    .map { goalDb ->
                        var totalSeconds: Int = todayIntervalsUi.intervalsUI
                            .sumOf { if (it.activity?.id == activityDb.id) it.seconds else 0 }
                        val lastWithActivity = todayIntervalsUi.intervalsUI
                            .lastOrNull { it.activity != null }
                        if (lastWithActivity?.activity?.id == activityDb.id) {
                            val timeFinish = lastWithActivity.timeFinish()
                            val now = time()
                            if (now < timeFinish)
                                reportApi("MainActivity goal bad time $now $timeFinish")
                            totalSeconds += (now - timeFinish)
                        }

                        val timeDone: Int = totalSeconds.limitMax(goalDb.seconds)
                        val timeLeft: Int = goalDb.seconds - timeDone
                        val textRight: String =
                            if (timeLeft > 0) timeLeft.toTimerHintNote(isShort = false) else goalDb.finish_text

                        val goalTf: TextFeatures = goalDb.note.textFeatures()

                        GoalBarUi(
                            goalDb = goalDb,
                            goalTf = goalTf,
                            activityDb = activityDb,
                            textLeft = prepGoalTextLeft(
                                note = goalTf.textNoFeatures.takeIf { it.isNotBlank() } ?: activityName,
                                secondsLeft = totalSeconds,
                            ),
                            textRight = textRight,
                            ratio = timeDone.toFloat() / goalDb.seconds.toFloat(),
                        )
                    }
            }
            .flatten()

        val menuTime: String = UnixTime().getStringByComponents(UnixTime.StringComponent.hhmm24)
        val menuTasksNote = "${Cache.tasksDb.count { it.isToday }}"

        val mainTasks: List<MainTask> = run {
            val tasksUi: List<TaskUi> =
                if (KvDb.todayOnHomeScreenCached())
                    todayTasksUi
                else
                    todayTasksUi.filter { taskUi ->
                        val taskTf = taskUi.tf
                        // Condition
                        (taskTf.paused != null) ||
                        taskTf.isImportant ||
                        (taskTf.calcTimeData()?.type?.isEvent() == true)
                    }
            tasksUi.sortedUi(true).map { MainTask(it) }
        }

        val listsSizes: ListsSizes = run {
            val lc = listsContainerSize ?: return@run ListsSizes(0f, 0f)
            //
            // No one
            if (checklistDb == null && mainTasks.isEmpty())
                return@run ListsSizes(0f, 0f)
            //
            // Only one
            if (checklistDb != null && mainTasks.isEmpty())
                return@run ListsSizes(checklist = lc.totalHeight, mainTasks = 0f)
            if (checklistDb == null && mainTasks.isNotEmpty())
                return@run ListsSizes(checklist = 0f, mainTasks = lc.totalHeight)
            //
            // Both
            checklistDb!!
            val halfHeight: Float = lc.totalHeight / 2
            val tasksCount: Int = mainTasks.size
            val tasksFullHeight: Float = tasksCount * lc.itemHeight
            // Tasks smaller the half
            if (tasksFullHeight < halfHeight)
                return@run ListsSizes(
                    checklist = lc.totalHeight - tasksFullHeight,
                    mainTasks = tasksFullHeight,
                )
            // Tasks bigger the half
            val checklistCount: Int = checklistDb.getItemsCached().size.limitMin(2)
            val checklistFullHeight: Float = checklistCount * lc.itemHeight
            if (checklistFullHeight < halfHeight)
                return@run ListsSizes(
                    checklist = checklistFullHeight,
                    mainTasks = lc.totalHeight - checklistFullHeight,
                )
            ListsSizes(checklist = halfHeight, mainTasks = halfHeight)
        }

        val batteryUi: BatteryUi = run {
            val level: Int? = batteryLevelOrNull
            val text = "${level ?: "--"}"
            when {
                isBatteryChargingOrNull == true ->
                    BatteryUi(text, if (level == 100) ColorRgba.green else ColorRgba.blue, true)

                batteryLevelOrNull in 0..20 ->
                    BatteryUi(text, ColorRgba.red, true)

                else -> BatteryUi(text, ColorRgba.homeFontSecondary, false)
            }
        }
    }

    override val state = MutableStateFlow(
        State(
            interval = Cache.lastInterval,
            isPurple = false,
            todayTasksUi = listOf(),
            isTasksVisible = false,
            todayIntervalsUi = null, // todo init data
            fdroidMessage = null, // todo init data
            readmeMessage = null, // todo init data
            whatsNewMessage = null, // todo init data
            listsContainerSize = null,
            idToUpdate = 0,
        )
    )

    override fun onAppear() {
        val scope = scopeVm()
        IntervalDb.getLastOneOrNullFlow()
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
        TaskDb
            .getAscFlow()
            .map { it.filter { task -> task.isToday } }
            .onEachExIn(scope) { tasks ->
                state.update { it.copy(todayTasksUi = tasks.map { it.toUi() }) }
            }
        if (deviceData.isFdroid)
            KvDb.KEY.IS_SENDING_REPORTS
                .getOrNullFlow()
                .onEachExIn(scope) { kvDb ->
                    state.update {
                        it.copy(fdroidMessage = if (kvDb == null) "Message for F-Droid users" else null)
                    }
                }
        KvDb.KEY.HOME_README_OPEN_TIME
            .getOrNullFlow()
            .onEachExIn(scope) { kvDb ->
                state.update {
                    it.copy(readmeMessage = if (kvDb == null) "How to use the app" else null)
                }
            }
        KvDb.KEY.WHATS_NEW_CHECK_UNIX_DAY
            .getOrNullFlow()
            .onEachExIn(scope) { kvDb ->
                val lastHistoryUnixDay = WhatsNewVm.prepHistoryItemsUi().first().unixDay
                val message: String? =
                    if ((kvDb == null) || (lastHistoryUnixDay > kvDb.value.toInt()))
                        "What's New"
                    else null
                state.update {
                    it.copy(whatsNewMessage = message)
                }
            }

        ////

        IntervalDb.anyChangeFlow()
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
                        interval = Cache.lastInterval,
                        idToUpdate = it.idToUpdate + 1, // Force update
                    )
                }
                delay(1_000L)
            }
        }

        if (batteryLevelOrNull == null) {
            scope.launch {
                delay(2_000)
                if (batteryLevelOrNull == null)
                    reportApi("batteryLevelOrNull null")
            }
        }
    }

    fun upListsContainerSize(
        totalHeight: Float,
        itemHeight: Float,
    ) {
        val lc = ListsContainerSize(totalHeight, itemHeight)
        if (lc == state.value.listsContainerSize)
            return
        state.update { it.copy(listsContainerSize = lc) }
    }

    fun onReadmeOpen() {
        launchExDefault {
            KvDb.KEY.HOME_README_OPEN_TIME.upsertInt(time())
        }
    }

    fun toggleIsPurple() {
        state.update { it.copy(isPurple = !it.isPurple) }
    }

    fun toggleIsTasksVisible() {
        state.update { it.copy(isTasksVisible = !it.isTasksVisible) }
    }

    private suspend fun upTodayIntervalsUI() {
        val utcOffset = localUtcOffsetWithDayStart
        val todayDS = UnixTime(utcOffset = utcOffset).localDay
        val todayIntervalsUI = DayIntervalsUi
            .buildList(
                dayStart = todayDS,
                dayFinish = todayDS,
                utcOffset = utcOffset,
            )
            .first()
        state.update { it.copy(todayIntervalsUi = todayIntervalsUI) }
    }

    ///

    data class ListsContainerSize(
        val totalHeight: Float,
        val itemHeight: Float,
    )

    data class ListsSizes(
        val checklist: Float,
        val mainTasks: Float,
    )

    ///

    data class BatteryUi(
        val text: String,
        val colorRgba: ColorRgba,
        val isHighlighted: Boolean,
    )

    class GoalBarUi(
        val goalDb: GoalDb,
        val goalTf: TextFeatures,
        val activityDb: ActivityDb,
        val textLeft: String,
        val textRight: String,
        val ratio: Float,
    ) {

        val bgColor: ColorRgba = activityDb.colorRgba

        fun startInterval() {
            val timer: Int = goalTf.timer ?: (45 * 60)
            launchExIo {
                IntervalDb.addWithValidation(
                    timer = timer,
                    activity = activityDb,
                    note = goalDb.note,
                )
            }
        }
    }

    class MainTask(
        val taskUi: TaskUi,
    ) {

        val text = taskUi.tf.textUi()
        val timerContext = ActivityTimerSheetVm.TimerContext.Task(taskUi.taskDb)
        val timeUI: TimeUI? = taskUi.tf.calcTimeData()?.let { timeData ->
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

private fun prepGoalTextLeft(
    note: String,
    secondsLeft: Int,
): String {
    if (secondsLeft == 0)
        return note
    val rem = secondsLeft % 60
    val secondsToUi = if (rem == 0) secondsLeft else (secondsLeft + (60 - rem))
    return "$note ${secondsToUi.toTimerHintNote(isShort = false)}"
}
