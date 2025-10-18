package me.timeto.shared.vm.history

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.timeto.shared.*
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.limitMax
import me.timeto.shared.limitMin
import me.timeto.shared.time
import me.timeto.shared.DialogsManager
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.vm.history.form.HistoryFormUtils
import me.timeto.shared.vm.Vm

private const val initInDays: Int = -1

class HistoryVm : Vm<HistoryVm.State>() {

    private var inDaysGlobal: Int = initInDays

    data class State(
        val daysUi: List<DayUi>,
    )

    override val state = MutableStateFlow(
        State(
            daysUi = emptyList(),
        )
    )

    init {
        val scopeVm = scopeVm()
        scopeVm.launchEx {
            // Fix https://developer.apple.com/forums/thread/741406
            if (SystemInfo.instance.os is SystemInfo.Os.Ios) {
                selectAndUpdate(0)
                delay(500) // Doesn't work less than 400
            }
            selectAndUpdate(inDaysGlobal)
        }
        IntervalDb.anyChangeFlow().drop(1).onEachExIn(scopeVm) {
            selectAndUpdate(inDaysGlobal)
        }
    }

    // To update seconds string for the last interval if needed
    // Update only if less 1 min, otherwise twitching last bar
    // that should update on background by timer.
    fun restartDaysUiIfLess1Min() {
        if ((Cache.lastIntervalDb.id + 60) > time())
            restartDaysUi()
    }

    fun restartDaysUi() {
        scopeVm().launch {
            inDaysGlobal = initInDays
            selectAndUpdate(inDaysGlobal)
        }
    }

    fun moveIntervalToTasks(
        intervalDb: IntervalDb,
        dialogsManager: DialogsManager,
    ) {
        HistoryFormUtils.moveToTasksUi(
            intervalDb = intervalDb,
            dialogsManager = dialogsManager,
            onSuccess = {},
        )
    }

    fun deleteInterval(
        intervalDb: IntervalDb,
        dialogsManager: DialogsManager,
    ) {
        HistoryFormUtils.deleteIntervalUi(
            intervalDb = intervalDb,
            dialogsManager = dialogsManager,
            onSuccess = {},
        )
    }

    private suspend fun selectAndUpdate(inDays: Int) {
        val daysUi = DaysUiUtils.selectDaysUi(
            firstDay = UnixTime().inDays(inDays).localDay,
            lastDay = UnixTime().localDay,
        )
        state.update {
            it.copy(daysUi = daysUi)
        }
    }

    ///

    class DayUi(
        val unixDay: Int,
        val intervalsDb: List<IntervalDb>,
        val nextIntervalTimeStart: Int,
    ) {

        private val dayUnixTime: UnixTime = UnixTime.byLocalDay(unixDay)
        private val dayTimeStart: Int = dayUnixTime.time
        private val dayTimeFinish: Int = dayTimeStart + 86_400 - 1

        val dayText: String = UnixTime.byLocalDay(unixDay).getStringByComponents(
            UnixTime.StringComponent.dayOfMonth,
            UnixTime.StringComponent.space,
            UnixTime.StringComponent.month,
            UnixTime.StringComponent.comma,
            UnixTime.StringComponent.space,
            UnixTime.StringComponent.dayOfWeek3,
        )

        val intervalsUi: List<IntervalUi> = intervalsDb.map { intervalDb ->
            val unixTime: UnixTime = intervalDb.unixTime()
            val goalDb: Goal2Db = intervalDb.selectGoalDbCached()

            val finishTime: Int =
                intervalsDb.getNextOrNull(intervalDb)?.id ?: nextIntervalTimeStart
            val seconds: Int = finishTime - intervalDb.id
            val barTimeFinish: Int = dayTimeFinish.limitMax(finishTime)

            IntervalUi(
                listId = "$unixDay ${intervalDb.id}",
                intervalDb = intervalDb,
                goalDb = goalDb,
                isStartsPrevDay = unixTime.localDay < unixDay,
                text = (intervalDb.note ?: goalDb.name).textFeatures().textUi(
                    withTimer = false,
                ),
                secondsForBar = barTimeFinish - dayTimeStart.limitMin(intervalDb.id),
                barTimeFinish = barTimeFinish,
                timeString = unixTime.getStringByComponents(UnixTime.StringComponent.hhmm24),
                periodString = makePeriodString(seconds),
                color = goalDb.colorRgba,
            )
        }
    }

    data class IntervalUi(
        val listId: String,
        val intervalDb: IntervalDb,
        val goalDb: Goal2Db,
        val isStartsPrevDay: Boolean,
        val text: String,
        val secondsForBar: Int,
        val barTimeFinish: Int,
        val timeString: String,
        val periodString: String,
        val color: ColorRgba,
    )
}

