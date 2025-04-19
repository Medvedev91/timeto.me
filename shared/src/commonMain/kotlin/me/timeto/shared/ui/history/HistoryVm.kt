package me.timeto.shared.ui.history

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.misc.time
import me.timeto.shared.vm.__Vm

class HistoryVm : __Vm<HistoryVm.State>() {

    data class State(
        val daysUi: List<DayUi>,
    )

    override val state = MutableStateFlow(
        State(
            daysUi = emptyList(),
        )
    )

    init {
        val now = UnixTime()
        val scopeVm = scopeVm()
        IntervalDb.selectBetweenIdDescFlow(
            timeStart = now.inDays(-10).time,
            timeFinish = Int.MAX_VALUE,
        ).map { it.reversed() }.onEachExIn(scopeVm) { intervalsDbAsc ->
            state.update {
                it.copy(
                    daysUi = makeDaysUi(intervalsDbAsc = intervalsDbAsc),
                )
            }
        }
    }

    ///

    class DayUi(
        val unixDay: Int,
        val intervalsDb: List<IntervalDb>,
        val nextIntervalTimeStart: Int,
    ) {

        val intervalsUi: List<IntervalUi> = intervalsDb.map { intervalDb ->
            val unixTime: UnixTime = intervalDb.unixTime()
            val activityDb: ActivityDb = intervalDb.selectActivityDbCached()

            val sectionDayTimeStart: Int = UnixTime.byLocalDay(unixDay).time
            val sectionDayTimeFinish: Int = sectionDayTimeStart + 86400 - 1

            val finishTime: Int =
                intervalsDb.getNextOrNull(intervalDb)?.id ?: nextIntervalTimeStart
            val seconds: Int = finishTime - intervalDb.id
            val barTimeFinish: Int = sectionDayTimeFinish.limitMax(finishTime)

            IntervalUi(
                intervalDb = intervalDb,
                activityDb = activityDb,
                isStartsPrevDay = unixTime.localDay < unixDay,
                text = (intervalDb.note ?: activityDb.name).textFeatures().textUi(
                    withActivityEmoji = false,
                    withTimer = false,
                ),
                secondsForBar = barTimeFinish - sectionDayTimeStart.limitMin(intervalDb.id),
                barTimeFinish = barTimeFinish,
                timeString = unixTime.getStringByComponents(UnixTime.StringComponent.hhmm24),
                periodString = makePeriodString(seconds),
                color = activityDb.colorRgba,
            )
        }

        val dayText: String = UnixTime.byLocalDay(unixDay).getStringByComponents(
            UnixTime.StringComponent.dayOfMonth,
            UnixTime.StringComponent.space,
            UnixTime.StringComponent.month,
            UnixTime.StringComponent.comma,
            UnixTime.StringComponent.space,
            UnixTime.StringComponent.dayOfWeek3,
        )
    }

    data class IntervalUi(
        val intervalDb: IntervalDb,
        val activityDb: ActivityDb,
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

private fun makeDaysUi(
    intervalsDbAsc: List<IntervalDb>,
): List<HistoryVm.DayUi> {
    val daysUi: MutableList<HistoryVm.DayUi> = mutableListOf()

    // "last" I mean the last while iteration
    var lastDay = UnixTime(intervalsDbAsc.first().id).localDay
    var lastList = mutableListOf<IntervalDb>()

    intervalsDbAsc.forEach { interval ->
        val intervalTime = interval.unixTime()
        if (lastDay == intervalTime.localDay) {
            lastList.add(interval)
        } else {
            daysUi.add(
                HistoryVm.DayUi(
                    unixDay = lastDay,
                    intervalsDb = lastList,
                    nextIntervalTimeStart = interval.id,
                )
            )
            lastDay = intervalTime.localDay
            // If the interval starts at 00:00 the tail from the previous day is not needed
            lastList = if (intervalTime.localDayStartTime() == intervalTime.time) {
                mutableListOf(interval)
            } else {
                mutableListOf(lastList.last(), interval)
            }
        }
    }
    if (lastList.isNotEmpty()) {
        daysUi.add(
            HistoryVm.DayUi(
                unixDay = lastDay,
                intervalsDb = lastList,
                nextIntervalTimeStart = time(),
            )
        )
    }

    return daysUi
}
