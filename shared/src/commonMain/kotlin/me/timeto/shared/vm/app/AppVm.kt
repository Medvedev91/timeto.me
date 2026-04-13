package me.timeto.shared.vm.app

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.*
import me.timeto.shared.time
import me.timeto.shared.ShortcutPerformer
import me.timeto.shared.db.KvDb.Companion.isSendingReports
import me.timeto.shared.vm.whats_new.WhatsNewVm
import me.timeto.shared.vm.Vm

class AppVm : Vm<AppVm.State>() {

    companion object {

        val backupStateFlow = MutableStateFlow<String?>(null)
    }

    data class State(
        val isAppReady: Boolean,
        val backupMessage: String?,
    )

    override val state = MutableStateFlow(
        State(
            isAppReady = false,
            backupMessage = null,
        )
    )

    init {
        scopeVm().launchEx {

            initKmpDeferred.await()

            if (!Cache.isLateInitInitialized())
                fillInitData(withDemoData = false)

            // todo remove migration starts May 2026
            ActivityDb.selectAll().forEach { activityDb ->
                val goal_json: String = activityDb.goal_json ?: return@forEach
                val oldTimer: Int = goal_json.toIntOrNull() ?: return@forEach
                activityDb.updateGoal(ActivityDb.GoalType.Timer(seconds = oldTimer))
            }

            state.update { it.copy(isAppReady = true) }

            ///

            backupStateFlow
                .drop(1)
                .onEachExIn(this) { newMessage ->
                    state.update { it.copy(backupMessage = newMessage) }
                }

            IntervalDb
                .selectLastOneOrNullFlow()
                .filterNotNull()
                .onEachExIn(this) { lastIntervalDb ->
                    NotificationAlarm.rescheduleAll()
                    performShortcutForInterval(lastIntervalDb, secondsLimit = 3)
                    keepScreenOnStateFlow.emit(lastIntervalDb.selectActivityDb().keepScreenOn)
                }

            DayStartOffsetUtils.buildTodayFlow().onEachExIn(this) { todayWithDayStartOffset ->
                ChecklistDb.selectAsc().forEach { checklistDb ->
                    checklistDb.resetIfNeeded(todayWithDayStartOffset = todayWithDayStartOffset)
                }
                RepeatingDb.syncTodaySafe(todayWithDayStartOffset)
                syncTmrw(todayWithDayStartOffset)
            }

            TimeFlows.todayFlow.onEachExIn(this) { today ->
                EventDb.syncTodaySafe(today)
            }

            combine(
                KvDb.KEY.IS_SENDING_REPORTS.selectOrNullFlow()
                    .map { it.isSendingReports() }.distinctUntilChanged(),
                NotificationsPermission.flow.filterNotNull(),
                TimeFlows.todayFlow,
                pingTriggerFlow,
            ) { isSendingReportsKvDb, notificationsPermission, _, _ ->
                if (!isSendingReportsKvDb)
                    return@combine
                ping(notificationsPermission = notificationsPermission)
            }.launchIn(this)

            NotificationsPermission.flow
                .filter { it == NotificationsPermission.granted }
                .onEachExIn(this) {
                    onNotificationsPermissionReady(delayMls = 0)
                }

            launchEx {
                try {
                    TimeFlows.launchFlows()
                } catch (_: CancellationException) {
                    // On app close
                }
            }
        }
    }

    fun onNotificationsPermissionReady(delayMls: Long) {
        scopeVm().launchEx {
            delay(delayMls)
            NotificationAlarm.rescheduleAll()
        }
    }
}

private fun performShortcutForInterval(
    intervalDb: IntervalDb,
    secondsLimit: Int,
) {
    if ((intervalDb.id + secondsLimit) < time())
        return

    val text: String = ("${intervalDb.note ?: ""} ${intervalDb.selectActivityDbCached().name}")
    val shortcutDb: ShortcutDb = text.textFeatures().shortcutsDb.firstOrNull() ?: return

    ShortcutPerformer.perform(shortcutDb)
}

///

private suspend fun syncTmrw(todayWithDayStartOffset: Int) {
    val todayFolder: TaskFolderDb = TaskFolderDb.selectAllSorted().first { it.isToday }
    val dayStartOffsetSeconds: Int = DayStartOffsetUtils.getOffsetSeconds()
    Cache.tasksDb
        .filter { it.isTmrw }
        .filter {
            DayStartOffsetUtils.calcDay(
                time = it.id,
                dayStartOffsetSeconds = dayStartOffsetSeconds,
            ) < todayWithDayStartOffset
        }
        .forEach { taskDb ->
            launchExIo {
                taskDb.updateFolder(
                    newFolder = todayFolder,
                    replaceIfTmrw = false, // No matter
                )
            }
        }
}

///

