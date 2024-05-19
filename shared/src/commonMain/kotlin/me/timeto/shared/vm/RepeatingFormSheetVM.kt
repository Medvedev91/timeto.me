package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.libs.DaytimeModel

class RepeatingFormSheetVM(
    private val repeating: RepeatingDb?
) : __VM<RepeatingFormSheetVM.State>() {

    data class State(
        val headerTitle: String,
        val headerDoneText: String,
        val textFeatures: TextFeatures,
        val daytimeModel: DaytimeModel?,
        val isImportant: Boolean,
        val activePeriodIndex: Int?,
        val selectedNDays: Int,
        val selectedWeekDays: List<Int>,
        val selectedDaysOfMonth: Set<Int>,
        val selectedDaysOfYear: List<RepeatingDb.Period.DaysOfYear.MonthDayItem>,
    ) {

        val daytimeHeader = "Time of the Day"
        val daytimeNote: String = daytimeModel?.text ?: "None"

        val defDaytimeModel: DaytimeModel = daytimeModel ?: DaytimeModel(hour = 12, minute = 0)

        val isImportantHeader = "Is Important"

        val inputTextValue = textFeatures.textNoFeatures
        val isHeaderDoneEnabled = inputTextValue.isNotBlank() &&
                                  activePeriodIndex != null &&
                                  textFeatures.activity != null &&
                                  textFeatures.timer != null

        // TRICK The order is hardcoded in ui
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
        val activePeriodIndex = if (repeating == null) null else {
            when (val period = repeating.getPeriod()) {
                is RepeatingDb.Period.EveryNDays -> if (period.nDays == 1) 0 else 1
                is RepeatingDb.Period.DaysOfWeek -> 2
                is RepeatingDb.Period.DaysOfMonth -> 3
                is RepeatingDb.Period.DaysOfYear -> 4
            }
        }

        // For UI "Every Day" and "Every N Days" is a different, but the logic is same EveryNDays.
        // That's by if the EveryNDays == 1, the "Every Day" would be checked, and for
        // Every N Days it's needed to set 2 as a default value instead of 1.
        val selectedNDays: Int = run {
            val period = (repeating?.getPeriod() as? RepeatingDb.Period.EveryNDays) ?: return@run 2
            if (period.nDays == 1) 2 else period.nDays
        }

        val selectedWeekDays: List<Int> = run {
            (repeating?.getPeriod() as? RepeatingDb.Period.DaysOfWeek)?.weekDays ?: listOf()
        }

        val selectedDaysOfMonth: Set<Int> = run {
            (repeating?.getPeriod() as? RepeatingDb.Period.DaysOfMonth)?.days ?: setOf()
        }

        val selectedDaysOfYear: List<RepeatingDb.Period.DaysOfYear.MonthDayItem> = run {
            (repeating?.getPeriod() as? RepeatingDb.Period.DaysOfYear)?.items ?: listOf()
        }

        state = MutableStateFlow(
            State(
                headerTitle = if (repeating != null) "Edit Repeating" else "New Repeating",
                headerDoneText = if (repeating != null) "Save" else "Create",
                textFeatures = (repeating?.text ?: "").textFeatures(),
                daytimeModel = repeating?.daytime?.let { DaytimeModel.byDaytime(it) },
                isImportant = repeating?.is_important?.toBoolean10() ?: false,
                activePeriodIndex = activePeriodIndex,
                selectedNDays = selectedNDays,
                selectedWeekDays = selectedWeekDays,
                selectedDaysOfMonth = selectedDaysOfMonth,
                selectedDaysOfYear = selectedDaysOfYear,
            )
        )
    }

    fun setTextValue(text: String) {
        state.update { it.copy(textFeatures = it.textFeatures.copy(textNoFeatures = text)) }
    }

    fun upTextFeatures(textFeatures: TextFeatures) {
        state.update { it.copy(textFeatures = textFeatures) }
    }

    fun upDaytime(daytimeModel: DaytimeModel?) {
        state.update { it.copy(daytimeModel = daytimeModel) }
    }

    fun toggleIsImportant() {
        state.update { it.copy(isImportant = !it.isImportant) }
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

    fun save(
        onSuccess: () -> Unit,
    ) = scopeVM().launchEx {
        try {
            // todo check if a text without features
            val nameWithFeatures = state.value.textFeatures.textWithFeatures()
            val isImportant = state.value.isImportant

            val daytime: Int? = state.value.daytimeModel?.seconds

            // !! Because "enabled" contains the checking
            val period = when (state.value.activePeriodIndex!!) {
                0 -> RepeatingDb.Period.EveryNDays(1)
                1 -> RepeatingDb.Period.EveryNDays(state.value.selectedNDays)
                2 -> RepeatingDb.Period.DaysOfWeek(state.value.selectedWeekDays)
                3 -> RepeatingDb.Period.DaysOfMonth(state.value.selectedDaysOfMonth)
                4 -> RepeatingDb.Period.DaysOfYear(state.value.selectedDaysOfYear)
                else -> throw Exception()
            }

            if (repeating != null) {
                repeating.upWithValidation(
                    text = nameWithFeatures,
                    period = period,
                    daytime = daytime,
                    isImportant = isImportant,
                )
                TaskDb.getAsc().forEach { task ->
                    val tf = task.text.textFeatures()
                    if (tf.fromRepeating?.id == repeating.id) {
                        val newTf = tf.copy(isImportant = isImportant)
                        task.upTextWithValidation(newTf.textWithFeatures())
                    }
                }
            } else {
                val lastDay: Int = if (period is RepeatingDb.Period.EveryNDays && period.nDays == 1)
                    UnixTime().localDay - 1
                else
                    UnixTime().localDay

                RepeatingDb.addWithValidation(
                    text = nameWithFeatures,
                    period = period,
                    lastDay = lastDay,
                    daytime = daytime,
                    isImportant = isImportant,
                )

                RepeatingDb.syncTodaySafe(RepeatingDb.todayWithOffset())
            }
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }
}
