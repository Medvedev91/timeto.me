package me.timeto.shared.ui.timer

import kotlinx.coroutines.flow.MutableStateFlow
import me.timeto.shared.vm.__Vm

class TimerVm(
    initSeconds: Int,
) : __Vm<TimerVm.State>() {

    val initSelectedSeconds: Int
    val pickerItems: List<PickerItem>

    data class State(
        val tmp: Boolean = true,
    )

    override val state: MutableStateFlow<State>

    init {
        initSelectedSeconds = initSeconds
        pickerItems = buildPickerItems(initSeconds)
        state = MutableStateFlow(State())
    }

    ///

    data class PickerItem(
        val seconds: Int,
        val title: String,
    )
}

///

private fun buildPickerItems(
    defSeconds: Int,
): List<TimerVm.PickerItem> {

    val a: List<Int> =
        (1..10).map { it * 60 } + // 1 - 10 min by 1 min
        (1..10).map { (600 + (it * 300)) } + // 15 min - 1 hour by 5 min
        (1..138).map { (3_600 + (it * 600)) } + // 1 hour + by 10 min
        defSeconds

    return a.toSet().sorted().map { seconds ->

        val hours: Int = seconds / 3600
        val minutes: Int = (seconds % 3600) / 60

        val title: String = when {
            hours == 0 -> "$minutes min"
            minutes == 0 -> "$hours h"
            else -> "$hours : ${minutes.toString().padStart(2, '0')}"
        }

        TimerVm.PickerItem(
            seconds = seconds,
            title = title
        )
    }
}