private suspend fun fillInitData(
    withDemoData: Boolean,
) {

    TaskFolderDb.insertNoValidation(TaskFolderDb.ID_TODAY, "Today", 1)
    TaskFolderDb.insertTmrw()
    TaskFolderDb.insertNoValidation(time(), "SMDAY", 3)

    KvDb.KEY.WHATS_NEW_CHECK_UNIX_DAY.upsertInt(WhatsNewVm.historyItemsUi.first().unixDay)

    val readingActivityDb = addReadingActivity()
    val workActivityDb = addWorkActivity()
    val exercisesActivityDb = addExercisesActivity()
    val (morningActivityDb, initIntervalDb) = addMorningActivityAndStartInterval()
    val eatingActivityDb = addEatingActivity()
    val commuteActivityDb = addCommuteActivity()
    val freeTimeActivityDb = addFreeTimeActivity()
    val sleepActivityDb = addSleepActivity()

    Cache.fillLateInit(initIntervalDb, initIntervalDb) // To 100% ensure

    // Demo
    if (withDemoData) {
        fillDemoData(
            morningActivityDb = morningActivityDb,
            commuteActivityDb = commuteActivityDb,
            workActivityDb = workActivityDb,
            eatingActivityDb = eatingActivityDb,
            exercisesActivityDb = exercisesActivityDb,
            readingActivityDb = readingActivityDb,
            freeTimeActivityDb = freeTimeActivityDb,
            sleepActivityDb = sleepActivityDb,
        )
    }
}

//
// Activities

private val everyDayActivityPeriod: ActivityDb.Period =
    ActivityDb.Period.DaysOfWeek.everyDay

private suspend fun addReadingActivity(): ActivityDb {
    // Checklist
    val checklistDb = ChecklistDb.insertWithValidation("Reading", isResetOnDayStarts = true)
    ChecklistItemDb.insertWithValidation("Read 30 Pages", checklistDb, false)
    // Activity
    val activityTitle = "Reading".textFeatures()
        .copy(checklistsDb = listOf(checklistDb))
        .textWithFeatures()
    val activityDb = ActivityDb.insertWithValidation(
        name = activityTitle,
        goalType = ActivityDb.GoalType.Timer(seconds = 3_600),
        timerType = ActivityDb.TimerType.RestOfGoal,
        period = everyDayActivityPeriod,
        emoji = "📖",
        colorRgba = Palette.purple.dark,
        keepScreenOn = true,
        pomodoroTimer = 5 * 60,
        timerHints = listOf(30 * 60, 60 * 60),
        parentActivityDb = null,
        type = ActivityDb.Type.general,
    )
    activityDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 2, cellIdx = 4, size = 2))
    return activityDb
}

private suspend fun addWorkActivity(): ActivityDb {
    // Checklist
    val checklistDb = ChecklistDb.insertWithValidation("Work", isResetOnDayStarts = true)
    ChecklistItemDb.insertWithValidation("Workday Plan", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Done", checklistDb, false)
    // Activity
    val activityTitle = "Work".textFeatures()
        .copy(checklistsDb = listOf(checklistDb))
        .textWithFeatures()
    val activityDb = ActivityDb.insertWithValidation(
        name = activityTitle,
        goalType = ActivityDb.GoalType.Timer(seconds = 8 * 3_600),
        timerType = ActivityDb.TimerType.RestOfGoal,
        period = everyDayActivityPeriod,
        emoji = "📁",
        colorRgba = Palette.blue.dark,
        keepScreenOn = true,
        pomodoroTimer = 5 * 60,
        timerHints = listOf(60 * 60, 4 * 60 * 60, 8 * 60 * 60),
        parentActivityDb = null,
        type = ActivityDb.Type.general,
    )
    activityDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 1, cellIdx = 0, size = 6))
    return activityDb
}

private suspend fun addExercisesActivity(): ActivityDb {
    // Checklist
    val checklistDb = ChecklistDb.insertWithValidation("Exercises", isResetOnDayStarts = true)
    ChecklistItemDb.insertWithValidation("Smart Watch", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Bottle of Water", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Shower", checklistDb, false)
    // Activity
    val activityTitle = "Exercises".textFeatures()
        .copy(checklistsDb = listOf(checklistDb))
        .textWithFeatures()
    val activityDb = ActivityDb.insertWithValidation(
        name = activityTitle,
        goalType = ActivityDb.GoalType.Timer(seconds = 3_600),
        timerType = ActivityDb.TimerType.RestOfGoal,
        period = everyDayActivityPeriod,
        emoji = "💪",
        colorRgba = Palette.orange.dark,
        keepScreenOn = false,
        pomodoroTimer = 5 * 60,
        timerHints = listOf(20 * 60, 60 * 60, 3 * 60 * 60),
        parentActivityDb = null,
        type = ActivityDb.Type.general,
    )
    activityDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 2, cellIdx = 2, size = 2))
    return activityDb
}

