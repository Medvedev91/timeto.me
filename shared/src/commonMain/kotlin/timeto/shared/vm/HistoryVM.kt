package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.*
import timeto.shared.db.ActivityModel
import timeto.shared.db.IntervalModel

class HistoryVM : __VM<HistoryVM.State>() {

    data class State(
        val sections: List<HistorySection>,
        val activitiesFormAddUI: List<ActivityFormAddUI>,
    ) {
        val minPickerDay = DI.firstInterval.unixTime().localDay
    }

    override val state = MutableStateFlow(
        State(
            sections = listOf(),
            activitiesFormAddUI = listOf(),
        )
    )

    override fun onAppear() {
        IntervalModel.getAscFlow().onEachExIn(scopeVM()) { intervalsAsc ->
            state.update {
                it.copy(
                    sections = prepHistorySections(allIntervalsAsc = intervalsAsc),
                    activitiesFormAddUI = ActivityModel.getAscSorted().map { ActivityFormAddUI(it) },
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
        val activity: ActivityModel,
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
                    if (IntervalModel.getByIdOrNull(timestamp) != null)
                        throw UIException("Time is unavailable")

                    IntervalModel.addWithValidation(
                        deadline = activity.deadline,
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
        val intervals: List<IntervalModel>,
        val nextIntervalStart: Int,
    ) {

        val dayText = UnixTime.byLocalDay(day).getStringByComponents(listOf(
            UnixTime.StringComponent.dayOfMonth,
            UnixTime.StringComponent.space,
            UnixTime.StringComponent.month,
            UnixTime.StringComponent.comma,
            UnixTime.StringComponent.space,
            UnixTime.StringComponent.dayOfWeek3,
        ))
    }

    /**
     * A separate class with data to not store in memory all intervals.
     */
    data class IntervalUI(
        val interval: IntervalModel,
        val isStartsPrevDay: Boolean,
        val activityText: String,
        val noteText: String?,
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
                if (IntervalModel.getByIdOrNull(timestamp) != null)
                    throw UIException("Time is unavailable")

                interval.upId(timestamp)
            } catch (e: UIException) {
                showUiAlert(e.uiMessage)
            }
        }

        fun delete() {
            showUiConfirmation(
                UIConfirmationData(
                    text = "Are you sure you want to delete \"$activityText\"",
                    buttonText = "Delete",
                    isRed = true,
                ) {
                    try {
                        launchExDefault {
                            // todo UI
                            if (IntervalModel.getAsc(limit = 2).size < 2)
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
                interval: IntervalModel,
                section: HistorySection,
            ): IntervalUI {
                val unixTime = interval.unixTime()
                val activity = interval.getActivityDI()

                val sectionDayTimeStart = UnixTime.byLocalDay(section.day).time
                val sectionDayTimeFinish = sectionDayTimeStart + 86400 - 1

                val finishTime = section.intervals.getNextOrNull(interval)?.id ?: section.nextIntervalStart
                val seconds = finishTime - interval.id
                val barTimeFinish = sectionDayTimeFinish.min(finishTime)

                return IntervalUI(
                    interval = interval,
                    isStartsPrevDay = unixTime.localDay < section.day,
                    activityText = TextFeatures.parse(activity.nameWithEmoji()).textUi,
                    noteText = interval.note?.let { TextFeatures.parse(it).textUi },
                    secondsForBar = barTimeFinish - sectionDayTimeStart.max(interval.id),
                    barTimeFinish = barTimeFinish,
                    timeString = unixTime.getStringByComponents(listOf(UnixTime.StringComponent.hhmm24)),
                    periodString = prepPeriodString(seconds),
                    color = activity.getColorRgba(),
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
    allIntervalsAsc: List<IntervalModel>,
): List<HistoryVM.HistorySection> {
    val sections = mutableListOf<HistoryVM.HistorySection>()

    // "last" I mean the last while iteration
    var lastDay = UnixTime(allIntervalsAsc.first().id).localDay
    var lastList = mutableListOf<IntervalModel>()

    allIntervalsAsc.forEach { interval ->
        val intervalTime = interval.unixTime()
        if (lastDay == intervalTime.localDay) {
            lastList.add(interval)
        } else {
            sections.add(
                HistoryVM.HistorySection(
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
            HistoryVM.HistorySection(
                day = lastDay,
                intervals = lastList,
                nextIntervalStart = time()
            )
        )
    }

    return sections
}
