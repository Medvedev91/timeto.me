package me.timeto.shared.vm.app

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.*
import me.timeto.shared.time
import me.timeto.shared.vm.goals.form.GoalFormData
import me.timeto.shared.ShortcutPerformer
import me.timeto.shared.vm.whats_new.WhatsNewVm
import me.timeto.shared.vm.Vm

class AppVm : Vm<AppVm.State>() {

    companion object {

        // todo
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
                fillInitData()

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

            ActivityDb
                .anyChangeFlow()
                .drop(1)
                .onEachExIn(this) {
                    // In case the pomodoro changes
                    NotificationAlarm.rescheduleAll()
                }

            launchEx {
                while (true) {
                    /**
                     * Not delayToNextMinute(extraMls = 1_000L):
                     * - No need to wait after daytime changes;
                     * - No need to wait after backup restore.
                     */
                    try {
                        delay(1_000L)
                    } catch (_: CancellationException) {
                        break // On app closing
                    }
                    try {
                        syncTmrw()
                        syncTodayEvents()
                        syncTodayRepeating()
                    } catch (e: Throwable) {
                        reportApi("AppVM sync today error:$e")
                        delay(300_000L)
                    }
                }
            }

            launchEx {
                // Delay to not load the system at startup. But not too long
                // because some data may be needed soon, like feedback subject.
                delay(500L)
                while (true) {
                    ping()
                    try {
                        delay(10 * 60 * 1_000L) // 10 min
                    } catch (e: CancellationException) {
                        break // On app closing
                    }
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

    val shortcutDb: ShortcutDb =
        intervalDb.note?.textFeatures()?.shortcutsDb?.firstOrNull()
            ?: intervalDb.selectActivityDbCached().name.textFeatures().shortcutsDb.firstOrNull()
            ?: return

    ShortcutPerformer.perform(shortcutDb)
}

//
// Sync today

private var syncTodayRepeatingLastDay: Int? = null
private suspend fun syncTodayRepeating() {
    val todayWithOffset = RepeatingDb.todayWithOffset()
    if (syncTodayRepeatingLastDay == todayWithOffset)
        return
    RepeatingDb.syncTodaySafe(todayWithOffset)
    // In case on error while syncTodaySafe()
    syncTodayRepeatingLastDay = todayWithOffset
}

private var syncTodayEventsLastDay: Int? = null
private suspend fun syncTodayEvents() {
    // GD "Day Start Offset" -> "Using for Events"
    val todayNoOffset = UnixTime().localDay
    // To avoid unnecessary checks. It works without that.
    if (syncTodayEventsLastDay == todayNoOffset)
        return
    EventDb.syncTodaySafe(todayNoOffset)
    // In case on error while syncTodaySafe()
    syncTodayEventsLastDay = todayNoOffset
}

private fun syncTmrw() {
    // DI to performance
    // Using .localDayWithDayStart() everywhere
    val utcOffsetDS = localUtcOffsetWithDayStart
    val todayDay = UnixTime(utcOffset = utcOffsetDS).localDay
    val todayFolder = Cache.getTodayFolderDb()
    Cache.tasksDb
        .filter { it.isTmrw && (it.unixTime(utcOffset = utcOffsetDS).localDay < todayDay) }
        .forEach { task ->
            launchExIo {
                task.updateFolder(
                    newFolder = todayFolder,
                    replaceIfTmrw = false // No matter
                )
            }
        }
}

///

private suspend fun fillInitData() {

    syncTodayEventsLastDay = null
    syncTodayRepeatingLastDay = null
    pingLastDay = null

    TaskFolderDb.insertNoValidation(TaskFolderDb.ID_TODAY, "Today", 1)
    TaskFolderDb.insertTmrw()
    TaskFolderDb.insertNoValidation(time(), "SMDAY", 3)

    KvDb.KEY.WHATS_NEW_CHECK_UNIX_DAY.upsertInt(WhatsNewVm.historyItemsUi.first().unixDay)

    addPersonalDevelopmentActivity()
    addWorkActivity()
    addExercisesActivity()
    val initIntervalDb = addGettingReadyActivityAndStartGoal()
    addCommuteActivity()
    addFreeTimeActivity()
    addSleepActivity()

    Cache.fillLateInit(initIntervalDb, initIntervalDb) // To 100% ensure
}

//
// Activities

private val everyDayGoalPeriod: GoalDb.Period =
    GoalDb.Period.DaysOfWeek.buildWithValidation(setOf(0, 1, 2, 3, 4, 5, 6))

private suspend fun addPersonalDevelopmentActivity() {
    // Activity
    val activityDb = ActivityDb.addWithValidation(
        name = "Personal Development",
        emoji = "📖",
        timer = 30 * 60,
        sort = InitActivitySort.personalDevelopment.ordinal,
        type = ActivityDb.Type.general,
        colorRgba = Palette.purple.dark,
        keepScreenOn = true,
        goalFormsData = listOf(),
        pomodoroTimer = 5 * 60,
        timerHints = setOf(15 * 60, 45 * 60),
    )
    // Reading Goal
    val readingForm = GoalFormData(null, 3_600, everyDayGoalPeriod, "Reading", "👍", false, 0)
    val readingGoalDb = GoalDb.insertAndGet(activityDb, readingForm)
    readingGoalDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 2, cellIdx = 4, size = 2))
}

private suspend fun addWorkActivity() {
    // Activity
    val activityDb = ActivityDb.addWithValidation(
        name = "Work",
        emoji = "📁",
        timer = 45 * 60,
        sort = InitActivitySort.work.ordinal,
        type = ActivityDb.Type.general,
        colorRgba = Palette.blue.dark,
        keepScreenOn = true,
        goalFormsData = listOf(),
        pomodoroTimer = 5 * 60,
        timerHints = setOf(45 * 60, 2 * 3_600),
    )
    // Goal
    val goalForm = GoalFormData(null, 8 * 3_600, everyDayGoalPeriod, "Work", "✅", true, 0)
    val goalDb = GoalDb.insertAndGet(activityDb, goalForm)
    goalDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 1, cellIdx = 0, size = 6))
}