private suspend fun addMorningActivityAndStartInterval(): Pair<ActivityDb, IntervalDb> {
    // Checklist
    val checklistDb = ChecklistDb.insertWithValidation("Morning", isResetOnDayStarts = true)
    ChecklistItemDb.insertWithValidation("Glass of Water", checklistDb, true)
    ChecklistItemDb.insertWithValidation("Stretching", checklistDb, true)
    ChecklistItemDb.insertWithValidation("Shower", checklistDb, true)
    ChecklistItemDb.insertWithValidation("Breakfast", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Pills", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Day Plan", checklistDb, false)
    // Activity
    val activityTitle = "Morning".textFeatures()
        .copy(checklistsDb = listOf(checklistDb))
        .textWithFeatures()
    val goalSeconds = 3_600
    val activityDb = ActivityDb.insertWithValidation(
        name = activityTitle,
        goalType = ActivityDb.GoalType.Timer(seconds = goalSeconds),
        timerType = ActivityDb.TimerType.RestOfGoal,
        period = everyDayActivityPeriod,
        emoji = "🚀",
        colorRgba = Palette.indigo.dark,
        keepScreenOn = true,
        pomodoroTimer = 5 * 60,
        timerHints = listOf(30 * 60, 60 * 60, 60 * 60 + 30 * 60),
        parentActivityDb = null,
        type = ActivityDb.Type.general,
    )
    activityDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 0, cellIdx = 0, size = 3))
    // Start Goal
    return activityDb to activityDb.startTimer(goalSeconds)
}

private suspend fun addEatingActivity(): ActivityDb {
    val activityDb = ActivityDb.insertWithValidation(
        name = "Eating",
        goalType = ActivityDb.GoalType.Timer(seconds = 3_600),
        timerType = ActivityDb.TimerType.RestOfGoal,
        period = everyDayActivityPeriod,
        emoji = "🥦",
        colorRgba = Palette.indigo.dark,
        keepScreenOn = true,
        pomodoroTimer = 5 * 60,
        timerHints = listOf(15 * 60, 60 * 60),
        parentActivityDb = null,
        type = ActivityDb.Type.general,
    )
    activityDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 2, cellIdx = 0, size = 2))
    return activityDb
}

private suspend fun addCommuteActivity(): ActivityDb {
    val activityDb = ActivityDb.insertWithValidation(
        name = "Commute",
        goalType = ActivityDb.GoalType.Timer(seconds = 3_600),
        timerType = ActivityDb.TimerType.RestOfGoal,
        period = everyDayActivityPeriod,
        emoji = "🚗",
        colorRgba = Palette.cyan.dark,
        keepScreenOn = false,
        pomodoroTimer = 5 * 60,
        timerHints = listOf(30 * 60, 60 * 60),
        parentActivityDb = null,
        type = ActivityDb.Type.general,
    )
    activityDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 0, cellIdx = 3, size = 3))
    return activityDb
}

private suspend fun addFreeTimeActivity(): ActivityDb {
    // Checklist
    val checklistDb = ChecklistDb.insertWithValidation("Free Time", isResetOnDayStarts = true)
    ChecklistItemDb.insertWithValidation("Walk", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Meditation", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Hobby", checklistDb, false)
    ChecklistItemDb.insertWithValidation("News", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Small Tasks", checklistDb, false)
    // Activity
    val activityTitle = "Free Time".textFeatures()
        .copy(checklistsDb = listOf(checklistDb))
        .textWithFeatures()
    val activityDb = ActivityDb.insertWithValidation(
        name = activityTitle,
        goalType = ActivityDb.GoalType.Timer(seconds = 3 * 3_600),
        timerType = ActivityDb.TimerType.RestOfGoal,
        period = everyDayActivityPeriod,
        emoji = "💡",
        colorRgba = Palette.gray.dark,
        keepScreenOn = true,
        pomodoroTimer = 5 * 60,
        timerHints = listOf(15 * 60, 60 * 60),
        parentActivityDb = null,
        type = ActivityDb.Type.other,
    )
    activityDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 3, cellIdx = 0, size = 2))
    return activityDb
}

private suspend fun addSleepActivity(): ActivityDb {
    // Checklist
    val checklistDb = ChecklistDb.insertWithValidation("Sleep", isResetOnDayStarts = true)
    ChecklistItemDb.insertWithValidation("Set Alarm", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Check Tomorrow", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Prepare Breakfast", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Day Reflection", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Wake Up", checklistDb, false)
    // Activity
    val activityTitle = "Sleep".textFeatures()
        .copy(checklistsDb = listOf(checklistDb))
        .textWithFeatures()
    val activityDb = ActivityDb.insertWithValidation(
        name = activityTitle,
        goalType = ActivityDb.GoalType.Timer(seconds = 8 * 3_600),
        timerType = ActivityDb.TimerType.RestOfGoal,
        period = everyDayActivityPeriod,
        emoji = "🌙",
        colorRgba = Palette.green.dark,
        keepScreenOn = false,
        pomodoroTimer = 5 * 60,
        timerHints = listOf(60 * 60, 7 * 60 * 60),
        parentActivityDb = null,
        type = ActivityDb.Type.general,
    )
    activityDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 3, cellIdx = 2, size = 4))
    return activityDb
}
