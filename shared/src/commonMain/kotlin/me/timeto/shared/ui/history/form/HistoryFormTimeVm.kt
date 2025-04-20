package me.timeto.shared.ui.history.form

import kotlinx.coroutines.flow.MutableStateFlow
import me.timeto.shared.misc.time
import me.timeto.shared.vm.__Vm

class HistoryFormTimeVm(
    initTime: Int,
) : __Vm<HistoryFormTimeVm.State>() {

    data class State(
        val initTime: Int,
        val timerItemUi: List<TimerItemUi>,
    )

    override val state = MutableStateFlow(
        State(
            initTime = initTime,
            timerItemUi = makeTimerItemsUi(selectedTime = initTime),
        )
    )

    ///

    data class TimerItemUi(
        val time: Int,
        val withToday: Boolean,
    ) {
        val title: String =
            HistoryFormUtils.makeTimeNote(time, withToday = withToday)
    }
}

private fun makeTimerItemsUi(
    selectedTime: Int,
): List<HistoryFormTimeVm.TimerItemUi> {
    val secondsSet: MutableSet<Int> = mutableSetOf(selectedTime)

    val timeStart1Min: Int = selectedTime - (selectedTime % 60)
    for (i in 1 until 10) { // 10 minutes
        secondsSet.add(timeStart1Min + (i * 60))
        secondsSet.add(timeStart1Min - (i * 60))
    }

    val timeStart5Min: Int = selectedTime - (selectedTime % (60 * 5))
    for (i in 1 until 12) { // ~ 1 hour
        secondsSet.add(timeStart5Min + (i * 60 * 5))
        secondsSet.add(timeStart5Min - (i * 60 * 5))
    }

    val timeStart10Min: Int = selectedTime - (selectedTime % (60 * 10))
    for (i in 1 until 144) { // ~ 1 day
        secondsSet.add(timeStart10Min + (i * 60 * 10))
        secondsSet.add(timeStart10Min - (i * 60 * 10))
    }

    val now: Int = time()
    return secondsSet.filter { it <= now }.sorted().map { time ->
        HistoryFormTimeVm.TimerItemUi(time, withToday = false)
    }
}