private suspend fun addExercisesActivity() {
    // Activity
    val activityDb = ActivityDb.addWithValidation(
        name = "Exercises / Health",
        emoji = "💪",
        timer = 20 * 60,
        sort = InitActivitySort.exercises.ordinal,
        type = ActivityDb.Type.general,
        colorRgba = Palette.orange.dark,
        keepScreenOn = false,
        goalFormsData = listOf(),
        pomodoroTimer = 5 * 60,
        timerHints = setOf(5 * 60, 15 * 60, 1 * 3_600),
    )
    // Goal
    val goalForm = GoalFormData(null, 3_600, everyDayGoalPeriod, "Exercises", "💪", true, 0)
    val goalDb = GoalDb.insertAndGet(activityDb, goalForm)
    goalDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 2, cellIdx = 2, size = 2))
}

private suspend fun addGettingReadyActivityAndStartGoal(): IntervalDb {
    // Activity
    val activityDb = ActivityDb.addWithValidation(
        name = "Getting ready",
        emoji = "🚀",
        timer = 30 * 60,
        sort = InitActivitySort.gettingReady.ordinal,
        type = ActivityDb.Type.general,
        colorRgba = Palette.indigo.dark,
        keepScreenOn = true,
        goalFormsData = listOf(),
        pomodoroTimer = 5 * 60,
        timerHints = setOf(15 * 60, 30 * 60),
    )
    // Morning Checklist
    val morningChecklistDb = ChecklistDb.insertWithValidation("Morning")
    ChecklistItemDb.insertWithValidation("Glass of Water", morningChecklistDb, true)
    ChecklistItemDb.insertWithValidation("Shower", morningChecklistDb, true)
    ChecklistItemDb.insertWithValidation("Breakfast", morningChecklistDb, false)
    ChecklistItemDb.insertWithValidation("Day Plan", morningChecklistDb, false)
    // Morning Goal
    val morningGoalTitle = "Morning".textFeatures()
        .copy(checklistsDb = listOf(morningChecklistDb))
        .textWithFeatures()
    val morningGoalForm = GoalFormData(null, 3_600, everyDayGoalPeriod, morningGoalTitle, "⏲️", false, 0)
    val morningGoalDb = GoalDb.insertAndGet(activityDb, morningGoalForm)
    morningGoalDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 0, cellIdx = 0, size = 3))
    // Recharge Checklist
    val rechargeChecklistDb = ChecklistDb.insertWithValidation("Recharge")
    ChecklistItemDb.insertWithValidation("Dinner", rechargeChecklistDb, false)
    ChecklistItemDb.insertWithValidation("Relax", rechargeChecklistDb, false)
    ChecklistItemDb.insertWithValidation("Meditation", rechargeChecklistDb, false)
    // Recharge Goal
    val rechargeGoalTitle = "Recharge".textFeatures()
        .copy(checklistsDb = listOf(rechargeChecklistDb))
        .textWithFeatures()
    val rechargeGoalForm = GoalFormData(null, 2 * 3_600, everyDayGoalPeriod, rechargeGoalTitle, "⏲️", false, 0)
    val rechargeGoalDb = GoalDb.insertAndGet(activityDb, rechargeGoalForm)
    rechargeGoalDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 2, cellIdx = 0, size = 2))
    // Start Goal
    return morningGoalDb.startInterval(DayBarsUi.buildToday().buildGoalStats(morningGoalDb))
}

