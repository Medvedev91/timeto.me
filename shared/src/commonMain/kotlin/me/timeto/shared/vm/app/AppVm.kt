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
import kotlin.time.Duration.Companion.milliseconds

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

            // todo remove migration starts July 2026
            val allTaskFolders = TaskFolderDb.selectAllSorted()
            if (!allTaskFolders.any { it.isTomorrow })
                TaskFolderDb.insertNoValidation(id = TaskFolderDb.ID_TOMORROW, sort = 2, activityDb = null, name = "Tomorrow", symbol = Symbol.Icon.IconEnum.moon.toIcon())
            if (!allTaskFolders.any { it.isSomeday }) {
                val smday = allTaskFolders.firstOrNull { it.name.lowercase() == "smday" }
                if (smday != null) {
                    db.transaction {
                        db.taskFolderQueries.updateIdTodoRemove(newId = TaskFolderDb.ID_SOMEDAY, oldId = smday.id)
                        db.taskQueries.updateFolderIdTodoRemove(newFolderId = TaskFolderDb.ID_SOMEDAY, oldFolderId = smday.id)
                    }
                } else {
                    TaskFolderDb.insertNoValidation(id = TaskFolderDb.ID_SOMEDAY, sort = 3, activityDb = null, name = "Someday", symbol = Symbol.Icon.IconEnum.inbox.toIcon())
                }
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
                syncTomorrow(todayWithDayStartOffset)
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
            delay(delayMls.milliseconds)
            NotificationAlarm.rescheduleAll()
        }
    }
}

private fun performShortcutForInterval(
    intervalDb: IntervalDb,
    secondsLimit: Int,
) {
    if ((intervalDb.time + secondsLimit) < time())
        return

    val text: String = ("${intervalDb.note ?: ""} ${intervalDb.selectActivityDbCached().name}")
    val shortcutDb: ShortcutDb = text.textFeatures().shortcutsDb.firstOrNull() ?: return

    ShortcutPerformer.perform(shortcutDb)
}

///

private suspend fun syncTomorrow(todayWithDayStartOffset: Int) {
    val todayFolder: TaskFolderDb = TaskFolderDb.selectAllSorted().first { it.isToday }
    val dayStartOffsetSeconds: Int = DayStartOffsetUtils.getOffsetSeconds()
    Cache.tasksDb
        .filter { it.isTomorrow }
        .filter {
            DayStartOffsetUtils.calcDay(
                time = it.id,
                dayStartOffsetSeconds = dayStartOffsetSeconds,
            ) < todayWithDayStartOffset
        }
        .forEach { taskDb ->
            launchExIo {
                taskDb.updateFolder(
                    taskFolderDb = todayFolder,
                    updateFolderActivity = true,
                    replaceIfTmrw = false, // No matter
                )
            }
        }
}

///

private suspend fun fillInitData(
    withDemoData: Boolean,
) {
    NoteFolderDb.insertNoValidation(
        id = 1,
        sort = 0,
        onHome = true,
        symbol = Symbol.Icon.IconEnum.pencil_note.toIcon(),
        name = "Notes",
    )

    TaskFolderDb.insertNoValidation(
        id = TaskFolderDb.ID_TODAY,
        sort = 1,
        activityDb = null,
        name = "Today",
        symbol = Symbol.Icon.IconEnum.sun.toIcon(),
    )
    TaskFolderDb.insertNoValidation(
        id = TaskFolderDb.ID_TOMORROW,
        sort = 2,
        activityDb = null,
        name = "Tomorrow",
        symbol = Symbol.Icon.IconEnum.moon.toIcon(),
    )
    TaskFolderDb.insertNoValidation(
        id = TaskFolderDb.ID_SOMEDAY,
        sort = 3,
        activityDb = null,
        name = "Someday",
        symbol = Symbol.Icon.IconEnum.inbox.toIcon(),
    )

    KvDb.KEY.WHATS_NEW_CHECK_UNIX_DAY.upsertInt(WhatsNewVm.historyItemsUi.first().unixDay)

    val (morningActivityDb, initIntervalDb) = addMorningActivityAndStartInterval()
    val workActivityDb = addWorkActivity()
    val smallTasksActivityDb = addSmallTasksActivity()
    val readingActivityDb = addReadingActivity()
    val workoutActivityDb = addWorkoutActivity()
    val freeTimeActivityDb = addFreeTimeActivity()
    val sleepActivityDb = addSleepActivity()

    Cache.fillLateInit(initIntervalDb, initIntervalDb) // To 100% ensure

    // Demo
    if (withDemoData) {
        fillDemoData(
            morningActivityDb = morningActivityDb,
            workActivityDb = workActivityDb,
            smallTasksActivityDb = smallTasksActivityDb,
            readingActivityDb = readingActivityDb,
            workoutActivityDb = workoutActivityDb,
            freeTimeActivityDb = freeTimeActivityDb,
            sleepActivityDb = sleepActivityDb,
        )
    }
}

