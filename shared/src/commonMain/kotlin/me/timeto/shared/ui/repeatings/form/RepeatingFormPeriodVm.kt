package me.timeto.shared.ui.repeatings.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.UnixTime
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.ui.DialogsManager
import me.timeto.shared.UiException
import me.timeto.shared.vm.__Vm

class RepeatingFormPeriodVm(
    initPeriod: RepeatingDb.Period?,
) : __Vm<RepeatingFormPeriodVm.State>() {

    data class State(
        val title: String,
        val activePeriodIdx: Int,
        val selectedNDays: Int,
        val selectedDaysOfWeek: Set<Int>,
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

        val daysOfWeekUi: List<DayOfWeekUi> =
            UnixTime.dayOfWeekNames.mapIndexed { idx, name ->
                DayOfWeekUi(idx = idx, title = name)
            }

        val periodNote: String =
            periodPickerItemsUi[activePeriodIdx].title
    }

    override val state: MutableStateFlow<State>

    init {

        val activePeriodIdx: Int = when (initPeriod) {
            is RepeatingDb.Period.EveryNDays -> if (initPeriod.nDays == 1) 0 else 1
            is RepeatingDb.Period.DaysOfWeek -> 2
            is RepeatingDb.Period.DaysOfMonth -> 3
            is RepeatingDb.Period.DaysOfYear -> 4
            null -> 0
        }

        // For UI "Every Day" and "Every N Days" is a different, but the logic is same EveryNDays.
        // That's why if the EveryNDays == 1, the "Every Day" would be checked, and for
        // Every N Days it's needed to set 2 as a default value instead of 1.
        val selectedNDays: Int = run {
            val period = (initPeriod as? RepeatingDb.Period.EveryNDays) ?: return@run 2
            if (period.nDays == 1) 2 else period.nDays
        }

        val selectedDaysOfWeek: Set<Int> = run {
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
                selectedDaysOfWeek = selectedDaysOfWeek,
                selectedDaysOfMonth = selectedDaysOfMonth,
                selectedDaysOfYear = selectedDaysOfYear,
            )
        )
    }

    fun setActivePeriodIdx(newIdx: Int) {
        state.update { it.copy(activePeriodIdx = newIdx) }
    }

    fun setSelectedNDays(newNDays: Int) {
        state.update { it.copy(selectedNDays = newNDays) }
    }

    fun toggleDayOfWeek(dayOfWeekIdx: Int) {
        state.update { state ->
            val newSet = state.selectedDaysOfWeek.toMutableSet()
            if (newSet.contains(dayOfWeekIdx))
                newSet.remove(dayOfWeekIdx)
            else
                newSet.add(dayOfWeekIdx)
            state.copy(selectedDaysOfWeek = newSet)
        }
    }

    fun toggleDayOfMonth(dayOfMonth: Int) {
        state.update { state ->
            val newSet = state.selectedDaysOfMonth.toMutableSet()
            if (newSet.contains(dayOfMonth))
                newSet.remove(dayOfMonth)
            else
                newSet.add(dayOfMonth)
            state.copy(selectedDaysOfMonth = newSet)
        }
    }

    fun addDayOfTheYear(item: RepeatingDb.Period.DaysOfYear.MonthDayItem) {
        state.update {
            it.copy(
                selectedDaysOfYear = it.selectedDaysOfYear
                    .toMutableList()
                    .apply { add(item) }
                    .sortDaysOfTheYear()
            )
        }
    }

    fun deleteDayOfTheYear(idx: Int) {
        state.update {
            it.copy(
                selectedDaysOfYear = it.selectedDaysOfYear
                    .toMutableList()
                    .apply { removeAt(idx) }
                    .sortDaysOfTheYear()
            )
        }
    }

    fun buildSelectedPeriod(
        dialogsManager: DialogsManager,
        onSuccess: (RepeatingDb.Period) -> Unit,
    ) {
        try {
            val periodIndex: Int = state.value.activePeriodIdx
            val period: RepeatingDb.Period = when (periodIndex) {
                0 -> RepeatingDb.Period.EveryNDays(1)
                1 -> RepeatingDb.Period.EveryNDays(state.value.selectedNDays)
                2 -> RepeatingDb.Period.DaysOfWeek(state.value.selectedDaysOfWeek)
                3 -> RepeatingDb.Period.DaysOfMonth(state.value.selectedDaysOfMonth)
                4 -> RepeatingDb.Period.DaysOfYear(state.value.selectedDaysOfYear)
                else -> throw Exception()
            }
            onSuccess(period)
        } catch (e: UiException) {
            dialogsManager.alert(e.uiMessage)
        }
    }

    ///

    data class PeriodPickerItemUi(
        val idx: Int,
        val title: String,
    )

    data class DayOfWeekUi(
        val idx: Int,
        val title: String,
    )
}

private fun List<RepeatingDb.Period.DaysOfYear.MonthDayItem>.sortDaysOfTheYear(
): List<RepeatingDb.Period.DaysOfYear.MonthDayItem> {
    return sortedWith(compareBy({ it.monthId }, { it.dayId }))
}
