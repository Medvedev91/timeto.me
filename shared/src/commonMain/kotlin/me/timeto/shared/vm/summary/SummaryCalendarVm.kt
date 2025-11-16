package me.timeto.shared.vm.summary

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.UnixTime
import me.timeto.shared.vm.Vm

class SummaryCalendarVm(
    selectedStartTime: UnixTime,
    selectedFinishTime: UnixTime,
) : Vm<SummaryCalendarVm.State>() {

    private var customSelectedStartDate: UnixTime? = null

    data class State(
        val weeksUi: List<WeekUi>,
        val selectedDays: Set<Int>,
    )

    override val state = MutableStateFlow(
        State(
            weeksUi = buildCalendar(),
            selectedDays = (selectedStartTime.localDay..selectedFinishTime.localDay).toSet(),
        )
    )

    fun selectDate(
        unixTime: UnixTime,
        onSelectionComplete: (UnixTime, UnixTime) -> Unit,
    ) {
        val customSelectedStartDate = customSelectedStartDate
        if (customSelectedStartDate != null) {
            if (customSelectedStartDate.time <= unixTime.time)
                onSelectionComplete(customSelectedStartDate, unixTime)
            else
                onSelectionComplete(unixTime, customSelectedStartDate)
            return
        }
        this.customSelectedStartDate = unixTime
        state.update { state ->
            state.copy(selectedDays = setOf(unixTime.localDay))
        }
    }

    ///

    data class DayUi(
        val timeStart: UnixTime,
        val isFirstDay: Boolean,
    ) {
        val title: String
        val subtitle: String?
        val unixDay: Int = timeStart.localDay

        init {
            val day: Int = timeStart.dayOfMonth()
            title = day.toString()
            subtitle = if (isFirstDay || day == 1) {
                val year: Int = timeStart.year()
                val yearNow: Int = UnixTime().year()
                val yearString: String = if (yearNow != year) " ${year.toString().takeLast(2)}" else ""
                timeStart.getStringByComponents(UnixTime.StringComponent.month3) + yearString
            } else null
        }
    }

    data class WeekUi(
        val daysUi: List<DayUi?>,
    ) {
        val id: Int = daysUi.first { it != null }!!.timeStart.time
    }
}

private fun buildCalendar(): List<SummaryCalendarVm.WeekUi> {
    val resList = mutableListOf<SummaryCalendarVm.WeekUi>()
    val timeStart = UnixTime(Cache.firstIntervalDb.id)
    val timeFinish = UnixTime()
    val curWeekDaysUi: MutableList<SummaryCalendarVm.DayUi?> =
        (0 until timeStart.dayOfWeek()).map { null }.toMutableList()
    (timeStart.localDay..timeFinish.localDay).forEach { day ->
        val unixDay = UnixTime.byLocalDay(day)
        curWeekDaysUi.add(
            SummaryCalendarVm.DayUi(
                timeStart = unixDay,
                isFirstDay = (day == timeStart.localDay)
            )
        )
        // Sunday
        if (unixDay.dayOfWeek() == 6) {
            resList.add(SummaryCalendarVm.WeekUi(curWeekDaysUi.toList()))
            curWeekDaysUi.clear()
            return@forEach
        }
        // End of Calendar
        if (timeFinish.localDay == day) {
            curWeekDaysUi.addAll(((timeFinish.dayOfWeek() + 1)..6).map { null })
            resList.add(SummaryCalendarVm.WeekUi(curWeekDaysUi.toList()))
            return@forEach
        }
    }
    return resList
}
