package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb

class HistoryVm : __Vm<HistoryVm.State>() {

    data class State(
        val sections: List<HistorySection>,
        val activitiesFormAddUI: List<ActivityFormAddUI>,
    ) {
        val minPickerDay = Cache.firstInterval.unixTime().localDay
    }

    override val state = MutableStateFlow(
        State(
            sections = listOf(),
            activitiesFormAddUI = listOf(),
        )
    )

    override fun onAppear() {
        IntervalDb.getAscFlow().onEachExIn(scopeVm()) { intervalsAsc ->
            state.update {
                it.copy(
                    sections = prepHistorySections(allIntervalsAsc = intervalsAsc),
                    activitiesFormAddUI = ActivityDb.getAscSorted().map { ActivityFormAddUI(it) },
                )
            }
        }
    }

    fun calcDayToMove(selectedDay: Int): Int {
        val availableDays = state.value.sections.map { it.day }

        if (availableDays.contains(selectedDay))
            return selectedDay

        availableDays
            .filter { it < selectedDay }
            .maxOrNull()
            ?.let { maxLower ->
                return maxLower
            }

        availableDays
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
            launchExDefault {
                try {
                    val timestamp = unixTime.time

                    // todo ui limit
                    if (timestamp > time())
                        throw UIException("Invalid time")

                    // todo ui limit
                    if (IntervalDb.getByIdOrNull(timestamp) != null)
                        throw UIException("Time is unavailable")

                    IntervalDb.addWithValidation(
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

    class HistorySection(
        val day: Int,
        val intervals: List<IntervalDb>,
        val nextIntervalStart: Int,
    ) {

        val dayText = UnixTime.byLocalDay(day).getStringByComponents(
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
    data class IntervalUI(
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
                if (IntervalDb.getByIdOrNull(timestamp) != null)
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
                            if (IntervalDb.getAsc(limit = 2).size < 2)
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
                section: HistorySection,
            ): IntervalUI {
                val unixTime = interval.unixTime()
                val activity = interval.getActivityDbCached()

                val sectionDayTimeStart = UnixTime.byLocalDay(section.day).time
                val sectionDayTimeFinish = sectionDayTimeStart + 86400 - 1

                val finishTime = section.intervals.getNextOrNull(interval)?.id ?: section.nextIntervalStart
                val seconds = finishTime - interval.id
                val barTimeFinish = sectionDayTimeFinish.limitMax(finishTime)

                return IntervalUI(
                    interval = interval,
                    isStartsPrevDay = unixTime.localDay < section.day,
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

private fun prepPeriodString(
    seconds: Int,
): String {
    val hms = seconds.toHms()
    return when {
        hms[0] == 0 && hms[1] == 0 -> "${hms[2]} sec"
        hms[0] == 0 -> "${hms[1]} min"
        else -> "${hms[0]}:${if (hms[1] < 10) "0${hms[1]}" else "${hms[1]}"}"
    }
}

private fun prepHistorySections(
    allIntervalsAsc: List<IntervalDb>,
): List<HistoryVm.HistorySection> {
    val sections = mutableListOf<HistoryVm.HistorySection>()

    // "last" I mean the last while iteration
    var lastDay = UnixTime(allIntervalsAsc.first().id).localDay
    var lastList = mutableListOf<IntervalDb>()

    allIntervalsAsc.forEach { interval ->
        val intervalTime = interval.unixTime()
        if (lastDay == intervalTime.localDay) {
            lastList.add(interval)
        } else {
            sections.add(
                HistoryVm.HistorySection(
                    day = lastDay,
                    intervals = lastList,
                    nextIntervalStart = interval.id
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
        sections.add(
            HistoryVm.HistorySection(
                day = lastDay,
                intervals = lastList,
                nextIntervalStart = time()
            )
        )
    }

    return sections
}
