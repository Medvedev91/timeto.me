package me.timeto.shared.vm.summary

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.UnixTime
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.launchEx
import me.timeto.shared.localUtcOffset
import me.timeto.shared.DayBarsUi
import me.timeto.shared.textFeatures
import me.timeto.shared.toHms
import me.timeto.shared.vm.Vm

class SummaryVm : Vm<SummaryVm.State>() {

    data class State(
        val pickerTimeStart: UnixTime,
        val pickerTimeFinish: UnixTime,
        val goalsUi: List<GoalUi>,
        val daysBarsUi: List<DayBarsUi>,
    ) {

        val minPickerTime: UnixTime = Cache.firstIntervalDb.unixTime()
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
                goalsUi = emptyList(),
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
                    goalsUi = prepGoalsUi(daysBarsUi),
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

    class GoalUi(
        val goalDb: Goal2Db,
        val seconds: Int,
        val ratio: Float,
        secondsPerDay: Int,
    ) {

        val title: String = goalDb.name.textFeatures().textUi()
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

private fun prepGoalsUi(
    daysBarsUi: List<DayBarsUi>
): List<SummaryVm.GoalUi> {
    val daysCount = daysBarsUi.size
    val totalSeconds = daysCount * 86_400
    val mapGoalSeconds: MutableMap<Int, Int> = mutableMapOf()
    daysBarsUi.forEach { dayBarsUi ->
        dayBarsUi.barsUi.forEach { sectionItem ->
            val goalDb = sectionItem.goalDb
            if (goalDb != null)
                mapGoalSeconds.incOrSet(goalDb.id, sectionItem.seconds)
        }
    }
    return mapGoalSeconds
        .map { (goalId, seconds) ->
            val goalDb: Goal2Db =
                Cache.goals2Db.first { it.id == goalId }
            SummaryVm.GoalUi(
                goalDb = goalDb,
                seconds = seconds,
                ratio = seconds.toFloat() / totalSeconds,
                secondsPerDay = seconds / daysCount,
            )
        }
        .sortedByDescending { it.seconds }
}

private fun <T> MutableMap<T, Int>.incOrSet(key: T, value: Int) {
    set(key, (get(key) ?: 0) + value)
}
