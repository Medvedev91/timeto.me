package me.timeto.shared.vm

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.*
import me.timeto.shared.misc.time
import me.timeto.shared.ui.goals.form.GoalFormData
import me.timeto.shared.ui.shortcuts.ShortcutPerformer
import me.timeto.shared.ui.whats_new.WhatsNewVm

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
                .onEachExIn(this) { lastInterval ->
                    rescheduleNotifications()
                    performShortcutForInterval(lastInterval, secondsLimit = 3)
                    keepScreenOnStateFlow.emit(lastInterval.selectActivityDbCached().keepScreenOn)
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

private fun performShortcutForInterval(
    intervalDb: IntervalDb,
    secondsLimit: Int,
) {
    if ((intervalDb.id + secondsLimit) < time())
        return

    val shortcutDb: ShortcutDb =
        intervalDb.note?.textFeatures()?.shortcuts?.firstOrNull()
        ?: intervalDb.selectActivityDbCached().name.textFeatures().shortcuts.firstOrNull()
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
            launchExDefault {
                task.upFolder(
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

    // TRICK time() only for SMDAY
    TaskFolderDb.insertNoValidation(TaskFolderDb.ID_TODAY, "Today", 1)
    TaskFolderDb.insertTmrw()
    TaskFolderDb.insertNoValidation(time(), "SMDAY", 3)

    KvDb.KEY.WHATS_NEW_CHECK_UNIX_DAY.upsertInt(WhatsNewVm.historyItemsUi.first().unixDay)

    val colorsWheel = Wheel(ActivityDb.colors)
    val cGreen = colorsWheel.next()
    val cBlue = colorsWheel.next()
    val cRed = colorsWheel.next()
    val cYellow = colorsWheel.next()
    val cPurple = colorsWheel.next()

    // @formatter:off
    val goals = listOf<GoalFormData>()
    val aNormal = ActivityDb.Type.general
    val min5 = 5 * 60
    val actMed = ActivityDb.addWithValidation("Meditation", "🧘‍♀️", 20 * 60, 1, aNormal, cYellow, true, goals, min5, emptySet())
    val actWork = ActivityDb.addWithValidation("Work", "📁", 40 * 60, 2, aNormal, cBlue, true, goals, min5, emptySet())
    ActivityDb.addWithValidation("Hobby", "🎸", 3600, 3, aNormal, cRed, true, goals, min5, emptySet())
    val actPd = ActivityDb.addWithValidation("Personal development", "📖", 30 * 60, 4, aNormal, cPurple, true, goals, min5, emptySet())
    val actEx = ActivityDb.addWithValidation("Exercises / Health", "💪", 20 * 60, 5, aNormal, colorsWheel.next(), false, goals, min5, emptySet())
    ActivityDb.addWithValidation("Walk", "👟", 30 * 60, 6, aNormal, colorsWheel.next(), false, goals, min5, emptySet())
    val actGr = ActivityDb.addWithValidation("Getting ready", "🚀", 30 * 60, 7, aNormal, colorsWheel.next(), true, goals, min5, emptySet())
    ActivityDb.addWithValidation("Sleep / Rest", "😴", 8 * 3600, 8, aNormal, cGreen, false, goals, min5, emptySet())
    val actOther = ActivityDb.addWithValidation("Other", "💡", 3600, 9, ActivityDb.Type.other, colorsWheel.next(), true, goals, min5, emptySet())

    val interval = IntervalDb.insertWithValidation(30 * 60, actPd, null)
    Cache.fillLateInit(interval, interval) // To 100% ensure

    val todayDay = UnixTime().localDay
    fun prepRep(title: String, activity: ActivityDb, timerMin: Int): String =
        title.textFeatures().copy(activity = activity, timer = timerMin * 60).textWithFeatures()
    RepeatingDb.insertWithValidationEx(prepRep("Exercises", actEx, 30), RepeatingDb.Period.EveryNDays(1), todayDay, null, false)
    RepeatingDb.insertWithValidationEx(prepRep("Meditation", actMed, 20), RepeatingDb.Period.EveryNDays(1), todayDay, null, false)
    RepeatingDb.insertWithValidationEx(prepRep("Small tasks", actOther, 30), RepeatingDb.Period.EveryNDays(1), todayDay, null, false)
    RepeatingDb.insertWithValidationEx(prepRep("Getting ready", actGr, 20), RepeatingDb.Period.EveryNDays(1), todayDay, null, false)
    RepeatingDb.insertWithValidationEx(prepRep("Weekly plan", actWork, 20), RepeatingDb.Period.DaysOfWeek(setOf(0)), todayDay, null, false)
    // @formatter:on
}
