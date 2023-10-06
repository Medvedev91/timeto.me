package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.DI
import me.timeto.shared.UnixTime
import me.timeto.shared.launchEx
import me.timeto.shared.localUtcOffset
import me.timeto.shared.vm.ui.ActivitiesPeriodUI

class SummarySheetVM : __VM<SummarySheetVM.State>() {

    data class State(
        val pickerTimeStart: UnixTime,
        val pickerTimeFinish: UnixTime,
        val activitiesUI: List<ActivitiesPeriodUI.ActivityUI>,
    ) {

        val minPickerTime: UnixTime = DI.firstInterval.unixTime()
        val maxPickerTime: UnixTime = DI.lastInterval.unixTime()

        val timeStartText: String = pickerTimeStart.getStringByComponents(buttonDateStringComponents)
        val timeFinishText: String = pickerTimeFinish.getStringByComponents(buttonDateStringComponents)

        val periodHints: List<PeriodHint>

        init {
            val now = UnixTime()
            val yesterday = now.inDays(-1)
            periodHints = listOfNotNull(
                PeriodHint(this, "Today", now, now),
                // Relevant for the first day, otherwise crash on click
                if (now.localDay > minPickerTime.localDay)
                    PeriodHint(this, "Yesterday", yesterday, yesterday)
                else null,
                PeriodHint(this, "7 days", yesterday.inDays(-6), yesterday),
                PeriodHint(this, "30 days", yesterday.inDays(-29), yesterday),
            )
        }
    }

    override val state: MutableStateFlow<State>

    init {
        val now = UnixTime()
        state = MutableStateFlow(
            State(
                pickerTimeStart = now,
                pickerTimeFinish = now,
                activitiesUI = listOf(), // todo
            )
        )
    }

    override fun onAppear() {
        setPeriod(state.value.pickerTimeStart, state.value.pickerTimeFinish)
    }

    fun setPeriod(
        pickerTimeStart: UnixTime,
        pickerTimeFinish: UnixTime,
    ) {
        scopeVM().launchEx {
            val activitiesPeriodUI = ActivitiesPeriodUI.build(
                dayStart = pickerTimeStart.localDay,
                dayFinish = pickerTimeFinish.localDay,
                utcOffset = localUtcOffset,
            )
            state.update {
                it.copy(
                    pickerTimeStart = pickerTimeStart,
                    pickerTimeFinish = pickerTimeFinish,
                    activitiesUI = activitiesPeriodUI.getActivitiesUI(),
                )
            }
        }
    }

    fun setPickerTimeStart(unixTime: UnixTime) = setPeriod(
        pickerTimeStart = unixTime,
        pickerTimeFinish = state.value.pickerTimeFinish,
    )

    fun setPickerTimeFinish(unixTime: UnixTime) = setPeriod(
        pickerTimeStart = state.value.pickerTimeStart,
        pickerTimeFinish = unixTime,
    )

    ///

    class PeriodHint(
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

private val buttonDateStringComponents = listOf(
    UnixTime.StringComponent.dayOfMonth,
    UnixTime.StringComponent.space,
    UnixTime.StringComponent.month3,
    UnixTime.StringComponent.comma,
    UnixTime.StringComponent.space,
    UnixTime.StringComponent.dayOfWeek3,
)
