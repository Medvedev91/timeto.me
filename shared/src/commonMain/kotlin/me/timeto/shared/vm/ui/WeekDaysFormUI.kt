package me.timeto.shared.vm.ui

import me.timeto.shared.UnixTime

class WeekDaysFormUI(
    weekDays: List<Int>,
) {

    val weekDaysUI = UnixTime.dayOfWeekNames1.mapIndexed { idx, title ->
        WeekDayUI(idx = idx, title = title, isSelected = idx in weekDays)
    }

    fun toggleWeekDay(idx: Int): List<Int> {
        val newWeekDays = weekDaysUI.toMutableList()
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
