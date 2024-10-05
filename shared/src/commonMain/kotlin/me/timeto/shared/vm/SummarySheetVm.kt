package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.models.DayIntervalsUi

class SummarySheetVm : __Vm<SummarySheetVm.State>() {

    data class State(
        val pickerTimeStart: UnixTime,
        val pickerTimeFinish: UnixTime,
        val activitiesUI: List<ActivityUI>,
        val daysIntervalsUi: List<DayIntervalsUi>,
        val isChartVisible: Boolean,
    ) {

        val minPickerTime: UnixTime = Cache.firstInterval.unixTime()
        val maxPickerTime: UnixTime = UnixTime()

        val timeStartText: String = pickerTimeStart.getStringByComponents(buttonDateStringComponents)
        val timeFinishText: String = pickerTimeFinish.getStringByComponents(buttonDateStringComponents)

        val periodHints: List<PeriodHint> = run {
            val now = UnixTime()
            val yesterday = now.inDays(-1)
            listOf(
                PeriodHint(this, "Today", now, now),
                PeriodHint(this, "Yesterday", yesterday, yesterday),
                PeriodHint(this, "7 days", yesterday.inDays(-6), yesterday),
                PeriodHint(this, "30 days", yesterday.inDays(-29), yesterday),
            )
        }

        val barsTimeRows: List<String> =
            ((2..22) + 0).filter { (it % 2) == 0 }.map { "$it".padStart(2, '0') }
    }

    override val state: MutableStateFlow<State>

    init {
        val now = UnixTime()
        state = MutableStateFlow(
            State(
                pickerTimeStart = now,
                pickerTimeFinish = now,
                activitiesUI = listOf(),
                daysIntervalsUi = listOf(),
                isChartVisible = false,
            )
        )
    }

    override fun onAppear() {
        setPeriod(state.value.pickerTimeStart, state.value.pickerTimeFinish)
    }

    fun toggleIsChartVisible() = state.update {
        it.copy(isChartVisible = !it.isChartVisible)
    }

    fun setPeriod(
        pickerTimeStart: UnixTime,
        pickerTimeFinish: UnixTime,
    ) {
        scopeVm().launchEx {
            val daysIntervalsUi = DayIntervalsUi.buildList(
                dayStart = pickerTimeStart.localDay,
                dayFinish = pickerTimeFinish.localDay,
                utcOffset = localUtcOffset,
            )
            state.update {
                it.copy(
                    pickerTimeStart = pickerTimeStart,
                    pickerTimeFinish = pickerTimeFinish,
                    activitiesUI = prepActivitiesUI(daysIntervalsUi),
                    daysIntervalsUi = daysIntervalsUi.reversed(),
                )
            }
        }
    }

    fun setPickerTimeStart(unixTime: UnixTime) = setPeriod(
        pickerTimeStart = unixTime,
        pickerTimeFinish = state.value.pickerTimeFinish,
    )

    fun setPickerTimeFinish(unixTime: UnixTime) = setPeriod(
        pickerTimeStart = state.value.pickerTimeStart,
        pickerTimeFinish = unixTime,
    )

    ///

    class ActivityUI(
        val activity: ActivityDb,
        val seconds: Int,
        val ratio: Float,
        secondsPerDay: Int,
    ) {

        val title = activity.name.textFeatures().textUi()
        val percentageString = "${(ratio * 100).toInt()}%"
        val perDayString: String = prepTimeString(secondsPerDay) + " / day"
        val totalTimeString: String = prepTimeString(seconds)

        companion object {

            private fun prepTimeString(seconds: Int): String {
                val (h, m, _) = seconds.toHms(roundToNextMinute = true)
                val items = mutableListOf<String>()
                if (h > 0) items.add("${h}h")
                if (m > 0) items.add("${m}m")
                return items.joinToString(" ")
            }
        }
    }

    class PeriodHint(
        state: State,
        val title: String,
        val pickerTimeStart: UnixTime,
        val pickerTimeFinish: UnixTime,
    ) {
        val isActive: Boolean =
            state.pickerTimeStart.localDay == pickerTimeStart.localDay &&
            state.pickerTimeFinish.localDay == pickerTimeFinish.localDay
    }
}

private val buttonDateStringComponents = listOf(
    UnixTime.StringComponent.dayOfMonth,
    UnixTime.StringComponent.space,
    UnixTime.StringComponent.month3,
    UnixTime.StringComponent.comma,
    UnixTime.StringComponent.space,
    UnixTime.StringComponent.dayOfWeek3,
)

private fun prepActivitiesUI(
    daysIntervalsUi: List<DayIntervalsUi>
): List<SummarySheetVm.ActivityUI> {
    val daysCount = daysIntervalsUi.size
    val totalSeconds = daysCount * 86_400
    val mapActivitySeconds: MutableMap<Int, Int> = mutableMapOf()
    daysIntervalsUi.forEach { dayIntervalsUi ->
        dayIntervalsUi.intervalsUI.forEach { sectionItem ->
            val activity = sectionItem.activity
            if (activity != null)
                mapActivitySeconds.incOrSet(activity.id, sectionItem.seconds)
        }
    }
    return mapActivitySeconds
        .map { (activityId, seconds) ->
            val activityDb: ActivityDb =
                Cache.activitiesDbSorted.first { it.id == activityId }
            SummarySheetVm.ActivityUI(
                activity = activityDb,
                seconds = seconds,
                ratio = seconds.toFloat() / totalSeconds,
                secondsPerDay = seconds / daysCount,
            )
        }
        .sortedByDescending { it.seconds }
}
