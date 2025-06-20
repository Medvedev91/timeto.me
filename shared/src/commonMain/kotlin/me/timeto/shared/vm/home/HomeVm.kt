package me.timeto.shared.vm.home

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.timeto.shared.*
import me.timeto.shared.db.*
import me.timeto.shared.db.KvDb.Companion.todayOnHomeScreen
import me.timeto.shared.limitMax
import me.timeto.shared.limitMin
import me.timeto.shared.SystemInfo
import me.timeto.shared.TaskUi
import me.timeto.shared.sortedUi
import me.timeto.shared.time
import me.timeto.shared.DayBarsUi
import me.timeto.shared.TimerStateUi
import me.timeto.shared.vm.activities.timer.ActivityTimerStrategy
import me.timeto.shared.vm.whats_new.WhatsNewVm
import me.timeto.shared.vm.Vm

class HomeVm : Vm<HomeVm.State>() {

    data class State(
        val intervalDb: IntervalDb,
        val isPurple: Boolean,
        val todayTasksUi: List<TaskUi>,
        val todayBarsUi: DayBarsUi?,
        val fdroidMessage: String?,
        val readmeMessage: String?,
        val whatsNewMessage: String?,
        val listsContainerSize: ListsContainerSize?,
        val idToUpdate: Long,
    ) {

        val timerStateUi = TimerStateUi(
            intervalDb = intervalDb,
            todayTasksDb = todayTasksUi.map { it.taskDb },
            isPurple = isPurple,
        )

        val activeActivityDb: ActivityDb =
            intervalDb.selectActivityDbCached()

        // todo or use interval.getTriggers()
        val textFeatures: TextFeatures =
            (intervalDb.note ?: activeActivityDb.name).textFeatures()

        val checklistDb: ChecklistDb? =
            textFeatures.checklistsDb.firstOrNull()

        val extraTriggers = ExtraTriggers(
            checklistsDb = textFeatures.checklistsDb.filter {
                it.id != checklistDb?.id
            },
            shortcutsDb = textFeatures.shortcutsDb,
        )

        // todo performance?
        val goalBarsUi: List<GoalBarUi> = if (todayBarsUi == null)
            listOf()
        else Cache.activitiesDbSorted
            .map { activityDb ->
                activityDb.getGoalsDbCached()
                    .filter { it.buildPeriod().isToday() }
                    .map { goalDb ->

                        val goalTf: TextFeatures = goalDb.note.textFeatures()

                        val dayBarsUiForGoal: List<DayBarsUi.BarUi> = todayBarsUi.barsUi
                            .filter { it.activityDb?.id == activityDb.id }
                            .filter {
                                // Goal without note is common for activity
                                if (goalTf.textNoFeatures.isBlank()) true
                                else goalTf.textNoFeatures == it.intervalTf.textNoFeatures
                            }
                        var totalSeconds: Int = dayBarsUiForGoal.sumOf { it.seconds }
                        val lastWithActivity = todayBarsUi.barsUi
                            .lastOrNull { it.activityDb != null }
                        if (
                            lastWithActivity != null &&
                            lastWithActivity == dayBarsUiForGoal.lastOrNull()
                        ) {
                            val timeFinish = lastWithActivity.timeFinish
                            val now = time()
                            if (now < timeFinish)
                                reportApi("MainActivity goal bad time $now $timeFinish")
                            totalSeconds += (now - timeFinish)
                        }

                        val timeLeft: Int = goalDb.seconds - totalSeconds
                        val textRight: String = run {
                            if (timeLeft > 0) timeLeft.toTimerHintNote(isShort = false)
                            else if (timeLeft == 0) goalDb.finish_text
                            else "+ ${(timeLeft * -1).toTimerHintNote(isShort = false)} ${goalDb.finish_text}"
                        }

                        GoalBarUi(
                            goalDb = goalDb,
                            goalTf = goalTf,
                            activityDb = activityDb,
                            totalSeconds = totalSeconds,
                            textRight = textRight,
                            ratio = totalSeconds.limitMax(goalDb.seconds).toFloat() / goalDb.seconds.toFloat(),
                        )
                    }
            }
            .flatten()

        val mainTasks: List<MainTask> = run {
            val tasksUi: List<TaskUi> =
                if (KvDb.KEY.TODAY_ON_HOME_SCREEN.selectOrNullCached().todayOnHomeScreen())
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
            val checklistCount: Int = checklistDb.getItemsCached().size.limitMin(1)
            val checklistFullHeight: Float = checklistCount * lc.itemHeight
            if (checklistFullHeight < halfHeight)
                return@run ListsSizes(
                    checklist = checklistFullHeight,
                    mainTasks = lc.totalHeight - checklistFullHeight,
                )
            ListsSizes(checklist = halfHeight, mainTasks = halfHeight)
        }
    }

    override val state = MutableStateFlow(
        State(
            intervalDb = Cache.lastIntervalDb,
            isPurple = false,
            todayTasksUi = listOf(),
            todayBarsUi = null, // todo init data
            fdroidMessage = null, // todo init data
            readmeMessage = null, // todo init data
            whatsNewMessage = null, // todo init data
            listsContainerSize = null,
            idToUpdate = 0,
        )
    )

