package me.timeto.shared.ui.repeatings.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.vm.__Vm

class RepeatingFormPeriodVm(
    initPeriod: RepeatingDb.Period?,
) : __Vm<RepeatingFormPeriodVm.State>() {

    data class State(
        val title: String,
        val activePeriodIdx: Int?,
        val selectedNDays: Int,
        val selectedWeekDays: Set<Int>,
        val selectedDaysOfMonth: Set<Int>,
        val selectedDaysOfYear: List<RepeatingDb.Period.DaysOfYear.MonthDayItem>,
    ) {

        val periodPickerItemsUi: List<PeriodPickerItemUi> = listOf(
            PeriodPickerItemUi(0, "Every Day"),
            PeriodPickerItemUi(1, "Every N Days"),
            PeriodPickerItemUi(2, "Days of the Week"),
            PeriodPickerItemUi(3, "Days of the Month"),
            PeriodPickerItemUi(4, "Days of the Year"),
        )
    }

    override val state: MutableStateFlow<State>

    init {

        val activePeriodIdx: Int? = when (initPeriod) {
            is RepeatingDb.Period.EveryNDays -> if (initPeriod.nDays == 1) 0 else 1
            is RepeatingDb.Period.DaysOfWeek -> 2
            is RepeatingDb.Period.DaysOfMonth -> 3
            is RepeatingDb.Period.DaysOfYear -> 4
            null -> null
        }

        // For UI "Every Day" and "Every N Days" is a different, but the logic is same EveryNDays.
        // That's why if the EveryNDays == 1, the "Every Day" would be checked, and for
        // Every N Days it's needed to set 2 as a default value instead of 1.
        val selectedNDays: Int = run {
            val period = (initPeriod as? RepeatingDb.Period.EveryNDays) ?: return@run 2
            if (period.nDays == 1) 2 else period.nDays
        }

        val selectedWeekDays: Set<Int> = run {
            (initPeriod as? RepeatingDb.Period.DaysOfWeek)?.weekDays?.toSet() ?: setOf()
        }

        val selectedDaysOfMonth: Set<Int> = run {
            (initPeriod as? RepeatingDb.Period.DaysOfMonth)?.days ?: setOf()
        }

        val selectedDaysOfYear: List<RepeatingDb.Period.DaysOfYear.MonthDayItem> = run {
            (initPeriod as? RepeatingDb.Period.DaysOfYear)?.items ?: listOf()
        }

        state = MutableStateFlow(
            State(
                title = "Period",
                activePeriodIdx = activePeriodIdx,
                selectedNDays = selectedNDays,
                selectedWeekDays = selectedWeekDays,
                selectedDaysOfMonth = selectedDaysOfMonth,
                selectedDaysOfYear = selectedDaysOfYear,
            )
        )
    }

    fun setActivePeriodIdx(newIdx: Int?) {
        state.update { it.copy(activePeriodIdx = newIdx) }
    }

    fun setSelectedNDays(newNDays: Int) {
        state.update { it.copy(selectedNDays = newNDays) }
    }

    ///

    data class PeriodPickerItemUi(
        val idx: Int,
        val title: String,
    )
}
