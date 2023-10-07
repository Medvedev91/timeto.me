package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.IntervalModel
import kotlin.math.abs

class ChartVM : __VM<ChartVM.State>() {

    data class State(
        val dayStart: Int,
        val dayFinish: Int,
        val selectedId: String?,
        val pieItems: List<PieChart.ItemData>,
        val minPickerDay: Int,
        val maxPickerDay: Int,
    )

    override val state: MutableStateFlow<State>

    init {
        val today = UnixTime().localDay
        val initDay = DI.firstInterval.unixTime().localDay
        state = MutableStateFlow(
            State(
                dayStart = today - 6,
                dayFinish = today,
                selectedId = null,
                pieItems = listOf(),
                minPickerDay = initDay,
                maxPickerDay = today,
            )
        )
    }

    override fun onAppear() {
        upPeriod(state.value.dayStart, state.value.dayFinish)
    }

    fun selectId(id: String?) {
        state.update { it.copy(selectedId = id) }
    }

    fun upPeriod(
        dayStart: Int,
        dayFinish: Int,
    ) {
        scopeVM().launchEx {
            val items = prepPieItems(dayStart, dayFinish)
            state.update {
                it.copy(
                    dayStart = dayStart,
                    dayFinish = dayFinish,
                    pieItems = items,
                )
            }
        }
    }

    fun upDayStart(day: Int) = upPeriod(
        dayStart = day,
        dayFinish = state.value.dayFinish,
    )

    fun upDayFinish(day: Int) = upPeriod(
        dayStart = state.value.dayStart,
        dayFinish = day,
    )
}

private suspend fun prepPieItems(
    formDayStart: Int,
    formDayFinish: Int,
): List<PieChart.ItemData> {

    /**
     * The period that is selected can be wider than the history with
     * the data, especially relevant on the first day of the launch,
     * when the data only for the day but the period of 7 days.
     */
    val realTimeStart = UnixTime.byLocalDay(formDayStart).time.limitMin(DI.firstInterval.id)
    val realTimeFinish = (UnixTime.byLocalDay(formDayFinish).inDays(1).time - 1).limitMax(time())

    ///

    val mapActivityIdSeconds = mutableMapOf<Int, Int>()
    val intervalsAsc = IntervalModel.getBetweenIdDesc(realTimeStart, realTimeFinish).reversed()
    intervalsAsc.forEachIndexed { index, interval ->
        val iSeconds = if (intervalsAsc.last() == interval)
            realTimeFinish - interval.id
        else
            intervalsAsc[index + 1].id - interval.id
        mapActivityIdSeconds.incOrSet(interval.activity_id, iSeconds)
    }
    val prevInterval = IntervalModel.getBetweenIdDesc(0, realTimeStart - 1, 1).firstOrNull()
    if (prevInterval != null) {
        val iSeconds = intervalsAsc.firstOrNull()?.id ?: realTimeFinish
        mapActivityIdSeconds.incOrSet(prevInterval.activity_id, iSeconds - realTimeStart)
    }

    ///

    val realTotalSeconds = abs(realTimeFinish - realTimeStart)

    val realDayStart = UnixTime(realTimeStart).localDay
    val realDayFinish = UnixTime(realTimeFinish).localDay

    return mapActivityIdSeconds
        .toList()
        .sortedByDescending { it.second }
        .map { (activityId, seconds) ->
            val activity = DI.activitiesSorted.first { it.id == activityId }
            val ratio = seconds.toFloat() / realTotalSeconds
            val tableNote = secondsToString(seconds / (realDayFinish - realDayStart + 1)) + if (realDayStart == realDayFinish) "" else " / day"

            PieChart.ItemData(
                id = "${activity.id}",
                value = seconds.toDouble(),
                color = activity.colorRgba,
                title = activity.nameWithEmoji().textFeatures().textUi(),
                shortTitle = activity.emoji,
                subtitleTop = "${(ratio * 100).toInt()}%",
                subtitleBottom = secondsToString(seconds),
                customData = tableNote,
            )
        }
}

private fun secondsToString(seconds: Int): String {
    val aTime = mutableListOf<String>()
    val hms = seconds.toHms()
    if (hms[0] > 0)
        aTime.add("${hms[0]}h")
    if (hms[1] > 0)
        aTime.add("${hms[1]}m")
    if (aTime.isEmpty())
        aTime.add("${hms[2]}s")
    return aTime.joinToString(" ")
}
