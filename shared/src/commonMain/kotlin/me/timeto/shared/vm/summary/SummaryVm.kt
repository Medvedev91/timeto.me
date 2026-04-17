package me.timeto.shared.vm.summary

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.UnixTime
import me.timeto.shared.launchEx
import me.timeto.shared.localUtcOffset
import me.timeto.shared.DayBarsUi
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.textFeatures
import me.timeto.shared.toHms
import me.timeto.shared.vm.Vm

class SummaryVm : Vm<SummaryVm.State>() {

    data class State(
        val pickerTimeStart: UnixTime,
        val pickerTimeFinish: UnixTime,
        val activitiesUi: List<ActivityUi>,
        val daysBarsUi: List<DayBarsUi>,
    ) {

        val dateTitle: String = run {
            // Single Day
            if (pickerTimeStart.localDay == pickerTimeFinish.localDay)
                return@run pickerTimeStart.getStringByComponents(
                    UnixTime.StringComponent.dayOfMonth,
                    UnixTime.StringComponent.space,
                    UnixTime.StringComponent.month3,
                )
            // Inside Month
            val startMonth: Int = pickerTimeStart.month()
            val finishMonth: Int = pickerTimeFinish.month()
            if (startMonth == finishMonth) {
                return@run listOf(
                    pickerTimeStart.dayOfMonth().toString() + "-",
                    pickerTimeFinish.dayOfMonth().toString() + " ",
                    UnixTime.monthNames3[startMonth],
                ).joinToString("")
            }
            // Different Months
            pickerTimeStart.getStringByComponents(
                UnixTime.StringComponent.dayOfMonth,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.month3,
            ) + " - " + pickerTimeFinish.getStringByComponents(
                UnixTime.StringComponent.dayOfMonth,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.month3,
            )
        }

        val minPickerTime: UnixTime = Cache.firstIntervalDb.unixTime()
        val maxPickerTime: UnixTime = UnixTime()

        val periodHints: List<PeriodHintUi> = run {
            val now = UnixTime()
            val yesterday = now.inDays(-1)
            listOf(
                PeriodHintUi(this, "Today", now, now),
                PeriodHintUi(this, "Yesterday", yesterday, yesterday),
                PeriodHintUi(this, "7d", yesterday.inDays(-6), yesterday),
                PeriodHintUi(this, "30d", yesterday.inDays(-29), yesterday),
            )
        }

        val isCustomPeriodSelected: Boolean = !periodHints.any {
            it.pickerTimeStart.localDay == pickerTimeStart.localDay &&
                    it.pickerTimeFinish.localDay == pickerTimeFinish.localDay
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

    fun setPeriodToday() {
        val nowUnixTime = UnixTime()
        setPeriod(nowUnixTime, nowUnixTime)
    }

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
                    activitiesUi = prepGoalsUi(daysBarsUi),
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
        val activityDb: ActivityDb,
        val seconds: Int,
        val ratio: Float,
        val children: MutableList<ActivityUi>,
        secondsPerDay: Int,
    ) {

        val title: String = activityDb.name.textFeatures().textUi()
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

private fun prepGoalsUi(
    daysBarsUi: List<DayBarsUi>
): List<SummaryVm.ActivityUi> {
    val daysCount = daysBarsUi.size
    val totalSeconds = daysCount * 86_400
    val mapActivitySeconds: MutableMap<Int, Int> = mutableMapOf()
    daysBarsUi.forEach { dayBarsUi ->
        dayBarsUi.barsUi.forEach { sectionItem ->
            val activityId = sectionItem.intervalDb?.activityId
            if (activityId != null)
                mapActivitySeconds.incOrSet(activityId, sectionItem.seconds)
        }
    }

    val recursiveMapGoalSeconds: MutableMap<Int, Int> = mutableMapOf()
    ActivityDb.selectParentRecursiveMapCached().forEach { (activityId, childrenActivitiesDb) ->
        val totalSeconds: Int =
            (mapActivitySeconds[activityId] ?: 0) + childrenActivitiesDb.sumOf { mapActivitySeconds[it.id] ?: 0 }
        if (totalSeconds > 0)
            recursiveMapGoalSeconds[activityId] = totalSeconds
    }

    val allActivitiesUi: List<SummaryVm.ActivityUi> =
        recursiveMapGoalSeconds
            .map { (activityId, seconds) ->
                val activityDb: ActivityDb =
                    Cache.activitiesDb.first { it.id == activityId }
                SummaryVm.ActivityUi(
                    activityDb = activityDb,
                    seconds = seconds,
                    ratio = seconds.toFloat() / totalSeconds,
                    children = mutableListOf(),
                    secondsPerDay = seconds / daysCount,
                )
            }
            .sortedByDescending { it.seconds }

    allActivitiesUi.forEach { goalUi ->
        val parentGoalId: Int = goalUi.activityDb.parent_id ?: return@forEach
        val parentGoalUi: SummaryVm.ActivityUi = allActivitiesUi.first { it.activityDb.id == parentGoalId }
        parentGoalUi.children.add(goalUi)
    }

    return allActivitiesUi.filter { it.activityDb.parent_id == null }
}

private fun <T> MutableMap<T, Int>.incOrSet(key: T, value: Int) {
    set(key, (get(key) ?: 0) + value)
}
