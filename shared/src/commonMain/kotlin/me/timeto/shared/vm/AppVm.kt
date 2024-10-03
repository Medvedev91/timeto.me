package me.timeto.shared.vm

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.*

class AppVm : __Vm<AppVm.State>() {

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

    override fun onAppear() {
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
                .getLastOneOrNullFlow()
                .filterNotNull()
                .onEachExIn(this) { lastInterval ->
                    ActivityDb.syncTimeHints()
                    rescheduleNotifications()
                    performShortcut(lastInterval, secondsLimit = 3)
                    keepScreenOnStateFlow.emit(lastInterval.getActivityDI().keepScreenOn)
                }

            ActivityDb
                .anyChangeFlow()
                .drop(1)
                .onEachExIn(this) {
                    // In case the pomodoro changes
                    rescheduleNotifications()
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
                    } catch (e: CancellationException) {
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
            rescheduleNotifications()
        }
    }
}

private fun performShortcut(
    interval: IntervalDb,
    secondsLimit: Int,
) {
    if ((interval.id + secondsLimit) < time())
        return

    val shortcut: ShortcutDb? =
        interval.note?.textFeatures()?.shortcuts?.firstOrNull()
            ?: interval.getActivityDI().name.textFeatures().shortcuts.firstOrNull()

    shortcut?.performUI()
}

///
/// Sync today

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
    val todayFolder = Cache.getTodayFolder()
    Cache.tasks
        .filter { it.isTmrw && (it.unixTime(utcOffset = utcOffsetDS).localDay < todayDay) }
        .forEach { task ->
            launchExDefault {
                task.upFolder(
                    newFolder = todayFolder,
                    replaceIfTmrw = false // No matter
                )
            }
        }
}

//////

private suspend fun fillInitData() {

    syncTodayEventsLastDay = null
    syncTodayRepeatingLastDay = null
    pingLastDay = null

    // TRICK time() only for SMDAY
    TaskFolderDb.addRaw(TaskFolderDb.ID_TODAY, "Today", 1)
    TaskFolderDb.addTmrw()
    TaskFolderDb.addRaw(time(), "SMDAY", 3)

    KvDb.KEY.WHATS_NEW_CHECK_UNIX_DAY.upsertInt(WhatsNewVm.prepHistoryItemsUi().first().unixDay)

    val colorsWheel = Wheel(ActivityDb.colors)
    val cGreen = colorsWheel.next()
    val cBlue = colorsWheel.next()
    val cRed = colorsWheel.next()
    val cYellow = colorsWheel.next()
    val cPurple = colorsWheel.next()

    // @formatter:off
    val goals = listOf<ActivityDb.Goal>()
    val defData = ActivityDb__Data.buildDefault()
    val aNormal = ActivityDb.TYPE.NORMAL
    val min5 = 5 * 60
    val actMed = ActivityDb.addWithValidation("Meditation", "🧘‍♀️", 20 * 60, 1, aNormal, cYellow, defData, true, goals, min5)
    val actWork = ActivityDb.addWithValidation("Work", "📁", 40 * 60, 2, aNormal, cBlue, defData, true, goals, min5)
    ActivityDb.addWithValidation("Hobby", "🎸", 3600, 3, aNormal, cRed, defData, true, goals, min5)
    val actPd = ActivityDb.addWithValidation("Personal development", "📖", 30 * 60, 4, aNormal, cPurple, defData, true, goals, min5)
    val actEx = ActivityDb.addWithValidation("Exercises / Health", "💪", 20 * 60, 5, aNormal, colorsWheel.next(), defData, false, goals, min5)
    ActivityDb.addWithValidation("Walk", "👟", 30 * 60, 6, aNormal, colorsWheel.next(), defData, false, goals, min5)
    val actGr = ActivityDb.addWithValidation("Getting ready", "🚀", 30 * 60, 7, aNormal, colorsWheel.next(), defData, true, goals, min5)
    ActivityDb.addWithValidation("Sleep / Rest", "😴", 8 * 3600, 8, aNormal, cGreen, defData, false, goals, min5)
    val actOther = ActivityDb.addWithValidation("Other", "💡", 3600, 9, ActivityDb.TYPE.OTHER, colorsWheel.next(), defData, true, goals, min5)

    val interval = IntervalDb.addWithValidation(30 * 60, actPd, null)
    Cache.fillLateInit(interval, interval) // To 100% ensure

    val todayDay = UnixTime().localDay
    fun prepRep(title: String, activity: ActivityDb, timerMin: Int): String =
        title.textFeatures().copy(activity = activity, timer = timerMin * 60).textWithFeatures()
    RepeatingDb.addWithValidation(prepRep("Exercises", actEx, 30), RepeatingDb.Period.EveryNDays(1), todayDay, null, false)
    RepeatingDb.addWithValidation(prepRep("Meditation", actMed, 20), RepeatingDb.Period.EveryNDays(1), todayDay, null, false)
    RepeatingDb.addWithValidation(prepRep("Small tasks", actOther, 30), RepeatingDb.Period.EveryNDays(1), todayDay, null, false)
    RepeatingDb.addWithValidation(prepRep("Getting ready", actGr, 20), RepeatingDb.Period.EveryNDays(1), todayDay, null, false)
    RepeatingDb.addWithValidation(prepRep("Weekly plan", actWork, 20), RepeatingDb.Period.DaysOfWeek(listOf(0)), todayDay, null, false)
    // @formatter:on
}
