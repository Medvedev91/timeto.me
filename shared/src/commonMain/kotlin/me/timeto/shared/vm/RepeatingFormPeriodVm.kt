package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.UIException
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.showUiAlert

class RepeatingFormPeriodVm(
    defaultPeriod: RepeatingDb.Period?,
) : __VM<RepeatingFormPeriodVm.State>() {

    data class State(
        val activePeriodIndex: Int?,
        val selectedNDays: Int,
        val selectedWeekDays: List<Int>,
        val selectedDaysOfMonth: Set<Int>,
        val selectedDaysOfYear: List<RepeatingDb.Period.DaysOfYear.MonthDayItem>,
    ) {

        // TRICK The order is hardcoded in the UI
        val periods = listOf(
            "Every Day",
            "Every N Days",
            "Days of the Week",
            "Days of the Month",
            "Days of the Year",
        )
    }

    override val state: MutableStateFlow<State>

    init {
        val activePeriodIndex: Int? = when (defaultPeriod) {
            null -> null
            is RepeatingDb.Period.EveryNDays -> if (defaultPeriod.nDays == 1) 0 else 1
            is RepeatingDb.Period.DaysOfWeek -> 2
            is RepeatingDb.Period.DaysOfMonth -> 3
            is RepeatingDb.Period.DaysOfYear -> 4
        }

        // For UI "Every Day" and "Every N Days" is a different, but the logic is same EveryNDays.
        // That's by if the EveryNDays == 1, the "Every Day" would be checked, and for
        // Every N Days it's needed to set 2 as a default value instead of 1.
        val selectedNDays: Int = run {
            val period = (defaultPeriod as? RepeatingDb.Period.EveryNDays) ?: return@run 2
            if (period.nDays == 1) 2 else period.nDays
        }

        val selectedWeekDays: List<Int> = run {
            (defaultPeriod as? RepeatingDb.Period.DaysOfWeek)?.weekDays ?: listOf()
        }

        val selectedDaysOfMonth: Set<Int> = run {
            (defaultPeriod as? RepeatingDb.Period.DaysOfMonth)?.days ?: setOf()
        }

        val selectedDaysOfYear: List<RepeatingDb.Period.DaysOfYear.MonthDayItem> = run {
            (defaultPeriod as? RepeatingDb.Period.DaysOfYear)?.items ?: listOf()
        }

        state = MutableStateFlow(
            State(
                activePeriodIndex = activePeriodIndex,
                selectedNDays = selectedNDays,
                selectedWeekDays = selectedWeekDays,
                selectedDaysOfMonth = selectedDaysOfMonth,
                selectedDaysOfYear = selectedDaysOfYear,
            )
        )
    }

    fun buildSelectedPeriod(
        onSuccess: (RepeatingDb.Period?) -> Unit,
    ) {
        try {
            val periodIndex = state.value.activePeriodIndex
            val period: RepeatingDb.Period? = when (periodIndex) {
                0 -> RepeatingDb.Period.EveryNDays(1)
                1 -> RepeatingDb.Period.EveryNDays(state.value.selectedNDays)
                2 -> RepeatingDb.Period.DaysOfWeek(state.value.selectedWeekDays)
                3 -> RepeatingDb.Period.DaysOfMonth(state.value.selectedDaysOfMonth)
                4 -> RepeatingDb.Period.DaysOfYear(state.value.selectedDaysOfYear)
                null -> null
                else -> throw Exception()
            }
            onSuccess(period)
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }

    fun setActivePeriodIndex(index: Int?) {
        state.update { it.copy(activePeriodIndex = index) }
    }

    fun setSelectedNDays(nDays: Int) {
        state.update { it.copy(selectedNDays = nDays) }
    }

    fun upWeekDays(newWeekDays: List<Int>) {
        state.update { it.copy(selectedWeekDays = newWeekDays) }
    }

    fun toggleDayOfMonth(day: Int) {
        val isDaySelected = day in state.value.selectedDaysOfMonth
        state.update {
            it.copy(
                selectedDaysOfMonth = it.selectedDaysOfMonth
                    .toMutableSet()
                    .apply {
                        if (isDaySelected) remove(day)
                        else add(day)
                    }
            )
        }
    }

    fun addDayOfTheYear(item: RepeatingDb.Period.DaysOfYear.MonthDayItem) {
        state.update {
            it.copy(
                selectedDaysOfYear = it.selectedDaysOfYear
                    .toMutableList()
                    .apply { add(item) }
                    .sortedWith(compareBy({ it.monthId }, { it.dayId }))
            )
        }
    }

    fun delDayOfTheYear(item: RepeatingDb.Period.DaysOfYear.MonthDayItem) {
        state.update {
            it.copy(
                selectedDaysOfYear = it.selectedDaysOfYear
                    .toMutableList()
                    .apply { remove(item) }
            )
        }
    }
}
