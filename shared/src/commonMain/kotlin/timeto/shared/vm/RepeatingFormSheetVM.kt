package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.*
import timeto.shared.db.RepeatingModel

class RepeatingFormSheetVM(
    val repeating: RepeatingModel?
) : __VM<RepeatingFormSheetVM.State>() {

    data class State(
        val headerTitle: String,
        val headerDoneText: String,
        val inputTextValue: String,
        val triggers: List<Trigger>,
        val activePeriodIndex: Int?,
        val selectedNDays: Int,
        val selectedWeekDays: List<Boolean>,
        val selectedDaysOfMonth: Set<Int>,
        val selectedDaysOfYear: List<RepeatingModel.Period.DaysOfYear.MonthDayItem>,
    ) {
        val isHeaderDoneEnabled = (inputTextValue.isNotBlank() && activePeriodIndex != null)

        // WARNING The order is hardcoded in UI
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
        val textFeatures = TextFeatures.parse(repeating?.text ?: "")
        val activePeriodIndex = if (repeating == null) null else {
            when (val period = repeating.getPeriod()) {
                is RepeatingModel.Period.EveryNDays -> if (period.nDays == 1) 0 else 1
                is RepeatingModel.Period.DaysOfWeek -> 2
                is RepeatingModel.Period.DaysOfMonth -> 3
                is RepeatingModel.Period.DaysOfYear -> 4
            }
        }

        // For UI "Every Day" and "Every N Days" is a different, but the logic is same EveryNDays.
        // That's by if the EveryNDays == 1, the "Every Day" would be checked, and for
        // Every N Days it's needed to set 2 as a default value instead of 1.
        val selectedNDays: Int = run {
            val period = (repeating?.getPeriod() as? RepeatingModel.Period.EveryNDays) ?: return@run 2
            if (period.nDays == 1) 2 else period.nDays
        }

        val selectedWeekDays: List<Boolean> = run {
            val period = (repeating?.getPeriod() as? RepeatingModel.Period.DaysOfWeek)
                ?: return@run listOf(false, false, false, false, false, false, false)
            (0..6).map { it in period.weekDays }.toList()
        }

        val selectedDaysOfMonth: Set<Int> = run {
            (repeating?.getPeriod() as? RepeatingModel.Period.DaysOfMonth)?.days ?: setOf()
        }

        val selectedDaysOfYear: List<RepeatingModel.Period.DaysOfYear.MonthDayItem> = run {
            (repeating?.getPeriod() as? RepeatingModel.Period.DaysOfYear)?.items ?: listOf()
        }

        state = MutableStateFlow(
            State(
                headerTitle = if (repeating != null) "Edit Repeating" else "New Repeating",
                headerDoneText = if (repeating != null) "Done" else "Create",
                inputTextValue = textFeatures.textNoFeatures,
                triggers = textFeatures.triggers,
                activePeriodIndex = activePeriodIndex,
                selectedNDays = selectedNDays,
                selectedWeekDays = selectedWeekDays,
                selectedDaysOfMonth = selectedDaysOfMonth,
                selectedDaysOfYear = selectedDaysOfYear,
            )
        )
    }

    fun setTextValue(text: String) {
        state.update { it.copy(inputTextValue = text) }
    }

    fun upTimerTime(timerString: String) {
        val timerTime = TimerTimeParser.findTime(state.value.inputTextValue)
        val newText = if (timerTime != null)
            state.value.inputTextValue.replace(timerTime.match, timerString)
        else
            state.value.inputTextValue.trim() + " $timerString"
        setTextValue(newText.trim() + " ")
    }

    fun setActivePeriodIndex(index: Int?) {
        state.update { it.copy(activePeriodIndex = index) }
    }

    fun setSelectedNDays(nDays: Int) {
        state.update { it.copy(selectedNDays = nDays) }
    }

    fun toggleWeekDay(index: Int) {
        state.update {
            it.copy(
                selectedWeekDays = it.selectedWeekDays
                    .toMutableList()
                    .apply {
                        set(index, !get(index))
                    }
            )
        }
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

    fun addDayOfTheYear(item: RepeatingModel.Period.DaysOfYear.MonthDayItem) {
        state.update {
            it.copy(
                selectedDaysOfYear = it.selectedDaysOfYear
                    .toMutableList()
                    .apply { add(item) }
                    .sortedWith(compareBy({ it.monthId }, { it.dayId }))
            )
        }
    }

    fun delDayOfTheYear(item: RepeatingModel.Period.DaysOfYear.MonthDayItem) {
        state.update {
            it.copy(
                selectedDaysOfYear = it.selectedDaysOfYear
                    .toMutableList()
                    .apply { remove(item) }
            )
        }
    }

    fun setTriggers(newTriggers: List<Trigger>) = state.update { it.copy(triggers = newTriggers) }

    fun save(
        onSuccess: () -> Unit
    ) = scopeVM().launchEx {
        try {
            // todo check if a text without features
            val nameWithFeatures = TextFeatures(
                textNoFeatures = state.value.inputTextValue,
                triggers = state.value.triggers,
            ).textWithFeatures()

            // !! Because "enabled" contains the checking
            val period = when (state.value.activePeriodIndex!!) {
                0 -> RepeatingModel.Period.EveryNDays(1)
                1 -> RepeatingModel.Period.EveryNDays(state.value.selectedNDays)
                2 -> RepeatingModel.Period.DaysOfWeek(
                    state.value.selectedWeekDays.mapIndexedNotNull { index, b -> if (b) index else null }
                )
                3 -> RepeatingModel.Period.DaysOfMonth(state.value.selectedDaysOfMonth)
                4 -> RepeatingModel.Period.DaysOfYear(state.value.selectedDaysOfYear)
                else -> throw Exception()
            }

            if (repeating != null) {
                repeating.upDataWithValidation(nameWithFeatures, period)
            } else
                RepeatingModel.addWithValidation(
                    nameWithFeatures,
                    period,
                    UnixTime().localDay
                )
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }
}
