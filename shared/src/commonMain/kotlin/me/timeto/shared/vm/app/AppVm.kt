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
                fillInitData()

            activitiesMigration()

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
                    keepScreenOnStateFlow.emit(lastIntervalDb.selectGoalDb().keepScreenOn)
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

            combine(
                KvDb.KEY.IS_SENDING_REPORTS.selectOrNullFlow()
                    .map { it.isSendingReports() }.distinctUntilChanged(),
                NotificationsPermission.flow.filterNotNull(),
                todayFlow,
                pingTriggerFlow,
            ) { isSendingReportsKvDb, notificationsPermission, _, _ ->
                if (!isSendingReportsKvDb)
                    return@combine
                ping(notificationsPermission = notificationsPermission)
            }.launchIn(this)

            launchEx {
                while (true) {
                    try {
                        delayToNextMinute()
                        todayFlow.emit(UnixTime().localDay)
                    } catch (_: CancellationException) {
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
            ?: intervalDb.selectGoalDbCached().name.textFeatures().shortcutsDb.firstOrNull()
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

    TaskFolderDb.insertNoValidation(TaskFolderDb.ID_TODAY, "Today", 1)
    TaskFolderDb.insertTmrw()
    TaskFolderDb.insertNoValidation(time(), "SMDAY", 3)

    KvDb.KEY.WHATS_NEW_CHECK_UNIX_DAY.upsertInt(WhatsNewVm.historyItemsUi.first().unixDay)

    addReadingGoal()
    addWorkGoal()
    addExercisesGoal()
    val initIntervalDb = addGettingReadyGoalAndStartGoal()
    addCommuteGoal()
    addFreeTimeGoal()
    addSleepGoal()

    Cache.fillLateInit(initIntervalDb, initIntervalDb) // To 100% ensure

    db.kVQueries.upsert(KvDb.KEY.ACTIVITIES_MIGRATED.name, "1")
}

//
// Activities

private val everyDayGoalPeriod: Goal2Db.Period =
    Goal2Db.Period.DaysOfWeek.everyDay

private suspend fun addReadingGoal() {
    val goalDb = Goal2Db.insertWithValidation(
        name = "Reading",
        seconds = 3_600,
        timer = 0,
        period = everyDayGoalPeriod,
        colorRgba = Palette.purple.dark,
        keepScreenOn = true,
        pomodoroTimer = 5 * 60,
        parentGoalDb = null,
        type = Goal2Db.Type.general,
    )
    goalDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 2, cellIdx = 4, size = 2))
}

private suspend fun addWorkGoal() {
    // Checklist
    val checklistDb = ChecklistDb.insertWithValidation("Work")
    ChecklistItemDb.insertWithValidation("Workday Plan", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Retrospective", checklistDb, false)
    // Goal
    val goalTitle = "Work".textFeatures()
        .copy(checklistsDb = listOf(checklistDb))
        .textWithFeatures()
    val goalDb = Goal2Db.insertWithValidation(
        name = goalTitle,
        seconds = 8 * 3_600,
        timer = 0,
        period = everyDayGoalPeriod,
        colorRgba = Palette.blue.dark,
        keepScreenOn = true,
        pomodoroTimer = 5 * 60,
        parentGoalDb = null,
        type = Goal2Db.Type.general,
    )
    goalDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 1, cellIdx = 0, size = 6))
}

private suspend fun addExercisesGoal() {
    // Checklist
    val checklistDb = ChecklistDb.insertWithValidation("Exercises")
    ChecklistItemDb.insertWithValidation("Smart Watch", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Bottle of Water", checklistDb, false)
    // Goal
    val goalTitle = "Exercises".textFeatures()
        .copy(checklistsDb = listOf(checklistDb))
        .textWithFeatures()
    val goalDb = Goal2Db.insertWithValidation(
        name = goalTitle,
        seconds = 3_600,
        timer = 0,
        period = everyDayGoalPeriod,
        colorRgba = Palette.orange.dark,
        keepScreenOn = false,
        pomodoroTimer = 5 * 60,
        parentGoalDb = null,
        type = Goal2Db.Type.general,
    )
    goalDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 2, cellIdx = 2, size = 2))
}

