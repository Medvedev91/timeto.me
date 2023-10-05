package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.DI
import me.timeto.shared.UnixTime
import me.timeto.shared.launchEx

class SummarySheetVM : __VM<SummarySheetVM.State>() {

    data class State(
        val pickerTimeStart: UnixTime,
        val pickerTimeFinish: UnixTime,
    ) {

        val minPickerTime: UnixTime = DI.firstInterval.unixTime()
        val maxPickerTime: UnixTime = DI.lastInterval.unixTime()

        val timeStartText: String = pickerTimeStart.getStringByComponents(buttonDateStringComponents)
        val timeFinishText: String = pickerTimeFinish.getStringByComponents(buttonDateStringComponents)

        val timeStartText: String = pickerTimeStart.getStringByComponents(buttonDateTextComponents)
        val timeFinishText: String = pickerTimeFinish.getStringByComponents(buttonDateTextComponents)
    }

    override val state: MutableStateFlow<State>

    init {
        val today = UnixTime()
        state = MutableStateFlow(
            State(
                pickerTimeStart = today,
                pickerTimeFinish = today,
            )
        )
    }

    fun setPeriod(
        pickerTimeStart: UnixTime,
        pickerTimeFinish: UnixTime,
    ) {
        scopeVM().launchEx {
            state.update {
                it.copy(
                    pickerTimeStart = pickerTimeStart,
                    pickerTimeFinish = pickerTimeFinish,
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
}

private val buttonDateStringComponents = listOf(
    UnixTime.StringComponent.dayOfMonth,
    UnixTime.StringComponent.space,
    UnixTime.StringComponent.month3,
    UnixTime.StringComponent.comma,
    UnixTime.StringComponent.space,
    UnixTime.StringComponent.dayOfWeek3,
)