//
// Activities

private val everyDayActivityPeriod: ActivityDb.Period =
    ActivityDb.Period.DaysOfWeek.everyDay

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
    val timerSeconds = 2 * 3_600
    val activityDb = ActivityDb.insertWithValidation(
        name = activityTitle,
        goalType = ActivityDb.GoalType.Checklist,
        timerType = ActivityDb.TimerType.FixedTimer(timer = timerSeconds),
        period = everyDayActivityPeriod,
        symbol = Symbol.Icon.IconEnum.rocket.toIcon(),
        colorRgba = Palette.indigo.dark,
        keepScreenOn = true,
        pomodoroTimer = 5 * 60,
        timerHints = listOf(30 * 60, 60 * 60, 60 * 60 + 30 * 60),
        parentActivityDb = null,
        type = ActivityDb.Type.general,
    )
    activityDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 0, cellIdx = 0, size = 2))
    // Start Goal
    return activityDb to activityDb.startTimer(timerSeconds)
}

private suspend fun addWorkActivity(): ActivityDb {
    // Checklist
    val checklistDb = ChecklistDb.insertWithValidation("Work", isResetOnDayStarts = true)
    ChecklistItemDb.insertWithValidation("Track Working Hours", checklistDb, false)
    // Activity
    val activityTitle = "Work".textFeatures()
        .copy(checklistsDb = listOf(checklistDb))
        .textWithFeatures()
    val activityDb = ActivityDb.insertWithValidation(
        name = activityTitle,
        goalType = ActivityDb.GoalType.Checklist,
        timerType = ActivityDb.TimerType.StopwatchDaily,
        period = everyDayActivityPeriod,
        symbol = Symbol.Icon.IconEnum.instruments.toIcon(),
        colorRgba = Palette.blue.dark,
        keepScreenOn = true,
        pomodoroTimer = 5 * 60,
        timerHints = listOf(60 * 60, 4 * 60 * 60, 8 * 60 * 60),
        parentActivityDb = null,
        type = ActivityDb.Type.general,
    )
    activityDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 0, cellIdx = 2, size = 2))
    return activityDb
}

private suspend fun addSmallTasksActivity(): ActivityDb {
    val activityDb = ActivityDb.insertWithValidation(
        name = "Small Tasks",
        goalType = ActivityDb.GoalType.Timer(seconds = 30 * 60),
        timerType = ActivityDb.TimerType.RestOfGoal,
        period = everyDayActivityPeriod,
        symbol = Symbol.Icon.IconEnum.bolt.toIcon(),
        colorRgba = Palette.cyan.light,
        keepScreenOn = true,
        pomodoroTimer = 5 * 60,
        timerHints = listOf(30 * 60, 60 * 60),
        parentActivityDb = null,
        type = ActivityDb.Type.general,
    )
    activityDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 0, cellIdx = 4, size = 2))
    return activityDb
}

private suspend fun addReadingActivity(): ActivityDb {
    // Activity
    val activityTitle = "Reading"
    val activityDb = ActivityDb.insertWithValidation(
        name = activityTitle,
        goalType = ActivityDb.GoalType.Counter(count = 2),
        timerType = ActivityDb.TimerType.StopwatchDaily,
        period = everyDayActivityPeriod,
        symbol = Symbol.Icon.IconEnum.book.toIcon(),
        colorRgba = Palette.purple.dark,
        keepScreenOn = true,
        pomodoroTimer = 5 * 60,
        timerHints = listOf(30 * 60, 60 * 60),
        parentActivityDb = null,
        type = ActivityDb.Type.general,
    )
    activityDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 1, cellIdx = 0, size = 1))
    return activityDb
}