///

private fun makePeriodString(
    seconds: Int,
): String {
    if (seconds < 60)
        return "$seconds sec"
    if (seconds < 3_600)
        return "${seconds / 60} min"
    val (h, m, _) = seconds.toHms()
    if (m == 0)
        return "${h}h"
    return "${h}h ${m.toString().padStart(2, '0')}m"
}

private object DaysUiUtils {

    suspend fun selectDaysUi(
        firstDay: Int,
        lastDay: Int,
    ): List<HistoryVm.DayUi> {
        val intervalsDbAsc = selectIntervalsDb(firstDay, lastDay)
        return makeDaysUi(firstDay, lastDay, intervalsDbAsc)
    }

    private suspend fun selectIntervalsDb(
        firstDay: Int,
        lastDay: Int,
    ): List<IntervalDb> {
        val timeStart: Int = UnixTime.byLocalDay(firstDay).time
        val timeFinish: Int = UnixTime.byLocalDay(lastDay + 1).time - 1

        // One before interval
        val intervalDbBefore: IntervalDb? = IntervalDb.selectBetweenIdDesc(
            timeStart = 0,
            timeFinish = timeStart - 1,
            limit = 1,
        ).firstOrNull()

        // All inside interval
        val dayIntervalsDbAsc = IntervalDb.selectBetweenIdAsc(
            timeStart = timeStart,
            timeFinish = timeFinish,
        )

        // One after interval
        val intervalDbAfter: IntervalDb? = IntervalDb.selectBetweenIdAsc(
            timeStart = timeFinish,
            timeFinish = Int.MAX_VALUE,
            limit = 1,
        ).firstOrNull()

        // Join before + inside + after in right order
        val intervalsDbAsc = mutableListOf<IntervalDb>()
        if (intervalDbBefore != null)
            intervalsDbAsc.add(intervalDbBefore)
        intervalsDbAsc.addAll(dayIntervalsDbAsc)
        if (intervalDbAfter != null)
            intervalsDbAsc.add(intervalDbAfter)

        return intervalsDbAsc
    }

    private fun makeDaysUi(
        firstDay: Int,
        lastDay: Int,
        // Cover - one before, all inside and one after.
        coverIntervalsDbAsc: List<IntervalDb>,
    ): List<HistoryVm.DayUi> {
        val daysUi: MutableList<HistoryVm.DayUi> = mutableListOf()

        var currDay: Int = UnixTime(coverIntervalsDbAsc.first().id).localDay
        var currIntervalsDb = mutableListOf<IntervalDb>()

        coverIntervalsDbAsc.forEach { intervalDb ->
            val intervalTime: UnixTime = intervalDb.unixTime()
            if (currDay == intervalTime.localDay) {
                currIntervalsDb.add(intervalDb)
            } else {
                daysUi.add(
                    HistoryVm.DayUi(
                        unixDay = currDay,
                        intervalsDb = currIntervalsDb,
                        nextIntervalTimeStart = intervalDb.id,
                    )
                )
                currDay = intervalTime.localDay
                // If the interval starts at 00:00 the tail from the previous day is not needed
                currIntervalsDb = if (intervalTime.localDayStartTime() == intervalTime.time) {
                    mutableListOf(intervalDb)
                } else {
                    mutableListOf(currIntervalsDb.last(), intervalDb)
                }
            }
        }

        if (currIntervalsDb.isNotEmpty()) {
            daysUi.add(
                HistoryVm.DayUi(
                    unixDay = currDay,
                    intervalsDb = currIntervalsDb,
                    nextIntervalTimeStart = time(),
                )
            )
        }

        return (firstDay..lastDay).mapNotNull { day ->
            val dayTimeStart: Int = UnixTime.byLocalDay(day).localDayStartTime()
            val dayTimeFinish: Int = dayTimeStart + 86_400 - 1
            val dayUi: HistoryVm.DayUi? = daysUi.firstOrNull { it.unixDay == day }
            if (dayUi != null)
                return@mapNotNull dayUi
            val intervalDbBefore: IntervalDb =
                coverIntervalsDbAsc.lastOrNull { it.id < dayTimeStart } ?: return@mapNotNull null
            val intervalDbAfter: IntervalDb? =
                coverIntervalsDbAsc.firstOrNull { it.id > dayTimeFinish }
            HistoryVm.DayUi(
                unixDay = day,
                intervalsDb = listOf(intervalDbBefore),
                nextIntervalTimeStart = (intervalDbAfter?.id ?: time()),
            )
        }
    }
}
