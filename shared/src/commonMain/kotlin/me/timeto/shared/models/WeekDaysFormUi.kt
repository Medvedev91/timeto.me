package me.timeto.shared.models

import me.timeto.shared.UnixTime

class WeekDaysFormUi(
    weekDays: List<Int>,
) {

    val weekDaysUi: List<WeekDayUI> = UnixTime.dayOfWeekNames1.mapIndexed { idx, title ->
        WeekDayUI(idx = idx, title = title, isSelected = idx in weekDays)
    }

    fun toggleWeekDay(idx: Int): List<Int> {
        val newWeekDays = weekDaysUi.toMutableList()
        val weekDay = newWeekDays[idx]
        newWeekDays[idx] = weekDay.copy(isSelected = !weekDay.isSelected)
        return newWeekDays.filter { it.isSelected }.map { it.idx }
    }

    ///

    data class WeekDayUI(
        val idx: Int,
        val title: String,
        val isSelected: Boolean,
    )
}