private suspend fun addWorkoutActivity(): ActivityDb {
    // Checklist
    val checklistDb = ChecklistDb.insertWithValidation("Workout", isResetOnDayStarts = true)
    ChecklistItemDb.insertWithValidation("Smart Watch", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Bottle of Water", checklistDb, false)
    // Activity
    val activityTitle = "Workout".textFeatures()
        .copy(checklistsDb = listOf(checklistDb))
        .textWithFeatures()
    val activityDb = ActivityDb.insertWithValidation(
        name = activityTitle,
        goalType = ActivityDb.GoalType.Counter(count = 1),
        timerType = ActivityDb.TimerType.StopwatchZero,
        period = everyDayActivityPeriod,
        symbol = Symbol.Icon.IconEnum.exercise.toIcon(),
        colorRgba = Palette.orange.dark,
        keepScreenOn = false,
        pomodoroTimer = 5 * 60,
        timerHints = listOf(20 * 60, 60 * 60, 3 * 60 * 60),
        parentActivityDb = null,
        type = ActivityDb.Type.general,
    )
    activityDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 1, cellIdx = 1, size = 1))
    return activityDb
}

private suspend fun addFreeTimeActivity(): ActivityDb {
    // Shopping
    val shoppingDb = ChecklistDb.insertWithValidation("Shopping", isResetOnDayStarts = false)
    ChecklistItemDb.insertWithValidation("Bread", shoppingDb, false)
    ChecklistItemDb.insertWithValidation("Butter", shoppingDb, false)
    ChecklistItemDb.insertWithValidation("Milk", shoppingDb, false)
    // Checklist
    val checklistDb = ChecklistDb.insertWithValidation("Free Time", isResetOnDayStarts = true)
    val shoppingItemTitle: String =
        "Shopping".textFeatures().copy(checklistsDb = listOf(shoppingDb)).textWithFeatures()
    ChecklistItemDb.insertWithValidation(shoppingItemTitle, checklistDb, false)
    // Activity
    val activityTitle = "Free Time".textFeatures()
        .copy(checklistsDb = listOf(checklistDb))
        .textWithFeatures()
    val activityDb = ActivityDb.insertWithValidation(
        name = activityTitle,
        goalType = null,
        timerType = ActivityDb.TimerType.StopwatchZero,
        period = everyDayActivityPeriod,
        symbol = Symbol.Icon.IconEnum.bulb.toIcon(),
        colorRgba = Palette.gray.dark,
        keepScreenOn = true,
        pomodoroTimer = 5 * 60,
        timerHints = listOf(15 * 60, 60 * 60),
        parentActivityDb = null,
        type = ActivityDb.Type.other,
    )
    activityDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 1, cellIdx = 2, size = 2))
    return activityDb
}

private suspend fun addSleepActivity(): ActivityDb {
    // Checklist
    val checklistDb = ChecklistDb.insertWithValidation("Sleep", isResetOnDayStarts = true)
    ChecklistItemDb.insertWithValidation("Glass of Water", checklistDb, true)
    ChecklistItemDb.insertWithValidation("Check Tomorrow", checklistDb, false)
    // Activity
    val activityTitle = "Sleep".textFeatures()
        .copy(checklistsDb = listOf(checklistDb))
        .textWithFeatures()
    val activityDb = ActivityDb.insertWithValidation(
        name = activityTitle,
        goalType = null,
        timerType = ActivityDb.TimerType.StopwatchZero,
        period = everyDayActivityPeriod,
        symbol = Symbol.Icon.IconEnum.moon_stars.toIcon(),
        colorRgba = Palette.green.dark,
        keepScreenOn = false,
        pomodoroTimer = 5 * 60,
        timerHints = listOf(60 * 60, 7 * 60 * 60),
        parentActivityDb = null,
        type = ActivityDb.Type.general,
    )
    activityDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 1, cellIdx = 4, size = 2))
    return activityDb
}