private suspend fun addCommuteActivity() {
    // Activity
    val activityDb = ActivityDb.addWithValidation(
        name = "Commute",
        emoji = "🚗",
        timer = 30 * 60,
        sort = InitActivitySort.commute.ordinal,
        type = ActivityDb.Type.general,
        colorRgba = Palette.cyan.dark,
        keepScreenOn = false,
        goalFormsData = listOf(),
        pomodoroTimer = 5 * 60,
        timerHints = setOf(30 * 60, 1 * 3_600),
    )
    // Goal
    val goalForm = GoalFormData(null, 3_600, everyDayGoalPeriod, "Commute", "⏲️", true, 0)
    val goalDb = GoalDb.insertAndGet(activityDb, goalForm)
    goalDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 0, cellIdx = 3, size = 3))
}

private suspend fun addFreeTimeActivity() {
    // Activity
    val activityDb = ActivityDb.addWithValidation(
        name = "Free Time",
        emoji = "💡",
        timer = 3_600,
        sort = InitActivitySort.freeTime.ordinal,
        type = ActivityDb.Type.other,
        colorRgba = Palette.gray.dark,
        keepScreenOn = true,
        goalFormsData = listOf(),
        pomodoroTimer = 5 * 60,
        timerHints = setOf(5 * 60, 15 * 60, 3_600),
    )
    // Goal
    val goalForm = GoalFormData(null, 2 * 3_600, everyDayGoalPeriod, "Free Time", "⏲️", true, 0)
    val goalDb: GoalDb = GoalDb.insertAndGet(activityDb, goalForm)
    goalDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 3, cellIdx = 0, size = 2))
}

private suspend fun addSleepActivity() {
    // Activity
    val activityDb = ActivityDb.addWithValidation(
        name = "Sleep",
        emoji = "🌙",
        timer = 8 * 3_600,
        sort = InitActivitySort.sleep.ordinal,
        type = ActivityDb.Type.general,
        colorRgba = Palette.green.dark,
        keepScreenOn = false,
        goalFormsData = listOf(),
        pomodoroTimer = 5 * 60,
        timerHints = setOf(20 * 60, 60 * 60, 6 * 3_600),
    )
    // Goal
    val goalForm = GoalFormData(null, 8 * 3_600, everyDayGoalPeriod, "Sleep", "⏰", true, 0)
    val goalDb: GoalDb = GoalDb.insertAndGet(activityDb, goalForm)
    goalDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 3, cellIdx = 2, size = 4))
}

private enum class InitActivitySort {
    personalDevelopment,
    work,
    exercises,
    gettingReady,
    commute,
    freeTime,
    sleep,
}