private suspend fun addGettingReadyGoalAndStartGoal(): IntervalDb {
    // Parent Goal
    val parentGoalDb = Goal2Db.insertWithValidation(
        name = "Getting ready",
        seconds = 2 * 3_600,
        timer = 0,
        period = everyDayGoalPeriod,
        colorRgba = Palette.indigo.dark,
        keepScreenOn = true,
        pomodoroTimer = 5 * 60,
        parentGoalDb = null,
        type = Goal2Db.Type.general,
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
    val morningGoalDb = Goal2Db.insertWithValidation(
        name = morningGoalTitle,
        seconds = 3_600,
        timer = 0,
        period = everyDayGoalPeriod,
        colorRgba = Palette.indigo.dark,
        keepScreenOn = true,
        pomodoroTimer = 5 * 60,
        parentGoalDb = parentGoalDb,
        type = Goal2Db.Type.general,
    )
    morningGoalDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 0, cellIdx = 0, size = 3))

    // Eating Goal
    val eatingGoalDb = Goal2Db.insertWithValidation(
        name = "Eating",
        seconds = 3_600,
        timer = 0,
        period = everyDayGoalPeriod,
        colorRgba = Palette.indigo.dark,
        keepScreenOn = true,
        pomodoroTimer = 5 * 60,
        parentGoalDb = parentGoalDb,
        type = Goal2Db.Type.general,
    )
    eatingGoalDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 2, cellIdx = 0, size = 2))

    // Start Goal
    return morningGoalDb.startInterval(DayBarsUi.buildToday().buildGoalStats(morningGoalDb))
}

private suspend fun addCommuteGoal() {
    // Checklist
    val checklistDb = ChecklistDb.insertWithValidation("Commute")
    ChecklistItemDb.insertWithValidation("Podcast", checklistDb, false)
    // Goal
    val goalTitle = "Commute".textFeatures()
        .copy(checklistsDb = listOf(checklistDb))
        .textWithFeatures()
    val goalDb = Goal2Db.insertWithValidation(
        name = goalTitle,
        seconds = 3_600,
        timer = 0,
        period = everyDayGoalPeriod,
        colorRgba = Palette.cyan.dark,
        keepScreenOn = false,
        pomodoroTimer = 5 * 60,
        parentGoalDb = null,
        type = Goal2Db.Type.general,
    )
    goalDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 0, cellIdx = 3, size = 3))
}

private suspend fun addFreeTimeGoal() {
    // Checklist
    val checklistDb = ChecklistDb.insertWithValidation("Free Time")
    ChecklistItemDb.insertWithValidation("Walk", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Meditation", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Hobby", checklistDb, false)
    ChecklistItemDb.insertWithValidation("News", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Small Tasks", checklistDb, false)
    // Goal
    val goalTitle = "Free Time".textFeatures()
        .copy(checklistsDb = listOf(checklistDb))
        .textWithFeatures()
    val goalDb = Goal2Db.insertWithValidation(
        name = goalTitle,
        seconds = 3 * 3_600,
        timer = 0,
        period = everyDayGoalPeriod,
        colorRgba = Palette.gray.dark,
        keepScreenOn = true,
        pomodoroTimer = 5 * 60,
        parentGoalDb = null,
        type = Goal2Db.Type.other,
    )
    goalDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 3, cellIdx = 0, size = 2))
}

private suspend fun addSleepGoal() {
    // Checklist
    val checklistDb = ChecklistDb.insertWithValidation("Sleep")
    ChecklistItemDb.insertWithValidation("Set Alarm", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Check Tomorrow", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Prepare Breakfast", checklistDb, false)
    ChecklistItemDb.insertWithValidation("Day Reflection", checklistDb, false)
    // Goal
    val goalTitle = "Sleep".textFeatures()
        .copy(checklistsDb = listOf(checklistDb))
        .textWithFeatures()
    val goalDb = Goal2Db.insertWithValidation(
        name = goalTitle,
        seconds = 8 * 3_600,
        timer = 0,
        period = everyDayGoalPeriod,
        colorRgba = Palette.green.dark,
        keepScreenOn = false,
        pomodoroTimer = 5 * 60,
        parentGoalDb = null,
        type = Goal2Db.Type.general,
    )
    goalDb.updateHomeButtonSort(HomeButtonSort(rowIdx = 3, cellIdx = 2, size = 4))
}
