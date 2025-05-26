package me.timeto.shared.ui.summary

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.UnixTime
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.incOrSet
import me.timeto.shared.launchEx
import me.timeto.shared.localUtcOffset
import me.timeto.shared.ui.DayBarsUi
import me.timeto.shared.textFeatures
import me.timeto.shared.toHms
import me.timeto.shared.vm.__Vm

class SummaryVm : __Vm<SummaryVm.State>() {

    data class State(
        val pickerTimeStart: UnixTime,
        val pickerTimeFinish: UnixTime,
        val activitiesUi: List<ActivityUi>,
        val daysBarsUi: List<DayBarsUi>,
    ) {

        val minPickerTime: UnixTime = Cache.firstInterval.unixTime()
        val maxPickerTime: UnixTime = UnixTime()

        val timeStartText: String = pickerTimeStart.getStringByComponents(buttonDateStringComponents)
        val timeFinishText: String = pickerTimeFinish.getStringByComponents(buttonDateStringComponents)

        val periodHints: List<PeriodHintUi> = run {
            val now = UnixTime()
            val yesterday = now.inDays(-1)
            listOf(
                PeriodHintUi(this, "Today", now, now),
                PeriodHintUi(this, "Yesterday", yesterday, yesterday),
                PeriodHintUi(this, "7 days", yesterday.inDays(-6), yesterday),
                PeriodHintUi(this, "30 days", yesterday.inDays(-29), yesterday),
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
                activitiesUi = emptyList(),
                daysBarsUi = emptyList(),
            )
        )
        setPeriod(
            pickerTimeStart = state.value.pickerTimeStart,
            pickerTimeFinish = state.value.pickerTimeFinish,
        )
    }

    ///

    fun setPeriod(
        pickerTimeStart: UnixTime,
        pickerTimeFinish: UnixTime,
    ) {
        scopeVm().launchEx {
            val daysBarsUi = DayBarsUi.buildList(
                dayStart = pickerTimeStart.localDay,
                dayFinish = pickerTimeFinish.localDay,
                utcOffset = localUtcOffset,
            )
            state.update {
                it.copy(
                    pickerTimeStart = pickerTimeStart,
                    pickerTimeFinish = pickerTimeFinish,
                    activitiesUi = prepActivitiesUi(daysBarsUi),
                    daysBarsUi = daysBarsUi.reversed(),
                )
            }
        }
    }

    fun setPickerTimeStart(unixTime: UnixTime) {
        setPeriod(
            pickerTimeStart = unixTime,
            pickerTimeFinish = state.value.pickerTimeFinish,
        )
    }

    fun setPickerTimeFinish(unixTime: UnixTime) {
        setPeriod(
            pickerTimeStart = state.value.pickerTimeStart,
            pickerTimeFinish = unixTime,
        )
    }

    ///

    class ActivityUi(
        val activity: ActivityDb,
        val seconds: Int,
        val ratio: Float,
        secondsPerDay: Int,
    ) {

        val title: String = activity.name.textFeatures().textUi()
        val percentageString: String = "${(ratio * 100).toInt()}%"
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

    class PeriodHintUi(
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

///

private val buttonDateStringComponents = listOf(
    UnixTime.StringComponent.dayOfMonth,
    UnixTime.StringComponent.space,
    UnixTime.StringComponent.month3,
    UnixTime.StringComponent.comma,
    UnixTime.StringComponent.space,
    UnixTime.StringComponent.dayOfWeek3,
)

private fun prepActivitiesUi(
    daysBarsUi: List<DayBarsUi>
): List<SummaryVm.ActivityUi> {
    val daysCount = daysBarsUi.size
    val totalSeconds = daysCount * 86_400
    val mapActivitySeconds: MutableMap<Int, Int> = mutableMapOf()
    daysBarsUi.forEach { dayBarsUi ->
        dayBarsUi.barsUi.forEach { sectionItem ->
            val activity = sectionItem.activityDb
            if (activity != null)
                mapActivitySeconds.incOrSet(activity.id, sectionItem.seconds)
        }
    }
    return mapActivitySeconds
        .map { (activityId, seconds) ->
            val activityDb: ActivityDb =
                Cache.activitiesDbSorted.first { it.id == activityId }
            SummaryVm.ActivityUi(
                activity = activityDb,
                seconds = seconds,
                ratio = seconds.toFloat() / totalSeconds,
                secondsPerDay = seconds / daysCount,
            )
        }
        .sortedByDescending { it.seconds }
}