    init {
        val scopeVm = scopeVm()

        IntervalDb.selectLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scopeVm) { intervalDb ->
                state.update {
                    val isNewInterval: Boolean =
                        it.intervalDb.id != intervalDb.id
                    it.copy(
                        intervalDb = intervalDb,
                        isPurple = if (isNewInterval) false else it.isPurple,
                    )
                }
            }

        TaskDb
            .selectAscFlow()
            .map { it.filter { task -> task.isToday } }
            .onEachExIn(scopeVm) { tasks ->
                state.update { it.copy(todayTasksUi = tasks.map { it.toUi() }) }
            }

        if (SystemInfo.instance.isFdroid)
            KvDb.KEY.IS_SENDING_REPORTS
                .selectOrNullFlow()
                .onEachExIn(scopeVm) { kvDb ->
                    state.update {
                        it.copy(fdroidMessage = if (kvDb == null) "Message for F-Droid Users" else null)
                    }
                }

        KvDb.KEY.HOME_README_OPEN_TIME
            .selectOrNullFlow()
            .onEachExIn(scopeVm) { kvDb ->
                state.update {
                    it.copy(readmeMessage = if (kvDb == null) "How to Use the App" else null)
                }
            }

        KvDb.KEY.WHATS_NEW_CHECK_UNIX_DAY
            .selectOrNullFlow()
            .onEachExIn(scopeVm) { kvDb ->
                val lastHistoryUnixDay: Int =
                    WhatsNewVm.historyItemsUi.first().unixDay
                val message: String? =
                    if ((kvDb == null) || (lastHistoryUnixDay > kvDb.value.toInt()))
                        "What's New"
                    else null
                state.update {
                    it.copy(whatsNewMessage = message)
                }
            }

        ///

        IntervalDb.anyChangeFlow()
            .onEachExIn(scopeVm) {
                upTodayBarsUi()
            }
        scopeVm.launch {
            while (true) {
                delayToNextMinute()
                upTodayBarsUi()
            }
        }

        ///

        scopeVm.launch {
            while (true) {
                state.update {
                    it.copy(
                        intervalDb = Cache.lastIntervalDb,
                        idToUpdate = it.idToUpdate + 1, // Force update
                    )
                }
                delay(1_000L)
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
        launchExIo {
            KvDb.KEY.HOME_README_OPEN_TIME.upsertInt(time())
        }
    }

    fun toggleIsPurple() {
        state.update { it.copy(isPurple = !it.isPurple) }
    }

    private suspend fun upTodayBarsUi() {
        val utcOffset = localUtcOffsetWithDayStart
        val todayDS = UnixTime(utcOffset = utcOffset).localDay
        val todayBarsUi = DayBarsUi
            .buildList(
                dayStart = todayDS,
                dayFinish = todayDS,
                utcOffset = utcOffset,
            )
            .first()
        state.update { it.copy(todayBarsUi = todayBarsUi) }
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

    class GoalBarUi(
        val goalDb: GoalDb,
        val goalTf: TextFeatures,
        val activityDb: ActivityDb,
        val totalSeconds: Int,
        val textRight: String,
        val ratio: Float,
    ) {

        val bgColor: ColorRgba = activityDb.colorRgba

        val textLeft = prepGoalTextLeft(
            note = goalTf.textNoFeatures,
            secondsLeft = totalSeconds,
        )

        fun startInterval() {
            val timer: Int = run {
                val goalTimer = goalDb.timer
                if (goalTimer > 0)
                    return@run goalTimer
                val secondsLeft: Int = goalDb.seconds - totalSeconds
                if (secondsLeft > 0)
                    return@run secondsLeft
                45 * 60
            }
            val noteTf: TextFeatures =
                goalDb.note.textFeatures().copy(goalDb = goalDb)
            launchExIo {
                TaskDb.selectAsc()
                    .filter { taskDb ->
                        val tf = taskDb.text.textFeatures()
                        taskDb.isToday && (tf.paused != null) && (tf.goalDb?.id == goalDb.id)
                    }
                    .forEach { taskDb ->
                        taskDb.delete()
                    }
                IntervalDb.insertWithValidation(
                    timer = timer,
                    activityDb = activityDb,
                    note = noteTf.textWithFeatures(),
                )
            }
        }
    }

    class MainTask(
        val taskUi: TaskUi,
    ) {

        val text = taskUi.tf.textUi()

        val timerStrategy: ActivityTimerStrategy =
            ActivityTimerStrategy.Task(taskDb = taskUi.taskDb)

        val timeUi: TimeUi? = taskUi.tf.calcTimeData()?.let { timeData ->
            TimeUi(
                text = timeData.timeText(),
                note = timeData.timeLeftText(),
                status = timeData.status,
            )
        }

        class TimeUi(
            val text: String,
            val note: String,
            val status: TextFeatures.TimeData.STATUS,
        )
    }

    data class ExtraTriggers(
        val checklistsDb: List<ChecklistDb>,
        val shortcutsDb: List<ShortcutDb>,
    )
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
