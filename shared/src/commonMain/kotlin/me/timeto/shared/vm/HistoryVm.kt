package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.misc.time

class HistoryVm : __Vm<HistoryVm.State>() {

    data class State(
        val daysUi: List<DayUi>,
        val activitiesFormAddUI: List<ActivityFormAddUI>,
    ) {
        val minPickerDay: Int = Cache.firstInterval.unixTime().localDay
    }

    override val state = MutableStateFlow(
        State(
            daysUi = emptyList(),
            activitiesFormAddUI = emptyList(),
        )
    )

    init {
        val now = UnixTime()
        val scopeVm = scopeVm()
        IntervalDb.selectBetweenIdDescFlow(
            timeStart = now.inDays(-10).time,
            timeFinish = now.time,
        ).map { it.reversed() }.onEachExIn(scopeVm) { intervalsDbAsc ->
            state.update {
                it.copy(
                    daysUi = prepDaysUi(intervalsDbAsc = intervalsDbAsc),
                    activitiesFormAddUI = ActivityDb.selectSorted().map { ActivityFormAddUI(it) },
                )
            }
        }
    }

    fun calcDayToMove(selectedDay: Int): Int {
        val availableUnixDays: List<Int> =
            state.value.daysUi.map { it.unixDay }

        if (availableUnixDays.contains(selectedDay))
            return selectedDay

        availableUnixDays
            .filter { it < selectedDay }
            .maxOrNull()
            ?.let { maxLower ->
                return maxLower
            }

        availableUnixDays
            .filter { it > selectedDay }
            .minOrNull()
            ?.let { minHigher ->
                return minHigher
            }

        throw Exception("invalid day to move")
    }

    ///

    class ActivityFormAddUI(
        val activity: ActivityDb,
    ) {

        fun addInterval(
            unixTime: UnixTime,
            onSuccess: () -> Unit,
        ) {
            launchExIo {
                try {
                    val timestamp = unixTime.time

                    // todo ui limit
                    if (timestamp > time())
                        throw UIException("Invalid time")

                    // todo ui limit
                    if (IntervalDb.selectByIdOrNull(timestamp) != null)
                        throw UIException("Time is unavailable")

                    IntervalDb.insertWithValidation(
                        timer = activity.timer,
                        note = null,
                        activity = activity,
                        id = timestamp,
                    )
                    onSuccess()
                } catch (e: UIException) {
                    showUiAlert(e.uiMessage)
                }
            }
        }
    }

    class DayUi(
        val unixDay: Int,
        val intervalsDb: List<IntervalDb>,
        val nextIntervalTimeStart: Int,
    ) {

        val dayText: String = UnixTime.byLocalDay(unixDay).getStringByComponents(
            UnixTime.StringComponent.dayOfMonth,
            UnixTime.StringComponent.space,
            UnixTime.StringComponent.month,
            UnixTime.StringComponent.comma,
            UnixTime.StringComponent.space,
            UnixTime.StringComponent.dayOfWeek3,
        )
    }

    /**
     * A separate class with data to not store in memory all intervals.
     */
    data class IntervalUi(
        val interval: IntervalDb,
        val isStartsPrevDay: Boolean,
        val text: String,
        val secondsForBar: Int,
        val barTimeFinish: Int,
        val timeString: String,
        val periodString: String,
        val color: ColorRgba,
    ) {

        fun upTime(newTime: UnixTime): Unit = launchExDefault {
            try {
                val timestamp = newTime.time
                if (timestamp == interval.id)
                    return@launchExDefault

                // todo ui limit
                if (timestamp > time())
                    throw UIException("Invalid time")

                // todo ui limit
                if (IntervalDb.selectByIdOrNull(timestamp) != null)
                    throw UIException("Time is unavailable")

                interval.upId(timestamp)
            } catch (e: UIException) {
                showUiAlert(e.uiMessage)
            }
        }

        fun delete() {
            showUiConfirmation(
                UIConfirmationData(
                    text = "Are you sure you want to delete \"$text\"",
                    buttonText = "Delete",
                    isRed = true,
                ) {
                    try {
                        launchExDefault {
                            // todo UI
                            if (IntervalDb.selectAsc(limit = 2).size < 2)
                                throw UIException("Unable to delete the first item")
                            interval.delete()
                        }
                    } catch (e: UIException) {
                        showUiAlert(e.uiMessage)
                    }
                }
            )
        }

        companion object {

            fun build(
                interval: IntervalDb,
                dayUi: DayUi,
            ): IntervalUi {
                val unixTime = interval.unixTime()
                val activity = interval.selectActivityDbCached()

                val sectionDayTimeStart = UnixTime.byLocalDay(dayUi.unixDay).time
                val sectionDayTimeFinish = sectionDayTimeStart + 86400 - 1

                val finishTime: Int =
                    dayUi.intervalsDb.getNextOrNull(interval)?.id ?: dayUi.nextIntervalTimeStart
                val seconds: Int = finishTime - interval.id
                val barTimeFinish: Int = sectionDayTimeFinish.limitMax(finishTime)

                return IntervalUi(
                    interval = interval,
                    isStartsPrevDay = unixTime.localDay < dayUi.unixDay,
                    text = (interval.note ?: activity.name).textFeatures().textUi(),
                    secondsForBar = barTimeFinish - sectionDayTimeStart.limitMin(interval.id),
                    barTimeFinish = barTimeFinish,
                    timeString = unixTime.getStringByComponents(UnixTime.StringComponent.hhmm24),
                    periodString = prepPeriodString(seconds),
                    color = activity.colorRgba,
                )
            }
        }
    }
}

///

private fun prepPeriodString(
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

private fun prepDaysUi(
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
